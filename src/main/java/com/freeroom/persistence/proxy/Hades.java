package com.freeroom.persistence.proxy;

import com.freeroom.di.util.Pair;
import com.freeroom.persistence.Atlas;
import com.google.common.base.Optional;
import net.sf.cglib.proxy.Enhancer;
import net.sf.cglib.proxy.Factory;
import org.apache.log4j.Logger;

import java.lang.reflect.Field;
import java.lang.reflect.Type;
import java.sql.*;
import java.util.List;
import java.util.Properties;

import static com.freeroom.di.util.FuncUtils.each;
import static com.freeroom.di.util.FuncUtils.map;
import static com.freeroom.persistence.Atlas.isList;
import static com.google.common.base.Strings.isNullOrEmpty;
import static com.google.common.collect.ImmutableList.copyOf;
import static com.google.common.collect.Lists.newArrayList;
import static java.lang.String.format;
import static java.sql.Statement.RETURN_GENERATED_KEYS;

public class Hades
{
    private static Logger logger = Logger.getLogger(Hades.class);

    private final Properties properties;

    public Hades(final Properties properties)
    {
        this.properties = properties;
    }

    public Object create(final Class<?> clazz, final Long primaryKey, final int blockSize)
    {
        final Enhancer enhancer = new Enhancer();
        enhancer.setSuperclass(clazz);
        enhancer.setCallback(new Charon(this, clazz, Pair.of(Atlas.getPrimaryKeyName(clazz), primaryKey), blockSize));
        return enhancer.create();
    }

    public List<Object> createList(final Class<?> clazz, final String sql,
                                   final Optional<Long> foreignKey, final int blockSize)
    {
        final Enhancer enhancer = new Enhancer();
        enhancer.setSuperclass(List.class);
        enhancer.setCallback(new Hecate(this, clazz, sql, foreignKey, blockSize));
        return (List<Object>)enhancer.create();
    }

    public void persist(final Object obj, final Optional<Pair<String, Long>> foreignKeyAndValue)
    {
        if (obj == null) return;

        if (obj instanceof Factory) {
            if (((Factory)obj).getCallback(0) instanceof Charon) {
                persistExisted((Factory)obj, foreignKeyAndValue);
            } else {
                persistExistedList((Factory)obj, foreignKeyAndValue);
            }
        } else {
            if (isList(obj)) {
                persistNewList((List)obj, foreignKeyAndValue);
            } else {
                persistNew(obj, foreignKeyAndValue);
            }
        }
    }

