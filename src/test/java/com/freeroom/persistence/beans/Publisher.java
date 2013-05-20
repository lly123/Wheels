package com.freeroom.persistence.beans;

import com.freeroom.persistence.annotations.ID;
import com.freeroom.persistence.annotations.Persist;

public class Publisher
{
    @ID
    private long id;

    @Persist
    private String name;

    @Persist
    private String profile;
}
