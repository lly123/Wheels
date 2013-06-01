package com.freeroom.persistence.beans;

import com.freeroom.persistence.annotations.ID;
import com.freeroom.persistence.annotations.Persist;
import com.freeroom.persistence.proxy.IdPurpose;

import java.util.List;

public class Book
{
    @ID
    private long bookid;

    @Persist
    private long isbn;

    @Persist
    private String name;

    @Persist
    private double price;

    @Persist
    private long publishDate;

    @Persist
    private Binding binding;

    @Persist
    private List<Long> tags;

    @Persist(foreignKey = true)
    private Publisher publisher;

    @Persist
    private List<Order> orders;

    @Persist
    private Reader reader;

    private IdPurpose idPurpose;

    public void setBookid(final long bookid)
    {
        this.bookid = bookid;
    }

    public long getIsbn()
    {
        return isbn;
    }

    public String getName()
    {
        return name;
    }

    public void setIsbn(final long isbn)
    {
        this.isbn = isbn;
    }

    public void setName(final String name)
    {
        this.name = name;
    }

    public double getPrice()
    {
        return price;
    }

    public void setPrice(final double price)
    {
        this.price = price;
    }

    public long getPublishDate()
    {
        return publishDate;
    }

    public void setPublishDate(final long publishDate)
    {
        this.publishDate = publishDate;
    }

    public List<Long> getTags()
    {
        return tags;
    }

    public void setTags(final List<Long> tags)
    {
        this.tags = tags;
    }

    public List<Order> getOrders()
    {
        return orders;
    }

    public void setOrders(final List<Order> orders)
    {
        this.orders = orders;
    }

    public Publisher getPublisher()
    {
        return publisher;
    }

    public void setPublisher(final Publisher publisher)
    {
        this.publisher = publisher;
    }

    public Reader getReader()
    {
        return reader;
    }

    public void setReader(final Reader reader)
    {
        this.reader = reader;
    }

    public void setIdPurpose(final IdPurpose idPurpose)
    {
        this.idPurpose = idPurpose;
    }

    public Binding getBinding()
    {
        return binding;
    }

    public void setBinding(final Binding binding)
    {
        this.binding = binding;
    }
}
