package com.freeroom.persistence;

import com.freeroom.persistence.beans.Book;
import com.freeroom.persistence.beans.Order;
import com.freeroom.persistence.beans.Publisher;
import com.google.common.base.Optional;
import org.junit.Before;
import org.junit.Test;

import java.util.List;

import static com.freeroom.persistence.DBFixture.getDbProperties;
import static com.freeroom.persistence.DBFixture.prepareDB;
import static com.google.common.collect.Lists.newArrayList;
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
    public void should_find_list_of_values()
    {
        final List<Object> books = athena.from(Book.class).find("price=18.39");

        assertThat(books.size(), is(2));
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

        book = (Book)athena.from(Book.class).find(3).get();
        assertThat(book.getIsbn(), is(1449323391L));
        assertThat(book.getName(), is("Testable JavaScript"));
    }

    @Test
    public void should_keep_null()
    {
        Book book = new Book();
        book.setIsbn(1449323391L);
        book.setName(null);
        book.setPrice(18.39);

        athena.persist(book);

        book = (Book)athena.from(Book.class).findOnly("isbn=1449323391").get();
        assertThat(book.getIsbn(), is(1449323391L));
        assertThat(book.getName(), is(nullValue()));
        assertThat(book.getPrice(), is(18.39));
    }

    @Test
    public void should_remove_persisted_book()
    {
        should_keep_null();

        final Book book = (Book)athena.from(Book.class).findOnly("isbn=1449323391").get();

        athena.remove(book);

        book.setName("ABC");
        athena.persist(book);
        assertThat(athena.from(Book.class).findOnly("isbn=1449323391").isPresent(), is(false));
    }

    @Test
    public void should_persist_list_with_removed_obj()
    {
        List<Object> books = athena.from(Book.class).find("price=18.39");

        assertThat(books.size(), is(2));

        books.remove(0);
        athena.persist(books);

        books = athena.from(Book.class).find("price=18.39");
        assertThat(books.size(), is(1));
    }

    @Test
    public void should_persist_list_with_added_obj()
    {
        List<Object> books = athena.from(Book.class).find("price=18.39");

        assertThat(books.size(), is(2));

        Book book = new Book();
        book.setIsbn(1449323391L);
        book.setName("Testable JavaScript");
        book.setPrice(18.39);
        books.add(book);
        athena.persist(books);

        books = athena.from(Book.class).find("price=18.39");
        assertThat(books.size(), is(3));
    }

    @Test
    public void should_persist_list_with_modified_obj()
    {
        List<Object> books = athena.from(Book.class).find("price=18.39");

        assertThat(books.size(), is(2));

        ((Book)books.get(0)).setName("A Book");
        athena.persist(books);

        books = athena.from(Book.class).find("name='A Book'");
        assertThat(books.size(), is(1));
    }

    @Test
    public void should_persist_new_list()
    {
        List<Object> books = newArrayList();

        final Book book1 = new Book();
        book1.setIsbn(1449323391L);
        book1.setName("Testable JavaScript");
        book1.setPrice(19.89);

        final Book book2 = new Book();
        book2.setIsbn(1449343910L);
        book2.setName("Bootstrap");
        book2.setPrice(19.89);

        books.add(book1);
        books.add(book2);

        athena.persist(books);

        books = athena.from(Book.class).find("price=19.89");
        assertThat(books.size(), is(2));
        assertThat(((Book)books.get(0)).getName(), is("Testable JavaScript"));
        assertThat(((Book)books.get(1)).getName(), is("Bootstrap"));
    }

    @Test
    public void should_load_ONE_TO_MANY_relations()
    {
        final Optional<Object> book = athena.from(Book.class).find(1);

        final List<Order> orders = ((Book)book.get()).getOrders();
        assertThat(orders.size(), is(2));
        assertThat(orders.get(0).getAmount(), is(8));
        assertThat(orders.get(0).getMemo(), is("Deliver at work time"));
    }

    @Test
    public void should_load_ONE_TO_ONE_relations()
    {
        final Optional<Object> book = athena.from(Book.class).find(1);

        assertThat(((Book)book.get()).getPublisher().getName(), is("O Reilly"));
    }

    @Test
    public void should_persist_new_book_with_relations()
    {
        Book book = new Book();
        book.setIsbn(1449323391L);
        book.setName("Testable JavaScript");

        final Publisher publisher = new Publisher();
        publisher.setName("O' Reilly");
        book.setPublisher(publisher);

        final Order order = new Order();
        order.setAmount(1);
        order.setMemo("Deliver on time.");
        book.setOrders(newArrayList(order));

        athena.persist(book);

        book = (Book)athena.from(Book.class).find(3).get();
        assertThat(book.getIsbn(), is(1449323391L));
        assertThat(book.getName(), is("Testable JavaScript"));

        assertThat(book.getPublisher().getName(), is("O' Reilly"));
        assertThat(book.getOrders().get(0).getAmount(), is(1));
    }
}
