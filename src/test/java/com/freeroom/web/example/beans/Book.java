package com.freeroom.web.example.beans;

import com.freeroom.persistence.annotations.ID;
import com.freeroom.persistence.annotations.Persist;
import com.freeroom.persistence.proxy.IdPurpose;

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
    private Binding binding;

    @Persist
    private List<Long> tags;

    @Persist(foreignKey = true)
    private Publisher publisher;

    @Persist
    private List<Order> orders;

    private IdPurpose idPurpose;

    public long getIsbn()
    {
        return isbn;
    }

    public Publisher getPublisher()
    {
        return publisher;
    }

    public List<Order> getOrders()
    {
        return orders;
    }
}
