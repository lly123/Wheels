package com.freeroom.test.beans.constructorInjection;

import com.freeroom.di.annotations.Bean;
import com.freeroom.di.annotations.Inject;

@Bean
public class Tarsier
{
    @Inject
    public Tarsier(Boa boa, Pangolin pangolin) {
    }
}
