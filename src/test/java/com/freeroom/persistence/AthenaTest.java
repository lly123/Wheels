package com.freeroom.persistence;

import com.freeroom.persistence.beans.Book;
import org.junit.Before;
import org.junit.Test;

import static com.freeroom.persistence.DBFixture.getDbProperties;
import static com.freeroom.persistence.DBFixture.prepareDB;
import static org.hamcrest.CoreMatchers.nullValue;
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
    public void should_select_by_id()
    {
        final Book book = (Book)athena.from(Book.class).find(1).get();

        assertThat(book.getIsbn(), is(123L));
        assertThat(book.getName(), is("JBoss Seam"));
    }
    
    @Test
    public void should_persist_existed_book()
    {
        Book book = (Book)athena.from(Book.class).find(1).get();
        book.setIsbn(1449323073L);
        book.setName("Learning Node");

        athena.persist(book);

        book = (Book)athena.from(Book.class).find(1).get();
        assertThat(book.getIsbn(), is(1449323073L));
        assertThat(book.getName(), is("Learning Node"));
    }

    @Test
    public void should_persist_new_book()
    {
        Book book = new Book();
        book.setIsbn(1449323391L);
        book.setName("Testable JavaScript");

        athena.persist(book);

        book = (Book)athena.from(Book.class).find(2).get();
        assertThat(book.getIsbn(), is(1449323391L));
        assertThat(book.getName(), is("Testable JavaScript"));
    }

    @Test
    public void should_keep_null()
    {
        Book book = new Book();
        book.setIsbn(1449323391L);
        book.setName(null);

        athena.persist(book);

        book = (Book)athena.from(Book.class).findOnly("isbn=1449323391").get();
        assertThat(book.getIsbn(), is(1449323391L));
        assertThat(book.getName(), is(nullValue()));
    }
}