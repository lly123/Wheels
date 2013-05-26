package com.freeroom.web.example.beans;

import com.freeroom.persistence.annotations.ID;
import com.freeroom.persistence.annotations.Persist;

public class Tag
{
    @ID
    private long id;

    @Persist
    private String label;

    @Persist
    private String text;

    public String getLabel()
    {
        return label;
    }
}
