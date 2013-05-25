package com.freeroom.web.example;

import com.freeroom.web.Apollo;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;

import java.sql.*;
import java.util.Calendar;
import java.util.GregorianCalendar;

import static org.eclipse.jetty.servlet.ServletContextHandler.SESSIONS;

public class TestServer
{
    public static void main(final String[] args) throws Exception
    {
        prepareDB();

        final Server server = new Server(8080);
        final ServletContextHandler context = new ServletContextHandler(SESSIONS);
        context.setContextPath("/");
        server.setHandler(context);

        context.addServlet(new ServletHolder(new Apollo("com.freeroom.web.example.beans")), "/*");

        server.start();
        server.join();
    }

    public static void prepareDB() throws SQLException
    {
        try(final Connection connection = DriverManager.getConnection("jdbc:hsqldb:mem:bookdb", "sa", "")) {
            final Statement statement = connection.createStatement();
            try {
                statement.executeUpdate("DROP TABLE PUBLIC.BOOK");
                statement.executeUpdate("DROP TABLE PUBLIC.PUBLISHER");
            } catch (Exception ignored) {}

            statement.executeUpdate(
                    "CREATE MEMORY TABLE PUBLIC.BOOK("+
                            "ID INTEGER GENERATED BY DEFAULT AS IDENTITY(START WITH 1) NOT NULL PRIMARY KEY, "+
                            "ISBN INTEGER, NAME VARCHAR(50), PRICE DECIMAL(8,2), "+
                            "PUBLISHDATE DECIMAL(15,0), TAGS VARCHAR(255))");

            addBook(connection, 1449344852, "AngularJS", 13.83, getMilliseconds(2013, 4, 30), "101,102,103");
            addBook(connection, 1449343910, "Bootstrap", 13.83, getMilliseconds(2013, 5, 29), "101");
            addBook(connection, 1449360726, "Functional JavaScript", 15.81, getMilliseconds(2013, 6, 18), "101,104");
            addBook(connection, 1449323391, "Testable JavaScript", 19.98, getMilliseconds(2013, 1, 31), "104");
            addBook(connection, 1449323073, "Learning Node", 20.98, getMilliseconds(2012, 10, 10), "101,102,103,104");

            statement.executeUpdate(
                    "CREATE MEMORY TABLE PUBLIC.PUBLISHER("+
                            "ID INTEGER GENERATED BY DEFAULT AS IDENTITY(START WITH 1) NOT NULL PRIMARY KEY, "+
                            "NAME VARCHAR(50), PROFILE VARCHAR(255))");

            addPublisher(connection, "O' Reilly", "A Publisher");
            addPublisher(connection, "Wrox", "Programmer To Programmer");

            statement.close();
        }
    }

    private static void addBook(final Connection connection, final int isbn, final String name,
                                final double price, final long publishDate, final String tags) throws SQLException
    {
        PreparedStatement preparedStatement = connection.prepareStatement(
                "INSERT INTO PUBLIC.BOOK (ISBN, NAME, PRICE, PUBLISHDATE, TAGS) VALUES (?, ?, ?, ?, ?)");
        preparedStatement.setLong(1, isbn);
        preparedStatement.setString(2, name);
        preparedStatement.setDouble(3, price);
        preparedStatement.setLong(4, publishDate);
        preparedStatement.setString(5, tags);
        preparedStatement.executeUpdate();
        preparedStatement.close();
    }

    private static void addPublisher(final Connection connection,
                                     final String publisherName, final String profile) throws SQLException
    {
        final PreparedStatement preparedStatement = connection.prepareStatement(
                "INSERT INTO PUBLIC.PUBLISHER (NAME, PROFILE) VALUES (?, ?)");
        preparedStatement.setString(1, publisherName);
        preparedStatement.setString(2, profile);
        preparedStatement.executeUpdate();
        preparedStatement.close();
    }

    private static long getMilliseconds(final int year, final int month, final int day)
    {
        final Calendar calendar = GregorianCalendar.getInstance();
        calendar.set(year, month - 1, day);
        return calendar.getTime().getTime();
    }
}
