package com.freeroom.test.beans.setterInjection;

import com.freeroom.di.annotations.Bean;
import com.freeroom.di.annotations.Inject;

@Bean
public class Leopard
{
    private Camel camel;
    private Cougar cougar;

    @Inject
    public void setAnimals(Camel camel, Cougar cougar)
    {
    }
}
