package com.freeroom.web.example.beans;

public class Book
{
    private long isbn;
    private String name;
    private double price;
    private long publishDate;

    public Book(final long isbn, final String name, final double price, final long publishDate)
    {
        this.isbn = isbn;
        this.name = name;
        this.price = price;
        this.publishDate = publishDate;
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
}
