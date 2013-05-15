package com.freeroom.persistence;

import com.freeroom.di.util.Pair;
import com.google.common.base.Optional;

import java.lang.reflect.Field;
import java.sql.*;
import java.util.List;

import static com.freeroom.di.util.FuncUtils.each;
import static com.google.common.base.Optional.absent;
import static com.google.common.base.Optional.of;
import static java.lang.String.format;

public class Athena
{
    private final Class<?> clazz;

    public static Athena from(final Class<?> clazz)
    {
        return new Athena(clazz);
    }

    public Athena(final Class<?> clazz)
    {
        this.clazz = clazz;
    }

    public Optional<Object> find(final int key)
    {
        String sql = format("SELECT * FROM %s WHERE %s", clazz.getSimpleName(),
                Atlas.getPrimaryKeyName(clazz) + "=" + key);

        try (Connection connection = getDBConnection()) {
            Object obj = newInstance(clazz);
            List<Field> columnFields = Atlas.getColumnFields(clazz);

            Statement statement = connection.createStatement();
            ResultSet resultSet = statement.executeQuery(sql);

            if (resultSet.next()) {
                for (Field columnField : columnFields) {
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

    public static void save(final Object obj)
    {
        List<Pair<String,Object>> columns = Atlas.getColumns(obj);

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

        try (Connection connection = getDBConnection()) {
            PreparedStatement statement = connection.prepareStatement(sql);

            int i = 1;
            for (Pair<String, Object> column : columns) {
                statement.setObject(i++, column.snd);
            }

            statement.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("DB exception.", e);
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

    private static Connection getDBConnection() throws SQLException
    {
        return DriverManager.getConnection("jdbc:hsqldb:mem:mydb", "sa", "");
    }
}
