package com.freeroom.persistence;

import com.freeroom.persistence.beans.Book;
import org.junit.Before;
import org.junit.Test;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Properties;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;

public class AthenaTest
{
    private Athena athena;

    @Before
    public void setUp() throws Exception
    {
        prepareDB();
        athena = new Athena(getDbProperties());
    }

    @Test
    public void should_save_and_select_by_id()
    {
        Book book = new Book();
        book.setIsbn(1449344852L);
        book.setName("AngularJS");
        athena.save(book);

        book = (Book) athena.from(Book.class).find(1).get();
        assertThat(book.getIsbn(), is(1449344852L));
        assertThat(book.getName(), is("AngularJS"));
    }

    private Properties getDbProperties()
    {
        Properties properties = new Properties();
        properties.setProperty("url", "jdbc:hsqldb:mem:mydb");
        properties.setProperty("username", "sa");
        properties.setProperty("password", "");
        return properties;
    }

    private void prepareDB() throws SQLException
    {
        try(Connection connection = DriverManager.getConnection("jdbc:hsqldb:mem:mydb", "sa", "")) {
            Statement statement = connection.createStatement();
            statement.executeUpdate("CREATE MEMORY TABLE PUBLIC.BOOK(ID INTEGER GENERATED BY DEFAULT AS IDENTITY(START WITH 1) NOT NULL PRIMARY KEY,ISBN INTEGER,NAME VARCHAR(50))");
            statement.close();
        }
    }
}
