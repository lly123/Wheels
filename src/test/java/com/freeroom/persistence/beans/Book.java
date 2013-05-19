package com.freeroom.persistence.beans;

import com.freeroom.persistence.annotations.ID;
import com.freeroom.persistence.annotations.Persist;

import java.util.List;

public class Book
{
    @ID
    private long id;

    @Persist
    private long isbn;

    @Persist
    private String name;

    @Persist
    private double price;

    @Persist
    private long publishDate;

    @Persist
    private List<Long> tags;

    @Persist
    private List<Order> orders;

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
}
