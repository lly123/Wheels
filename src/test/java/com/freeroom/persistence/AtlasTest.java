package com.freeroom.persistence;

import com.freeroom.di.util.Pair;
import com.freeroom.persistence.beans.Book;
import org.junit.Test;

import java.util.List;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

public class AtlasTest
{
    @Test
    public void should_get_primary_key_name()
    {
        assertThat(Atlas.getPrimaryKeyName(Book.class), is("id"));
    }

    @Test
    public void should_get_columns()
    {
        Book book = new Book();
        book.setIsbn(1449344852);
        book.setName("AngularJS");

        List<Pair<String,Object>> columns = Atlas.getColumns(book);

        assertThat(columns.get(0).fst, is("isbn"));
        assertThat((Long)columns.get(0).snd, is(1449344852L));
        assertThat(columns.get(1).fst, is("name"));
        assertThat((String)columns.get(1).snd, is("AngularJS"));
    }
}
