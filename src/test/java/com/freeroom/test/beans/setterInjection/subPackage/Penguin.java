package com.freeroom.test.beans.setterInjection.subPackage;

import com.freeroom.di.annotations.Bean;
import com.freeroom.di.annotations.Inject;

@Bean
public class Penguin
{
    private Raccoon raccoon;

    @Inject
    public void setRaccoon(final Raccoon raccoon)
    {
        this.raccoon = raccoon;
    }

    public Raccoon getRaccoon()
    {
        return raccoon;
    }
}
