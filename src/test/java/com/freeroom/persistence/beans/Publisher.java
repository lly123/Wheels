package com.freeroom.persistence.beans;

import com.freeroom.persistence.annotations.ID;
import com.freeroom.persistence.annotations.Persist;
import com.freeroom.persistence.proxy.IdPurpose;

public class Publisher
{
    @ID
    private long publisherid;

    @Persist
    private String name;

    @Persist
    private String profile;

    private IdPurpose idPurpose;

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

    public void setPublisherid(final long publisherid)
    {
        this.publisherid = publisherid;
    }

    public void setIdPurpose(final IdPurpose idPurpose)
    {
        this.idPurpose = idPurpose;
    }
}
