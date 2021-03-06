package com.freeroom.persistence;

import com.freeroom.di.annotations.Bean;
import com.freeroom.di.annotations.Inject;
import com.freeroom.persistence.exceptions.NotOnlyResultException;
import com.freeroom.persistence.proxy.Charon;
import com.freeroom.persistence.proxy.Hades;
import com.freeroom.persistence.proxy.Hecate;
import com.google.common.base.Optional;
import net.sf.cglib.proxy.Callback;
import net.sf.cglib.proxy.Factory;
import org.apache.log4j.Logger;

import java.sql.*;
import java.util.List;
import java.util.Properties;

import static com.google.common.base.Optional.absent;
import static com.google.common.base.Optional.of;
import static java.lang.Integer.parseInt;
import static java.lang.String.format;

@Bean
public class Athena
{
    private static Logger logger = Logger.getLogger(Athena.class);

    private final Properties properties;
    private final Hades hades;
    private Optional<Class<?>> entityClass;

    @Inject
    public Athena(final DBConfig dbConfig)
    {
        this.properties = dbConfig.getDbProperties();
        this.hades = new Hades(properties);
    }

    public Athena from(final Class<?> clazz)
    {
        this.entityClass = Optional.<Class<?>>of(clazz);
        return this;
    }

    public Optional<Object> find(final long key)
    {
        assertEntityClassExists();

        final Class<?> clazz = entityClass.get();
        final String primaryKeyName = Atlas.getPrimaryKeyName(clazz);
        final String sql = format("SELECT %s FROM %s WHERE %s=?", primaryKeyName, clazz.getSimpleName(), primaryKeyName);

        try (Connection connection = getDBConnection()) {
            final PreparedStatement statement = connection.prepareStatement(sql);
            statement.setLong(1, key);

            final ResultSet resultSet = statement.executeQuery();
            logger.debug("Execute SQL: " + sql);

            if (resultSet.next()) {
                return of(hades.create(clazz, resultSet.getLong(1), getBlockSize()));
            }
        } catch (Exception ignored) {}
        return absent();
    }

    public Optional<Object> findOnly(final String where)
    {
        assertEntityClassExists();

        final Class<?> clazz = entityClass.get();
        final String primaryKeyName = Atlas.getPrimaryKeyName(clazz);
        final String sql = format("SELECT %s FROM %s WHERE %s", primaryKeyName, clazz.getSimpleName(), where);

        try (Connection connection = getDBConnection()) {
            final PreparedStatement statement = connection.prepareStatement(sql);

            final ResultSet resultSet = statement.executeQuery();
            logger.debug("Execute SQL: " + sql);

            if (resultSet.next()) {
                final Optional<Object> retVal = of(hades.create(clazz, resultSet.getLong(1), getBlockSize()));

                if (resultSet.next()) {
                    throw new NotOnlyResultException("Found more than one result.");
                }

                return retVal;
            }
        } catch (Exception ignored) {}
        return absent();
    }

    public List<Object> find(final String where)
    {
        assertEntityClassExists();

        final Class<?> clazz = entityClass.get();
        final String primaryKeyName = Atlas.getPrimaryKeyName(clazz);
        final String sql = format("SELECT %s FROM %s WHERE %s", primaryKeyName, clazz.getSimpleName(), where);

        return hades.createList(clazz, sql, absent(), getBlockSize());
    }

    public List<Object> all()
    {
        assertEntityClassExists();

        final Class<?> clazz = entityClass.get();
        final String primaryKeyName = Atlas.getPrimaryKeyName(clazz);
        final String sql = format("SELECT %s FROM %s", primaryKeyName, clazz.getSimpleName());

        return hades.createList(clazz, sql, absent(), getBlockSize());
    }

    public void persist(final Object obj)
    {
        hades.persist(obj, absent());
    }

    public void remove(final Object obj)
    {
        if (obj instanceof Factory) {
            hades.remove((Factory)obj);
        }
    }

    public Object detach(final Object obj)
    {
        if (obj instanceof Factory) {
            final Callback callback = ((Factory) obj).getCallback(0);
            return callback instanceof Charon ? ((Charon) callback).detach() : ((Hecate) callback).detach();
        }
        return null;
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

    private int getBlockSize()
    {
        return parseInt(properties.getProperty("blockSize"));
    }
}
