package com.freeroom.test.beans.parallelPackages.packageTwo;

import com.freeroom.di.annotations.Bean;
import com.freeroom.di.annotations.Inject;
import com.freeroom.test.beans.parallelPackages.packageOne.Rhinoceros;

@Bean
public class Antelope
{
    private Rhinoceros rhinoceros;

    @Inject
    public void setRhinoceros(final Rhinoceros rhinoceros)
    {
        this.rhinoceros = rhinoceros;
    }

    public Rhinoceros getRhinoceros()
    {
        return rhinoceros;
    }
}
