package com.freeroom.persistence.proxy;

import com.freeroom.persistence.Atlas;
import net.sf.cglib.proxy.Enhancer;
import net.sf.cglib.proxy.Factory;
import org.apache.log4j.Logger;

import java.lang.reflect.Field;
import java.lang.reflect.Type;
import java.sql.*;
import java.util.List;
import java.util.Properties;

import static com.freeroom.di.util.FuncUtils.map;
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

    public Object create(final Class<?> clazz, final long primaryKey)
    {
        final Enhancer enhancer = new Enhancer();
        enhancer.setSuperclass(clazz);
        enhancer.setCallback(new Charon(this, clazz, primaryKey));
        return enhancer.create();
    }

    protected Object load(final Class<?> clazz, final long primaryKey)
    {
        final String sql = format("SELECT * FROM %s WHERE %s=?", clazz.getSimpleName(), Atlas.getPrimaryKeyName(clazz));

        try (Connection connection = getDBConnection()) {
            final Object obj = newInstance(clazz);
            final List<Field> columnFields = Atlas.getBasicFields(clazz);

            final PreparedStatement statement = connection.prepareStatement(sql);
            statement.setLong(1, primaryKey);

            final ResultSet resultSet = statement.executeQuery();
            logger.debug("Execute SQL: " + sql);

            if (resultSet.next()) {
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
        } else if (Atlas.isListInteger(field)) {
            String values = resultSet.getString(field.getName());
            field.set(obj, newArrayList(map(copyOf(values.split(",")), value -> Integer.parseInt(value))));
        }
    }

    protected boolean isDirty(final Object obj)
    {
        Charon charon = (Charon)((Factory) obj).getCallback(0);
        return charon.isDirty();
    }

    protected Object newInstance(final Class<?> clazz)
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
