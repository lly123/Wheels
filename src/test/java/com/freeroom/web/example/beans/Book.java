package com.freeroom.web.example.beans;

import java.util.List;

public class Book
{
    private long isbn;
    private String name;
    private double price;
    private long publishDate;
    private List<Integer> tags;

    public Book()
    {
    }

    public Book(final long isbn, final String name, final double price,
                final long publishDate, List<Integer> tags)
    {
        this.isbn = isbn;
        this.name = name;
        this.price = price;
        this.publishDate = publishDate;
        this.tags = tags;
    }

    public String getName()
    {
        return name;
    }

    public double getPrice()
    {
        return price;
    }

    public long getPublishDate()
    {
        return publishDate;
    }

    public long getIsbn()
    {
        return isbn;
    }

    public List<Integer> getTags()
    {
        return tags;
    }
}
