package com.freeroom.test.beans.parallelPackages.packageOne;

import com.freeroom.di.annotations.Bean;
import com.freeroom.di.annotations.Inject;
import com.freeroom.test.beans.parallelPackages.packageOne.subPackage.Hamster;

@Bean
public class Rhinoceros
{
    private Hamster hamster;

    @Inject
    public void setHamster(final Hamster hamster)
    {
        this.hamster = hamster;
    }

    public Hamster getHamster()
    {
        return hamster;
    }
}
