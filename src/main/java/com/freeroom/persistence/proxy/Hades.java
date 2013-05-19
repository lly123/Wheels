package com.freeroom.persistence.proxy;

import com.freeroom.di.util.Pair;
import com.freeroom.persistence.Atlas;
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
import static com.google.common.base.Strings.isNullOrEmpty;
import static com.google.common.collect.ImmutableList.copyOf;
import static com.google.common.collect.Lists.newArrayList;
import static java.lang.String.format;

public class Hades
{
    private static Logger logger = Logger.getLogger(Hades.class);

    private final Properties properties;

    public Hades(final Properties properties)
    {
        this.properties = properties;
    }

    public Object create(final Class<?> clazz, final Pair<String, Long> primaryKeyAndValue)
    {
        final Enhancer enhancer = new Enhancer();
        enhancer.setSuperclass(clazz);
        enhancer.setCallback(new Charon(this, clazz, primaryKeyAndValue));
        return enhancer.create();
    }

    public void persistExisted(final Factory obj)
    {
        if (!isDirty(obj)) return;

        final Charon charon = (Charon)obj.getCallback(0);
        final Pair<String, Long> primaryKeyAndValue = charon.getPrimaryKeyAndValue();
        final List<Pair<Field, Object>> columns = Atlas.getBasicFieldAndValues(charon.getCurrent());

        if (columns.size() == 0) return;

        final StringBuilder questionMarksBuffer = new StringBuilder();

        each(columns, column -> {
            questionMarksBuffer.append(column.fst.getName() + "=?,");
        });

        final String questionMarks = removeTailComma(questionMarksBuffer);

        final String sql = format("UPDATE %s SET %s WHERE %s=?",
                charon.getPersistBeanName(), questionMarks, primaryKeyAndValue.fst);

        try (final Connection connection = getDBConnection()) {
            final PreparedStatement statement = connection.prepareStatement(sql);

            int i = 1;
            for (final Pair<Field, Object> column : columns) {
                setValue(i++, statement, column);
            }

            statement.setObject(i, primaryKeyAndValue.snd);
            statement.executeUpdate();
            logger.debug("Execute SQL: " + sql);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public void persistNew(final Object obj)
    {
        final List<Pair<Field, Object>> columns = Atlas.getBasicFieldAndValues(obj);

        final StringBuilder columnNamesBuffer = new StringBuilder();
        final StringBuilder questionMarksBuffer = new StringBuilder();

        each(columns, column -> {
            columnNamesBuffer.append(column.fst.getName() + ",");
            questionMarksBuffer.append("?,");
        });

        final String columnNames = removeTailComma(columnNamesBuffer);
        final String questionMarks = removeTailComma(questionMarksBuffer);

        final String sql = format("INSERT INTO %s (%s) VALUES (%s)",
                obj.getClass().getSimpleName(), columnNames, questionMarks);

        try (final Connection connection = getDBConnection()) {
            final PreparedStatement statement = connection.prepareStatement(sql);

            int i = 1;
            for (final Pair<Field, Object> column : columns) {
                setValue(i++, statement, column);
            }

            statement.executeUpdate();
            logger.debug("Execute SQL: " + sql);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
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
                    columnField.setAccessible(true);
                    setBasicFieldValue(columnField, obj, resultSet);
                }
                return obj;
            }
            throw new RuntimeException(format("Can't find %s with id %s", clazz.getSimpleName(), primaryKey));
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
