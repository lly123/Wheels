package com.freeroom.test.beans.constructorInjection.subPackage;

import com.freeroom.di.annotations.Bean;
import com.freeroom.di.annotations.Inject;

@Bean
public class Caribou
{
    private final Marmot marmot;

    @Inject
    public Caribou(Marmot marmot) {
        this.marmot = marmot;
    }

    public Marmot getMarmot()
    {
        return marmot;
    }
}
