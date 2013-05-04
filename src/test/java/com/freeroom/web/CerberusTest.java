package com.freeroom.web;

import com.freeroom.web.beans.Book;
import org.junit.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.is;

public class CerberusTest
{
    @Test
    public void should_parse_key_and_value()
    {
        final Cerberus cerberus = new Cerberus();
        cerberus.add("key=value");

        assertThat((String)cerberus.getValue("key").get(), is("value"));
    }

    @Test
    public void should_add_multiple_key_value_pairs()
    {
        final Cerberus cerberus = new Cerberus();
        cerberus.add("key1=value1");
        cerberus.add("key2=value2");

        assertThat((String)cerberus.getValue("key1").get(), is("value1"));
        assertThat((String)cerberus.getValue("key2").get(), is("value2"));
    }

    @Test
    public void should_parse_nested_key_and_value()
    {
        final Cerberus cerberus = new Cerberus();
        cerberus.add("key_nestedKey=value");

        assertThat(cerberus.getValue("key").get(), is(instanceOf(Cerberus.class)));
        assertThat((String)((Cerberus)cerberus.getValue("key").get()).getValue("nestedKey").get(), is("value"));
    }

    @Test
    public void should_parse_nested_multiple_key_values()
    {
        final Cerberus cerberus = new Cerberus();
        cerberus.add("key_nestedKey=value");
        cerberus.add("key_nestedKey2=value2");
        cerberus.add("key3=value3");

        assertThat((String)cerberus.getValue("key3").get(), is("value3"));
        assertThat(cerberus.getValue("key").get(), is(instanceOf(Cerberus.class)));

        final Cerberus nested = (Cerberus)cerberus.getValue("key").get();
        assertThat((String)nested.getValue("nestedKey").get(), is("value"));
        assertThat((String)nested.getValue("nestedKey2").get(), is("value2"));
    }

    @Test
    public void should_fill_out_string_field()
    {
        final Cerberus cerberus = new Cerberus();
        cerberus.add("name=AngularJS");

        final Book book = (Book)cerberus.fill(Book.class);

        assertThat(book.getName(), is("AngularJS"));
    }

    @Test
    public void should_fill_out_integer_field()
    {
        final Cerberus cerberus = new Cerberus();
        cerberus.add("pageNumber=568");

        final Book book = (Book)cerberus.fill(Book.class);

        assertThat(book.getPageNumber(), is(568));
    }
}
