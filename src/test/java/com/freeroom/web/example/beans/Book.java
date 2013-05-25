package com.freeroom.web.example.beans;

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

    @Persist(foreignKey = true)
    private Publisher publisher;

    public long getIsbn()
    {
        return isbn;
    }
}
