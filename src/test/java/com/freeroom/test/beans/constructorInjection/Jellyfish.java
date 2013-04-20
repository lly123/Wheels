package com.freeroom.test.beans.constructorInjection;

import com.freeroom.di.annotations.Bean;
import com.freeroom.di.annotations.Inject;

@Bean
public class Jellyfish
{
    @Inject
    public Jellyfish()
    {
    }

    @Inject
    public Jellyfish(Pangolin pangolin)
    {
    }
}
