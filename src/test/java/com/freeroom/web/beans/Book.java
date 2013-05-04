package com.freeroom.web.beans;

public class Book
{
    private String name;
    private int pageNumber;
    private boolean imported;
    private Order order;

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
}
