package com.freeroom.test.beans.setterInjection;

import com.freeroom.di.annotations.Bean;
import com.freeroom.di.annotations.Inject;

@Bean
public class Mackerels
{
    private Leopard leopard;
    private Cougar cougar;

    @Inject
    public void setLeopard(final Leopard leopard)
    {
        this.leopard = leopard;
    }

    @Inject
    public void putCougar(final Cougar cougar)
    {
        this.cougar = cougar;
    }
}
