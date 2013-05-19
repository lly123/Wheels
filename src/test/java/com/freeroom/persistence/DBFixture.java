package com.freeroom.persistence;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Properties;

public class DBFixture
{
    public static Properties getDbProperties()
    {
        final Properties properties = new Properties();
        properties.setProperty("url", "jdbc:hsqldb:mem:mydb");
        properties.setProperty("username", "sa");
        properties.setProperty("password", "");
        return properties;
    }

    public static void prepareDB() throws SQLException
    {
        try(final Connection connection = DriverManager.getConnection("jdbc:hsqldb:mem:mydb", "sa", "")) {
            final Statement statement = connection.createStatement();
            try {
                statement.executeUpdate("DROP TABLE PUBLIC.BOOK");
                statement.executeUpdate("DROP TABLE PUBLIC.ORDER");
            } catch (Exception ignored) {}

            statement.executeUpdate(
                    "CREATE MEMORY TABLE PUBLIC.BOOK("+
                            "ID INTEGER GENERATED BY DEFAULT AS IDENTITY(START WITH 1) NOT NULL PRIMARY KEY, "+
                            "ISBN INTEGER, NAME VARCHAR(50), PRICE DECIMAL(8,2), "+
                            "PUBLISHDATE DECIMAL(12,0), TAGS VARCHAR(255))");
            statement.executeUpdate(
                    "INSERT INTO PUBLIC.BOOK (ISBN, NAME, PRICE, PUBLISHDATE, TAGS) "+
                    "VALUES (123, 'JBoss Seam', 18.39, 1234567890, '101,102')");

            statement.executeUpdate(
                    "CREATE MEMORY TABLE PUBLIC.ORDER("+
                            "ID INTEGER GENERATED BY DEFAULT AS IDENTITY(START WITH 1) NOT NULL PRIMARY KEY, "+
                            "BOOK_ID INTEGER, AMOUNT INTEGER, MEMO VARCHAR(255))");
            statement.executeUpdate(
                    "INSERT INTO PUBLIC.ORDER (BOOK_ID, AMOUNT, MEMO) "+
                    "VALUES (1, 8, 'Deliver at work time')");

            statement.close();
        }
    }
}
