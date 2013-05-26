package com.freeroom.web.example.beans;

import com.freeroom.persistence.annotations.ID;
import com.freeroom.persistence.annotations.Persist;
import com.freeroom.persistence.proxy.IdPurpose;

public class Order
{
    @ID
    private long id;

    @Persist
    private String name;

    @Persist
    private String phone;

    @Persist
    private String address;

    private IdPurpose idPurpose;

    public String getName()
    {
        return name;
    }
}
