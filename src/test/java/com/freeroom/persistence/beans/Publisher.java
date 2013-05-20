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

    public String getName()
    {
        return name;
    }

    public void setName(final String name)
    {
        this.name = name;
    }

    public String getProfile()
    {
        return profile;
    }

    public void setProfile(final String profile)
    {
        this.profile = profile;
    }
}
