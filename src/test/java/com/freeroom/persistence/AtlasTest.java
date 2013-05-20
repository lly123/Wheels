package com.freeroom.persistence;

import com.freeroom.di.util.Pair;
import com.freeroom.persistence.beans.Book;
import com.freeroom.persistence.beans.Publisher;
import org.junit.Test;

import java.lang.reflect.Field;
import java.util.List;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

public class AtlasTest
{
    @Test
    public void should_get_primary_key_name()
    {
        assertThat(Atlas.getPrimaryKeyName(Book.class), is("bookid"));
    }

    @Test
    public void should_get_basic_fields()
    {
        final List<Field> fields = Atlas.getBasicFields(Book.class);

        assertThat(fields.get(0).getName(), is("isbn"));
        assertThat(fields.get(1).getName(), is("name"));
    }

    @Test
    public void should_get_basic_fields_and_values()
    {
        final Book book = new Book();
        book.setIsbn(1449344852);
        book.setName("AngularJS");

        final List<Pair<Field,Object>> fields = Atlas.getBasicFieldAndValues(book);

        assertThat(fields.get(0).fst.getName(), is("isbn"));
        assertThat((Long)fields.get(0).snd, is(1449344852L));
        assertThat(fields.get(1).fst.getName(), is("name"));
        assertThat((String)fields.get(1).snd, is("AngularJS"));
    }

    @Test
    public void should_get_ONE_TO_MANY_relations()
    {
        final List<Pair<Field, Class>> relations = Atlas.getOneToManyRelations(Book.class);

        assertThat(relations.size(), is(1));
        assertThat(relations.get(0).snd.getSimpleName(), is("Order"));
    }

    @Test
    public void should_get_ONE_TO_ONE_relations()
    {
        final List<Field> relations = Atlas.getOneToOneRelations(Book.class);

        assertThat(relations.size(), is(1));
        assertThat(relations.get(0).getType().equals(Publisher.class), is(true));
    }
}
