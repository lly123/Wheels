package com.freeroom.persistence;

import com.freeroom.persistence.beans.Book;
import org.junit.Before;
import org.junit.Test;

import static com.freeroom.persistence.DBFixture.getDbProperties;
import static com.freeroom.persistence.DBFixture.prepareDB;
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
        Book book = (Book)athena.from(Book.class).find(1).get();
        assertThat(book.getIsbn(), is(123L));
        assertThat(book.getName(), is("JBoss Seam"));
    }
}
