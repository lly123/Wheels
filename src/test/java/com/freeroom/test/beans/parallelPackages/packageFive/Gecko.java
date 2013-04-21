package com.freeroom.test.beans.parallelPackages.packageFive;

import com.freeroom.di.annotations.Bean;
import com.freeroom.di.annotations.Inject;
import com.freeroom.test.beans.parallelPackages.packageTwo.Antelope;

@Bean
public class Gecko
{
    private Antelope antelope;

    @Inject
    public void setAntelope(final Antelope antelope)
    {
        this.antelope = antelope;
    }

    public Antelope getAntelope()
    {
        return antelope;
    }
}