    public void persistExisted(final Factory obj, final Optional<Pair<String, Long>> foreignKeyAndValue)
    {
        if (!isDirty(obj)) return;

        final Charon charon = (Charon)obj.getCallback(0);
        final Pair<String, Long> primaryKeyAndValue = charon.getPrimaryKeyAndValue();
        final List<Pair<Field, Object>> basicFields = Atlas.getBasicFieldAndValues(charon.getCurrent());

        if (basicFields.size() == 0) return;

        final StringBuilder questionMarksBuffer = new StringBuilder();

        each(basicFields, field -> {
            questionMarksBuffer.append(field.fst.getName() + "=?,");
        });

        final String questionMarks = removeTailComma(questionMarksBuffer);

        final String sql = format("UPDATE %s SET %s%s WHERE %s=?",
                charon.getPersistBeanName(),
                questionMarks,
                foreignKeyAndValue.isPresent() ? "," + foreignKeyAndValue.get().fst + "=?" : "",
                primaryKeyAndValue.fst);

        try (final Connection connection = getDBConnection()) {
            final PreparedStatement statement = connection.prepareStatement(sql);

            int i = 1;
            for (final Pair<Field, Object> column : basicFields) {
                setValue(i++, statement, column);
            }

            if (foreignKeyAndValue.isPresent()) {
                statement.setLong(i++, foreignKeyAndValue.get().snd);
            }
            statement.setObject(i, primaryKeyAndValue.snd);
            statement.executeUpdate();
            logger.debug("Execute SQL: " + sql);

            persistRelations(charon.getCurrent(), primaryKeyAndValue);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public void persistNew(final Object obj, final Optional<Pair<String, Long>> foreignKeyAndValue)
    {
        final List<Pair<Field, Object>> basicFields = Atlas.getBasicFieldAndValues(obj);

        final StringBuilder fieldNamesBuffer = new StringBuilder();
        final StringBuilder questionMarksBuffer = new StringBuilder();

        each(basicFields, field -> {
            fieldNamesBuffer.append(field.fst.getName() + ",");
            questionMarksBuffer.append("?,");
        });

        final String fieldNames = removeTailComma(fieldNamesBuffer);
        final String questionMarks = removeTailComma(questionMarksBuffer);

        final String sql = format("INSERT INTO %s (%s%s) VALUES (%s%s)",
                obj.getClass().getSimpleName(),
                fieldNames, foreignKeyAndValue.isPresent() ? "," + foreignKeyAndValue.get().fst : "",
                questionMarks, foreignKeyAndValue.isPresent() ? ",?" : "");

        try (final Connection connection = getDBConnection()) {
            final PreparedStatement statement = connection.prepareStatement(sql, RETURN_GENERATED_KEYS);

            int i = 1;
            for (final Pair<Field, Object> column : basicFields) {
                setValue(i++, statement, column);
            }

            if (foreignKeyAndValue.isPresent()) {
                statement.setLong(i, foreignKeyAndValue.get().snd);
            }
            statement.executeUpdate();
            logger.debug("Execute SQL: " + sql);

            final ResultSet generatedKeys = statement.getGeneratedKeys();
            if (generatedKeys.next()) {
                persistRelations(obj, Pair.of(Atlas.getPrimaryKeyName(obj.getClass()), generatedKeys.getLong(1)));
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public void persistExistedList(final Factory obj, final Optional<Pair<String, Long>> foreignKeyAndValue)
    {
        if (!isDirtyList(obj)) return;

        final Hecate hecate = (Hecate)obj.getCallback(0);
        each(hecate.getRemoved(), o -> remove(o));
        each(hecate.getAdded(), o -> persistNew(o, foreignKeyAndValue));
        each(hecate.getModified(), o -> persistExisted(o, foreignKeyAndValue));
    }

    public void persistNewList(final List objects, final Optional<Pair<String, Long>> foreignKeyAndValue)
    {
        for (final Object o : objects) {
            persistNew(o, foreignKeyAndValue);
        }
    }

    private void persistRelations(final Object obj, final Pair<String, Long> primaryKeyAndValue)
    {
        final Pair<String, Long> foreignKeyAndValue =
                Pair.of(obj.getClass().getSimpleName() + "_" + primaryKeyAndValue.fst, primaryKeyAndValue.snd);

        final List<Pair<Field, Class>> oneToManyRelations = Atlas.getOneToManyRelations(obj.getClass());
        each(oneToManyRelations, relation -> {
            try {
                persist(relation.fst.get(obj), Optional.of(foreignKeyAndValue));
            } catch (Exception ignored) {}
        });

        final List<Field> oneToOneRelations = Atlas.getOneToOneRelations(obj.getClass());
        each(oneToOneRelations, relation -> {
            try {
                persist(relation.get(obj), Optional.of(foreignKeyAndValue));
            } catch (Exception ignored) {}
        });
    }

    public void remove(final Factory obj)
    {
        final Charon charon = (Charon)obj.getCallback(0);
        final Pair<String, Long> primaryKeyAndValue = charon.getPrimaryKeyAndValue();
        final String sql = format("DELETE FROM %s WHERE %s=?", charon.getPersistBeanName(), primaryKeyAndValue.fst);

        try (final Connection connection = getDBConnection()) {
            final PreparedStatement statement = connection.prepareStatement(sql);
            statement.setLong(1, primaryKeyAndValue.snd);
            statement.executeUpdate();
            logger.debug("Execute SQL: " + sql);

            charon.removed();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    private void setValue(final int i, final PreparedStatement statement,
                          final Pair<Field, Object> column) throws SQLException
    {
        if (Atlas.isListLong(column.fst)) {
            final StringBuilder buffer = new StringBuilder();

            for (final Long value : (List<Long>)column.snd) {
                buffer.append(value + ",");
            }

            statement.setString(i, removeTailComma(buffer));
        } else {
            statement.setObject(i, column.snd);
        }
    }

    public boolean isDirty(final Object obj)
    {
        if (!(obj instanceof Factory)) return false;

        final Charon charon = (Charon)((Factory)obj).getCallback(0);
        return charon.isDirty();
    }

    public boolean isDirtyList(final Object objs)
    {
        if (!(objs instanceof Factory)) return false;

        final Hecate hecate = (Hecate)((Factory)objs).getCallback(0);
        return hecate.isDirty();
    }

    protected Object load(final Class<?> clazz, final long primaryKey)
    {
        final String sql = format("SELECT * FROM %s WHERE %s=?", clazz.getSimpleName(), Atlas.getPrimaryKeyName(clazz));

        try (Connection connection = getDBConnection()) {
            final Object obj = newInstance(clazz);
            final Field pkField = Atlas.getPrimaryKey(clazz);
            final List<Field> columnFields = Atlas.getBasicFields(clazz);

            final PreparedStatement statement = connection.prepareStatement(sql);
            statement.setLong(1, primaryKey);

            final ResultSet resultSet = statement.executeQuery();
            logger.debug("Execute SQL: " + sql);

            if (resultSet.next()) {
                pkField.setLong(obj, primaryKey);
                for (final Field columnField : columnFields) {
                    setBasicFieldValue(columnField, obj, resultSet);
                }
                return obj;
            }
            throw new RuntimeException(format("Can't find %s with id %s", clazz.getSimpleName(), primaryKey));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public List<Object> loadList(final Class<?> clazz, final String sql,
                                 final Optional<Long> foreignKey, final int blockSize)
    {
        try (Connection connection = getDBConnection()) {
            final PreparedStatement statement = connection.prepareStatement(sql);

            if (foreignKey.isPresent()) {
                statement.setLong(1, foreignKey.get());
            }

            final ResultSet resultSet = statement.executeQuery();
            logger.debug("Execute SQL: " + sql);

            final List<Object> retVal = newArrayList();
            while (resultSet.next()) {
                retVal.add(create(clazz, resultSet.getLong(1), blockSize));
            }
            return retVal;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    protected Object newInstance(final Class<?> clazz)
    {
        try {
            return clazz.getConstructor().newInstance();
        } catch (Exception e){
            throw new RuntimeException("Get exception when creating " + clazz, e);
        }
    }

    private void setBasicFieldValue(final Field field, final Object obj, final ResultSet resultSet) throws Exception
    {
        final Type fieldType = field.getGenericType();
        if (fieldType.equals(String.class)) {
            field.set(obj, resultSet.getString(field.getName()));
        } else if (fieldType.equals(int.class) || fieldType.equals(Integer.class)) {
            field.setInt(obj, resultSet.getInt(field.getName()));
        } else if (fieldType.equals(long.class) || fieldType.equals(Long.class)) {
            field.setLong(obj, resultSet.getLong(field.getName()));
        } else if (fieldType.equals(double.class) || fieldType.equals(Double.class)) {
            field.setDouble(obj, resultSet.getDouble(field.getName()));
        } else if (fieldType.equals(boolean.class) || fieldType.equals(Boolean.class)) {
            field.setBoolean(obj, resultSet.getBoolean(field.getName()));
        } else if (Atlas.isListLong(field)) {
            String values = resultSet.getString(field.getName());
            if (!isNullOrEmpty(values)) {
                field.set(obj, newArrayList(map(copyOf(values.split(",")), value -> Long.parseLong(value))));
            }
        }
    }

    private Connection getDBConnection() throws SQLException
    {
        return DriverManager.getConnection(
                properties.getProperty("url"),
                properties.getProperty("username"),
                properties.getProperty("password"));
    }

    private String removeTailComma(final StringBuilder buffer)
    {
        if (buffer.length() > 0) {
            return buffer.deleteCharAt(buffer.length() - 1).toString();
        } else {
            return "";
        }
    }
}
