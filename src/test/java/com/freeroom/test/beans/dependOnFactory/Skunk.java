package com.freeroom.test.beans.dependOnFactory;

import com.freeroom.di.annotations.Bean;
import com.freeroom.di.annotations.Inject;
import com.freeroom.test.beans.beanFactory.Dove;
import com.freeroom.test.beans.beanFactory.Pheasant;
import com.freeroom.test.beans.beanFactory.Toad;

@Bean
public class Skunk
{
    private final Dove dove;

    @Inject
    private Pheasant pheasant;

    private Toad toad;

    @Inject
    public Skunk(final Dove dove)
    {
        this.dove = dove;
    }

    @Inject
    public void setToad(final Toad toad)
    {
        this.toad = toad;
    }

    public Dove getDove()
    {
        return dove;
    }

    public Pheasant getPheasant()
    {
        return pheasant;
    }

    public Toad getToad()
    {
        return toad;
    }
}
