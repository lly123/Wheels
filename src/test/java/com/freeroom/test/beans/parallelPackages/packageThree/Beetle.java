package com.freeroom.test.beans.parallelPackages.packageThree;

import com.freeroom.di.annotations.Bean;
import com.freeroom.di.annotations.Inject;
import com.freeroom.test.beans.parallelPackages.packageOne.Rhinoceros;

@Bean
public class Beetle
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
