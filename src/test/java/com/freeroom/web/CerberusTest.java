package com.freeroom.web;

import com.freeroom.web.beans.Address;
import com.freeroom.web.beans.Book;
import com.freeroom.web.beans.Order;
import org.junit.Test;

import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.is;

public class CerberusTest
{
    @Test
    public void should_parse_key_and_value()
    {
        final Cerberus cerberus = new Cerberus("UTF-8");
        cerberus.add("key=value");

        assertThat((String)cerberus.getValue("key").get(), is("value"));
    }

    @Test
    public void should_add_multiple_key_value_pairs()
    {
        final Cerberus cerberus = new Cerberus("UTF-8");
        cerberus.add("key1=value1");
        cerberus.add("key2=value2");

        assertThat((String)cerberus.getValue("key1").get(), is("value1"));
        assertThat((String)cerberus.getValue("key2").get(), is("value2"));
    }

    @Test
    public void should_parse_nested_key_and_value()
    {
        final Cerberus cerberus = new Cerberus("UTF-8");
        cerberus.add("key_nestedKey=value");

        assertThat(cerberus.getValue("key").get(), is(instanceOf(Cerberus.class)));
        assertThat((String)((Cerberus)cerberus.getValue("key").get()).getValue("nestedKey").get(), is("value"));
    }

    @Test
    public void should_parse_nested_multiple_key_values()
    {
        final Cerberus cerberus = new Cerberus("UTF-8");
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
    public void should_fill_out_fields()
    {
        final Cerberus cerberus = new Cerberus("UTF-8");
        cerberus.add("name=AngularJS");
        cerberus.add("pageNumber=568");
        cerberus.add("imported=true");

        final Book book = (Book)cerberus.fill(Book.class);

        assertThat(book.getName(), is("AngularJS"));
        assertThat(book.getPageNumber(), is(568));
        assertThat(book.isImported(), is(true));
    }

    @Test
    public void should_fill_out_nested_fields()
    {
        final Cerberus cerberus = new Cerberus("UTF-8");
        cerberus.add("address_city=Beijing");
        cerberus.add("address_street=DongZhiMen");
        cerberus.add("address_zipCode=123456");

        final Book book = (Book)cerberus.fill(Book.class);

        assertThat(book.getAddress(), is(instanceOf(Address.class)));
        final Address address = book.getAddress();
        assertThat(address.getCity(), is("Beijing"));
        assertThat(address.getStreet(), is("DongZhiMen"));
        assertThat(address.getZipCode(), is(123456));
    }

    @Test
    public void should_fill_out_array()
    {
        final Cerberus cerberus = new Cerberus("UTF-8");
        cerberus.add("key[20]=value2");
        cerberus.add("key[10]=value1");
        cerberus.add("key[30]=value3");

        assertThat(cerberus.getValue("key").get(), is(instanceOf(List.class)));
        final List<String> value = (List<String>)cerberus.getValue("key").get();
        assertThat(value.get(0), is("value1"));
        assertThat(value.get(1), is("value2"));
        assertThat(value.get(2), is("value3"));
    }

    @Test
    public void should_fill_out_nested_array()
    {
        final Cerberus cerberus = new Cerberus("UTF-8");
        cerberus.add("authors[1]=Michael");
        cerberus.add("authors[2]=David");
        cerberus.add("authors[3]=Richard");

        final Book book = (Book)cerberus.fill(Book.class);

        assertThat(book.getAuthors().get(0), is("Michael"));
        assertThat(book.getAuthors().get(1), is("David"));
        assertThat(book.getAuthors().get(2), is("Richard"));
    }

    @Test
    public void should_fill_out_nested_Integer_array()
    {
        final Cerberus cerberus = new Cerberus("UTF-8");
        cerberus.add("tagIds[1]=101");
        cerberus.add("tagIds[2]=218");
        cerberus.add("tagIds[3]=585");

        final Book book = (Book)cerberus.fill(Book.class);

        assertThat(book.getTagIds().get(0), is(101));
        assertThat(book.getTagIds().get(1), is(218));
        assertThat(book.getTagIds().get(2), is(585));
    }
}
