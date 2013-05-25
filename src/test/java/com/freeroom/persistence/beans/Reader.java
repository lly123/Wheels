package com.freeroom.persistence.beans;

import com.freeroom.persistence.annotations.ID;
import com.freeroom.persistence.annotations.Persist;

public class Reader
{
    @ID
    private long readerid;

    @Persist
    private String name;
}
