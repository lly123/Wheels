package com.freeroom.persistence;

import com.freeroom.persistence.beans.Book;
import com.freeroom.persistence.beans.Order;
import com.freeroom.persistence.beans.Publisher;
import com.freeroom.persistence.beans.Reader;
import com.google.common.base.Optional;
import org.hamcrest.CoreMatchers;
import org.junit.Before;
import org.junit.Test;

import java.util.List;

import static com.freeroom.persistence.DBFixture.getDbProperties;
import static com.freeroom.persistence.DBFixture.prepareDB;
import static com.freeroom.persistence.proxy.IdPurpose.Locate;
import static com.freeroom.persistence.proxy.IdPurpose.Remove;
import static com.freeroom.persistence.proxy.IdPurpose.Update;
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
    public void should_cascading_remove_persisted_book()
    {
        final Optional<Object> book = athena.from(Book.class).find(1L);

        athena.remove(book.get());

        assertThat(athena.from(Book.class).find(1L).isPresent(), is(false));
        assertThat(athena.from(Publisher.class).all().size(), is(0));
        assertThat(athena.from(Reader.class).all().size(), is(0));
        assertThat(athena.from(Order.class).all().size(), is(0));
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

    @Test
    public void should_persist_existed_book_with_new_relations()
    {
        Book book = (Book)athena.from(Book.class).find(1).get();
        book.setPrice(19.88);

        athena.remove(book.getPublisher());
        final Publisher publisher = new Publisher();
        publisher.setName("O' Reilly");
        book.setPublisher(publisher);

        final Order order = new Order();
        order.setAmount(1);
        order.setMemo("Deliver on time.");
        book.getOrders().add(order);

        athena.persist(book);

        book = (Book)athena.from(Book.class).find(1).get();
        assertThat(book.getPrice(), is(19.88));

        assertThat(book.getPublisher().getName(), is("O' Reilly"));
        assertThat(book.getOrders().size(), is(3));
    }

    @Test
    public void should_get_absent_given_object_is_not_loaded()
    {
        final Book book = (Book)athena.from(Book.class).find(1).get();
        final Book detachedBook = (Book)athena.detach(book);

        assertThat(detachedBook, CoreMatchers.is(nullValue()));
    }

    @Test
    public void should_get_detached_object_given_no_relations()
    {
        final Book book = (Book)athena.from(Book.class).find(1).get();
        book.getName();
        final Book detachedBook = (Book)athena.detach(book);

        assertThat(detachedBook.getName(), CoreMatchers.is("JBoss Seam"));
        assertThat(detachedBook.getPrice(), CoreMatchers.is(18.39));
    }


    @Test
    public void should_get_detached_object_with_ONE_TO_ONE_relation()
    {
        final Book book = (Book)athena.from(Book.class).find(1).get();
        book.getName();
        book.getPublisher().getName();
        final Book detachedBook = (Book)athena.detach(book);

        assertThat(detachedBook.getName(), CoreMatchers.is("JBoss Seam"));
        assertThat(detachedBook.getPublisher().getName(), CoreMatchers.is("O Reilly"));
    }

    @Test
    public void should_get_detached_object_with_ONE_TO_MANY_relation()
    {
        final Book book = (Book)athena.from(Book.class).find(1).get();
        book.getName();
        book.getOrders().get(0);
        final Book detachedBook = (Book)athena.detach(book);

        assertThat(detachedBook.getName(), CoreMatchers.is("JBoss Seam"));
        assertThat(detachedBook.getOrders().get(0).getMemo(), CoreMatchers.is("Deliver at work time"));
    }

    @Test
    public void should_persist_book_with_id()
    {
        Book book = new Book();
        book.setBookid(1L);
        book.setIsbn(1449323399L);
        book.setName("A Book");
        book.setIdPurpose(Update);

        athena.persist(book);

        book = (Book)athena.from(Book.class).find(1).get();
        assertThat(book.getIsbn(), is(1449323399L));
        assertThat(book.getName(), is("A Book"));
    }

    @Test
    public void should_remove_book_with_id()
    {
        Book book = new Book();
        book.setBookid(1L);
        book.setIsbn(1449323399L);
        book.setName("A Book");
        book.setIdPurpose(Remove);

        athena.persist(book);

        final Optional<Object> object = athena.from(Book.class).find(1);
        assertThat(object.isPresent(), is(false));
    }

    @Test
    public void should_persist_book_with_id_and_ONE_TO_ONE_update_relation()
    {
        Book book = new Book();
        book.setBookid(1L);
        book.setIdPurpose(Locate);

        final Publisher publisher = new Publisher();
        publisher.setPublisherid(1L);
        publisher.setName("A Publisher");
        publisher.setIdPurpose(Update);
        book.setPublisher(publisher);

        athena.persist(book);

        book = (Book)athena.from(Book.class).find(1).get();
        assertThat(book.getIsbn(), is(123L));
        assertThat(book.getPublisher().getName(), is("A Publisher"));
    }

    @Test
    public void should_persist_book_with_id_and_ONE_TO_ONE_remove_relation()
    {
        Book book = new Book();
        book.setBookid(1L);
        book.setIsbn(456L);
        book.setIdPurpose(Update);

        final Publisher publisher = new Publisher();
        publisher.setPublisherid(1L);
        publisher.setIdPurpose(Remove);
        book.setPublisher(publisher);

        athena.persist(book);

        book = (Book)athena.from(Book.class).find(1).get();
        assertThat(book.getIsbn(), is(456L));
        assertThat(book.getPublisher(), is(nullValue()));
    }

    @Test
    public void should_persist_book_with_id_and_ONE_TO_MANY_add_relation()
    {
        Book book = new Book();
        book.setBookid(1L);
        book.setIsbn(456L);
        book.setIdPurpose(Update);

        final Order order1 = new Order();
        order1.setAmount(9);
        order1.setMemo("order1 Memo");

        final Order order2 = new Order();
        order2.setOrderid(1L);
        order2.setAmount(3);
        order2.setIdPurpose(Update);
        order2.setMemo("order2 Memo");

        book.setOrders(newArrayList(order1, order2));

        athena.persist(book);

        book = (Book)athena.from(Book.class).find(1).get();
        assertThat(book.getIsbn(), is(456L));
        assertThat(book.getOrders().size(), is(3));
        assertThat(book.getOrders().get(0).getMemo(), is("order2 Memo"));
        assertThat(book.getOrders().get(2).getMemo(), is("order1 Memo"));
    }
}
