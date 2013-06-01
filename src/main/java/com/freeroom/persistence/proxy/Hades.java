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
import static com.freeroom.persistence.proxy.IdPurpose.Locate;
import static com.freeroom.persistence.proxy.IdPurpose.Remove;
import static com.freeroom.persistence.proxy.IdPurpose.Update;
import static com.google.common.base.Optional.absent;
import static com.google.common.base.Optional.of;
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

    public Optional<Pair<String, Long>> persist(final Object obj, final Optional<Pair<String, Long>> foreignKeyAndValue)
    {
        if (obj == null) absent();

        if (obj instanceof Factory) {
            if (((Factory)obj).getCallback(0) instanceof Charon) {
                return of(persistExisted((Factory) obj, foreignKeyAndValue));
            } else {
                persistExistedList((Factory)obj, foreignKeyAndValue);
            }
        } else {
            if (isList(obj)) {
                persistNewList((List)obj, foreignKeyAndValue);
            } else {
                final Pair<String, Long> primaryKeyNameAndValue = Atlas.getPrimaryKeyNameAndValue(obj);
                final Optional<IdPurpose> idPurpose = Atlas.getIdPurpose(obj);
                if (primaryKeyNameAndValue.snd > 0 && idPurpose.isPresent()) {
                    if (idPurpose.get() == Locate) {
                        persistRelations(obj, primaryKeyNameAndValue);
                        return of(primaryKeyNameAndValue);
                    } else if (idPurpose.get() == Update) {
                        update(obj, primaryKeyNameAndValue, absent());
                        persistRelations(obj, primaryKeyNameAndValue);
                        return of(primaryKeyNameAndValue);
                    } else if (idPurpose.get() == Remove) {
                        remove((Factory)create(obj.getClass(), primaryKeyNameAndValue.snd, 1));
                    }
                } else {
                    return persistNew(obj, foreignKeyAndValue);
                }
            }
        }
        return absent();
    }

    protected Optional<Pair<String, Long>> persistNew(final Object obj, final Optional<Pair<String, Long>> foreignKeyAndValue)
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
                final Pair<String, Long> primaryKeyAndValue =
                        Pair.of(Atlas.getPrimaryKeyName(obj.getClass()), generatedKeys.getLong(1));
                persistRelations(obj, primaryKeyAndValue);
                return of(primaryKeyAndValue);
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return absent();
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
        } catch (SQLException ignored) {}
        charon.removed();
    }

    protected boolean isDirty(final Object obj)
    {
        if (!(obj instanceof Factory)) return false;

        final Charon charon = (Charon)((Factory)obj).getCallback(0);
        return charon.isDirty();
    }

    protected boolean isDirtyList(final Object objs)
    {
        if (!(objs instanceof Factory)) return false;

        final Hecate hecate = (Hecate)((Factory)objs).getCallback(0);
        return hecate.isDirty();
    }

    protected Pair<Object, List<Long>> load(final Class<?> clazz, final long primaryKey,
                                            final List<Pair<Field, String>> relations)
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
                return Pair.of(obj, relationIds(relations, resultSet));
            }
            throw new RuntimeException(format("Can't find %s with id %s", clazz.getSimpleName(), primaryKey));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    protected Pair<Object, List<Long>> load(final Class<?> clazz, final long primaryKey,
                                            final ResultSet resultSet, final List<Pair<Field, String>> relations)
    {
        final Object obj = newInstance(clazz);
        final Field pkField = Atlas.getPrimaryKey(clazz);
        final List<Field> columnFields = Atlas.getBasicFields(clazz);

        try {
            pkField.setLong(obj, primaryKey);
            for (final Field columnField : columnFields) {
                setBasicFieldValue(columnField, obj, resultSet);
            }
            return Pair.of(obj, relationIds(relations, resultSet));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    protected List<Object> loadList(final Class<?> clazz, final String sql,
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

    protected void loadBatch(final Class<?> clazz, final int beginIndex, final int blockSize, List<Object> objects)
    {
        final String primaryKeyName = Atlas.getPrimaryKeyName(clazz);
        final String ids = getBlockIds(beginIndex, blockSize, objects);

        if (ids.length() > 0) {
            final String sql = format("SELECT * FROM %s WHERE %s in (%s)", clazz.getSimpleName(), primaryKeyName, ids);

            try (Connection connection = getDBConnection()) {
                final PreparedStatement statement = connection.prepareStatement(sql);

                final ResultSet resultSet = statement.executeQuery();
                logger.debug("Execute SQL: " + sql);

                while (resultSet.next()) {
                    fillObjectByResultSet(objects, primaryKeyName, resultSet);
                }
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
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

    private Pair<String, Long> persistExisted(final Factory obj, final Optional<Pair<String, Long>> foreignKeyAndValue)
    {
        final Charon charon = (Charon)obj.getCallback(0);
        final Pair<String, Long> primaryKeyAndValue = charon.getPrimaryKeyAndValue();
        if (isDirty(obj)) {
            update(charon.getCurrent(), primaryKeyAndValue, foreignKeyAndValue);
        }
        persistRelations(charon.getCurrent(), primaryKeyAndValue);
        return primaryKeyAndValue;
    }

    private void persistExistedList(final Factory obj, final Optional<Pair<String, Long>> foreignKeyAndValue)
    {
        if (!isDirtyList(obj)) return;

        final Hecate hecate = (Hecate)obj.getCallback(0);
        each(hecate.getRemoved(), o -> remove(o));
        each(hecate.getAdded(), o -> persistNew(o, foreignKeyAndValue));
        each(hecate.getModified(), o -> persistExisted(o, foreignKeyAndValue));
    }

    private void persistNewList(final List objects, final Optional<Pair<String, Long>> foreignKeyAndValue)
    {
        for (final Object o : objects) {
            persist(o, foreignKeyAndValue);
        }
    }

    private void persistRelations(final Object obj, final Pair<String, Long> primaryKeyAndValue)
    {
        final Pair<String, Long> foreignKeyAndValue =
                Pair.of(obj.getClass().getSimpleName() + "_" + primaryKeyAndValue.fst, primaryKeyAndValue.snd);

        final List<Pair<Field, Class>> oneToManyRelations = Atlas.getOneToManyRelations(obj.getClass());
        each(oneToManyRelations, relation -> {
            try {
                persist(relation.fst.get(obj), of(foreignKeyAndValue));
            } catch (Exception ignored) {}
        });

        final List<Field> relationsWithoutFK = Atlas.getOneToOneRelationsWithoutForeignKey(obj.getClass());
        each(relationsWithoutFK, relation -> {
            try {
                persist(relation.get(obj), of(foreignKeyAndValue));
            } catch (Exception ignored) {}
        });

        persistOneToOneRelationsWithForeignKey(obj, primaryKeyAndValue);
    }

    private void persistOneToOneRelationsWithForeignKey(final Object obj, final Pair<String, Long> primaryKeyAndValue)
    {
        final StringBuilder questionMarksBuffer = new StringBuilder();
        final List<Long> childrenIds = newArrayList();
        final List<Pair<Field, String>> relationsWithFK = Atlas.getOneToOneRelationsWithForeignKey(obj.getClass());
        for (Pair<Field, String> relation : relationsWithFK) {
            try {
                final Class<?> childClass = relation.fst.getType();
                final Object relatedObj = relation.fst.get(obj);

                Optional<IdPurpose> idPurpose = absent();
                if (relatedObj != null) {
                    idPurpose = Atlas.getIdPurpose(relatedObj);
                }

                if (relatedObj == null || (idPurpose.isPresent() && idPurpose.get().equals(Remove))) {
                    final String primaryKeyName = Atlas.getPrimaryKeyName(childClass);
                    addChildId(questionMarksBuffer, childrenIds, childClass, primaryKeyName, 0L);
                }

                final Optional<Pair<String, Long>> keyAndValue = persist(relatedObj, absent());
                if (keyAndValue.isPresent()) {
                    addChildId(questionMarksBuffer, childrenIds, childClass, keyAndValue.get().fst, keyAndValue.get().snd);
                }
            } catch (Exception ignored) {}
        }

        if (childrenIds.size() > 0) {
            final String questionMarks = removeTailComma(questionMarksBuffer);
            final String sql = format("UPDATE %s SET %s WHERE %s=?",
                    obj.getClass().getSimpleName(), questionMarks, primaryKeyAndValue.fst);

            try (final Connection connection = getDBConnection()) {
                final PreparedStatement statement = connection.prepareStatement(sql);

                int i = 1;
                for (final Long id : childrenIds) {
                    statement.setLong(i++, id);
                }

                statement.setLong(i, primaryKeyAndValue.snd);
                statement.executeUpdate();
                logger.debug("Execute SQL: " + sql);
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        }
    }

    private Pair<String, Long> update(final Object obj, final Pair<String, Long> primaryKeyAndValue,
                                      final Optional<Pair<String, Long>> foreignKeyAndValue)
    {
        final List<Pair<Field, Object>> basicFields = Atlas.getBasicFieldAndValues(obj);

        if (basicFields.size() == 0) return primaryKeyAndValue;

        final StringBuilder questionMarksBuffer = new StringBuilder();
        each(basicFields, field -> {
            questionMarksBuffer.append(field.fst.getName() + "=?,");
        });

        final String questionMarks = removeTailComma(questionMarksBuffer);

        final String sql = format("UPDATE %s SET %s%s WHERE %s=?",
                obj.getClass().getSimpleName(),
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

            return primaryKeyAndValue;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    private void addChildId(final StringBuilder questionMarksBuffer, final List<Long> childrenIds,
                            final Class<?> childClass, final String childPrimaryKey, final Long childId)
    {
        questionMarksBuffer.append(childClass.getSimpleName() + "_" + childPrimaryKey + "=?,");
        childrenIds.add(childId);
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
        } else if (Atlas.isEnumType(column.fst.getType())) {
            statement.setString(i, column.snd.toString());
        } else {
            statement.setObject(i, column.snd);
        }
    }

    private List<Long> relationIds(final List<Pair<Field, String>> relations, final ResultSet resultSet)
    {
        List<Long> retVal = newArrayList();
        try {
            for (Pair<Field, String> relation : relations) {
                retVal.add(resultSet.getLong(relation.snd));
            }
        } catch (Exception ignored) {}
        return retVal;
    }

    private String getBlockIds(final int beginIndex, final int blockSize, final List<Object> objects)
    {
        Charon charon = (Charon)((Factory)objects.get(beginIndex)).getCallback(0);
        if (charon.isNotLoaded()) {
            int count = 0;
            final StringBuilder idsBuffer = new StringBuilder();
            for (int i = beginIndex; i < objects.size() && count < blockSize; i++) {
                charon = (Charon)((Factory)objects.get(i)).getCallback(0);
                if (charon.isNotLoaded()) {
                    idsBuffer.append(charon.getPrimaryKeyAndValue().snd + ",");
                    count++;
                }
            }
            return removeTailComma(idsBuffer);
        }
        return "";
    }

    private void fillObjectByResultSet(final List<Object> objects, final String primaryKeyName,
                                       final ResultSet resultSet) throws SQLException
    {
        final long pk = resultSet.getLong(primaryKeyName);
        for (Object o : objects) {
            final Charon charon = (Charon) ((Factory) o).getCallback(0);
            if (charon.getPrimaryKeyAndValue().snd == pk) {
                charon.load(of(resultSet));
                break;
            }
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
        } else if (Atlas.isEnumType(fieldType)) {
            field.set(obj, Enum.valueOf((Class)fieldType, resultSet.getString(field.getName())));
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
