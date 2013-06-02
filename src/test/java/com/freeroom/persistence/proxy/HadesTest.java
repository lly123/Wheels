package com.freeroom.persistence.proxy;

import com.freeroom.util.Pair;
import com.freeroom.persistence.beans.Book;
import com.freeroom.persistence.beans.Order;
import net.sf.cglib.proxy.Factory;
import org.junit.Before;
import org.junit.Test;

import java.sql.SQLException;
import java.util.List;

import static com.freeroom.persistence.DBFixture.getDbProperties;
import static com.freeroom.persistence.DBFixture.prepareDB;
import static com.google.common.base.Optional.of;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

public class HadesTest
{
    private static Hades hades;

    @Before
    public void setUp() throws SQLException
    {
        prepareDB();
        hades = new Hades(getDbProperties());
    }

    @Test
    public void should_get_simple_record()
    {
        final Book book = (Book)hades.create(Book.class, 1L, 1);

        assertThat(book.getIsbn(), is(123L));
        assertThat(book.getName(), is("JBoss Seam"));
        assertThat(book.getPrice(), is(18.39));
        assertThat(book.getPublishDate(), is(1234567890L));
        assertThat(book.getTags().get(0), is(101L));
        assertThat(book.getTags().get(1), is(102L));
    }

    @Test
    public void should_know_simple_record_has_been_changed()
    {
        final Book book = (Book)hades.create(Book.class, 1L, 1);
        book.setName("Learning Node");

        assertThat(hades.isDirty(book), is(true));
    }

    @Test
    public void should_load_records()
    {
        final List<Object> orders = hades.createList(Order.class,
                "SELECT orderid FROM order WHERE book_bookid=?", of(1L), 1);
        assertThat(orders.size(), is(2));
        assertThat(((Order)orders.get(0)).getAmount(), is(8));
        assertThat(((Order)orders.get(0)).getMemo(), is("Deliver at work time"));
    }

    @Test
    public void should_check_removed_obj_from_list()
    {
        final List<Object> orders = hades.createList(Order.class,
                "SELECT orderid FROM order WHERE book_bookid=?", of(1L), 1);

        orders.remove(0);

        assertThat(hades.isDirtyList(orders), is(true));
    }

    @Test
    public void should_check_added_obj_in_list()
    {
        final List<Object> orders = hades.createList(Order.class,
                "SELECT orderid FROM order WHERE book_bookid=?", of(1L), 1);

        final Order order = new Order();
        order.setAmount(1);
        orders.add(order);

        assertThat(hades.isDirtyList(orders), is(true));
    }

    @Test
    public void should_check_modified_obj_in_list()
    {
        final List<Object> orders = hades.createList(Order.class,
                "SELECT orderid FROM order WHERE book_bookid=?", of(1L), 1);

        final Order book = (Order)orders.get(0);
        book.setAmount(2);

        assertThat(hades.isDirtyList(orders), is(true));
    }

    @Test
    public void should_batch_load()
    {
        hades.persistNew(new Order(), of(Pair.of("book_bookid", 1L)));
        hades.persistNew(new Order(), of(Pair.of("book_bookid", 1L)));
        hades.persistNew(new Order(), of(Pair.of("book_bookid", 1L)));

        final List<Object> orders = hades.createList(Order.class,
                "SELECT orderid FROM order WHERE book_bookid=?", of(1L), 2);
        assertThat(orders.size(), is(5));

        ((Order)orders.get(2)).getAmount();
        assertThat(((Charon) ((Factory) orders.get(2)).getCallback(0)).isNotLoaded(), is(false));
        assertThat(((Charon)((Factory)orders.get(3)).getCallback(0)).isNotLoaded(), is(false));

        ((Order)orders.get(3)).getAmount();
        assertThat(((Charon)((Factory)orders.get(4)).getCallback(0)).isNotLoaded(), is(false));
    }
}
