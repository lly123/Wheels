package com.freeroom.web.beans;

import java.util.List;

public class Book
{
    private String name;
    private int pageNumber;
    private boolean imported;
    private Address address;
    private Binding binding;
    private List<String> authors;
    private List<Integer> tagIds;
    private List<Order> orders;

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

    public Address getAddress()
    {
        return address;
    }

    public List<Order> getOrders()
    {
        return orders;
    }

    public List<String> getAuthors()
    {
        return authors;
    }

    public List<Integer> getTagIds()
    {
        return tagIds;
    }

    public Binding getBinding()
    {
        return binding;
    }
}
