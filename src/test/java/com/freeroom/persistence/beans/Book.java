package com.freeroom.persistence.beans;

import com.freeroom.persistence.annotations.Column;
import com.freeroom.persistence.annotations.ID;

public class Book
{
    @ID
    private int id;

    @Column
    private long isbn;

    @Column
    private String name;

    public long getIsbn() {
        return isbn;
    }

    public String getName() {
        return name;
    }

    public void setIsbn(final long isbn) {
        this.isbn = isbn;
    }

    public void setName(final String name) {
        this.name = name;
    }
}
