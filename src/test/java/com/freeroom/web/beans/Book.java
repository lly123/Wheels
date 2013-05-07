package com.freeroom.web.beans;

import java.util.List;

public class Book
{
    private String name;
    private int pageNumber;
    private boolean imported;
    private Order order;
    private List<String> authors;
    private List<Integer> tagIds;

    public String getName()
    {
        return name;
    }

    public int getPageNumber()
    {
        return pageNumber;
    }

    public boolean isImported()
    {
        return imported;
    }

    public Order getOrder()
    {
        return order;
    }

    public List<String> getAuthors()
    {
        return authors;
    }

    public List<Integer> getTagIds()
    {
        return tagIds;
    }
}
