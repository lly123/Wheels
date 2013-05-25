package com.freeroom.web.example.beans;

import com.freeroom.persistence.annotations.ID;
import com.freeroom.persistence.annotations.Persist;
import com.freeroom.persistence.proxy.IdPurpose;

public class Publisher
{
    @ID
    private long id;

    @Persist
    private String name;

    @Persist
    private String profile;

    private IdPurpose idPurpose;

    public String getName()
    {
        return name;
    }
}
