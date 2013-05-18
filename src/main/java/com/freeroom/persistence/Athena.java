package com.freeroom.persistence;

import com.freeroom.persistence.proxy.Hades;
import com.google.common.base.Optional;
import net.sf.cglib.proxy.Factory;
import org.apache.log4j.Logger;

import java.sql.*;
import java.util.Properties;

import static com.google.common.base.Optional.absent;
import static com.google.common.base.Optional.of;
import static java.lang.String.format;

public class Athena
{
    private static Logger logger = Logger.getLogger(Athena.class);

    private final Properties properties;
    private final Hades hades;
    private Optional<Class<?>> entityClass;

    public Athena(final Properties properties)
    {
        this.properties = properties;
        this.hades = new Hades(properties);
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
        final String primaryKeyName = Atlas.getPrimaryKeyName(clazz);
        final String sql = format("SELECT %s FROM %s WHERE %s=?",
                primaryKeyName, clazz.getSimpleName(), primaryKeyName);

        try (Connection connection = getDBConnection()) {
            final PreparedStatement statement = connection.prepareStatement(sql);
            statement.setObject(1, key);

            final ResultSet resultSet = statement.executeQuery();
            logger.debug("Execute SQL: " + sql);

            if (resultSet.next()) {
                return of(hades.create(clazz, resultSet.getLong(1)));
            }
        } catch (Exception ignored) {}
        return absent();
    }

    public void persist(final Object obj)
    {
        if (obj instanceof Factory) {
            hades.persist(obj);
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

    private Connection getDBConnection() throws SQLException
    {
        return DriverManager.getConnection(
                properties.getProperty("url"),
                properties.getProperty("username"),
                properties.getProperty("password"));
    }
}
