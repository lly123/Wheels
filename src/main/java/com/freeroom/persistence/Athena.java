package com.freeroom.persistence;

import com.freeroom.di.util.Pair;
import com.google.common.base.Optional;
import org.apache.log4j.Logger;

import java.lang.reflect.Field;
import java.sql.*;
import java.util.List;
import java.util.Properties;

import static com.freeroom.di.util.FuncUtils.each;
import static com.google.common.base.Optional.absent;
import static com.google.common.base.Optional.of;
import static java.lang.String.format;

public class Athena
{
    private static Logger logger = Logger.getLogger(Athena.class);

    private final Properties properties;
    private Optional<Class<?>> entityClass;

    public Athena(final Properties properties)
    {
        this.properties = properties;
    }

    public Athena from(final Class<?> clazz)
    {
        this.entityClass = Optional.<Class<?>>of(clazz);
        return this;
    }

    public Optional<Object> find(final int key)
    {
        assertEntityClassExists();

        final Class<?> clazz = entityClass.get();
        final String sql = format("SELECT * FROM %s WHERE %s=?", clazz.getSimpleName(), Atlas.getPrimaryKeyName(clazz));

        try (Connection connection = getDBConnection()) {
            final Object obj = newInstance(clazz);
            final List<Field> columnFields = Atlas.getBasicFields(clazz);

            final PreparedStatement statement = connection.prepareStatement(sql);
            statement.setObject(1, key);

            final ResultSet resultSet = statement.executeQuery();
            logger.debug("Execute SQL: " + sql);

            if (resultSet.next()) {
                for (final Field columnField : columnFields) {
                    columnField.setAccessible(true);
                    columnField.set(obj, resultSet.getObject(columnField.getName()));
                }
                return of(obj);
            }
        } catch (Exception e) {
            throw new RuntimeException("DB exception.", e);
        }
        return absent();
    }

    public void save(final Object obj)
    {
        final List<Pair<String,Object>> columns = Atlas.getColumns(obj);

        final StringBuilder columnNamesBuffer = new StringBuilder();
        final StringBuilder questionMarksBuffer = new StringBuilder();
        each(columns, column -> {
            columnNamesBuffer.append(column.fst + ",");
            questionMarksBuffer.append("?,");
        });

        final String columnNames = columnNamesBuffer.deleteCharAt(columnNamesBuffer.length() - 1).toString();
        final String questionMarks = questionMarksBuffer.deleteCharAt(questionMarksBuffer.length() - 1).toString();

        final String sql = format("INSERT INTO %s (%s) VALUES (%s)",
                obj.getClass().getSimpleName(), columnNames, questionMarks);

        try (final Connection connection = getDBConnection()) {
            final PreparedStatement statement = connection.prepareStatement(sql);

            int i = 1;
            for (final Pair<String, Object> column : columns) {
                statement.setObject(i++, column.snd);
            }

            statement.executeUpdate();
            logger.debug("Execute SQL: " + sql);
        } catch (SQLException e) {
            throw new RuntimeException("DB exception.", e);
        }
    }

    public void clear(final Class<?> clazz)
    {
        final String sql = format("DELETE FROM %s", clazz.getSimpleName());
        try (final Connection connection = getDBConnection()) {
            final PreparedStatement statement = connection.prepareStatement(sql);
            statement.executeUpdate();
            logger.debug("Execute SQL: " + sql);
        } catch (SQLException e) {
            throw new RuntimeException("DB exception.", e);
        }
    }

    private void assertEntityClassExists()
    {
        if (!entityClass.isPresent()) {
            throw new RuntimeException("Using from() to set entity class.");
        }
    }

    private Object newInstance(final Class<?> clazz)
    {
        try {
            return clazz.getConstructor().newInstance();
        } catch (Exception e){
            throw new RuntimeException("Get exception when creating " + clazz, e);
        }
    }

    private Connection getDBConnection() throws SQLException
    {
        return DriverManager.getConnection(
                properties.getProperty("url"),
                properties.getProperty("username"),
                properties.getProperty("password"));
    }
}
