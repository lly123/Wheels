package com.freeroom.persistence;

import java.sql.*;

import static java.lang.String.format;

// java -cp ./hsqldb.jar org.hsqldb.Server -database.0 mydb -dbname.0 mydb
// java -cp hsqldb.jar org.hsqldb.util.DatabaseManagerSwing
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

    public Object find(final int key)
    {
        String sql = format("SELECT * FROM %s WHERE %s", clazz.getSimpleName(), Atlas.getPrimaryKeyName(clazz) + "=" + key);

        try {
            Class.forName("org.hsqldb.jdbcDriver" );
            Connection connection = DriverManager.getConnection("jdbc:hsqldb:hsql://localhost/mydb", "sa", "");
            Statement statement = connection.createStatement();
            ResultSet resultSet = statement.executeQuery(sql);
            resultSet.close();
            statement.close();
            connection.close();
        } catch (Exception e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }
        return null;
    }

    public static void save(final Object book)
    {
    }
}
