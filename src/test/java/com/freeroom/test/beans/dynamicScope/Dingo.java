package com.freeroom.test.beans.dynamicScope;

import com.freeroom.di.annotations.Bean;
import com.freeroom.di.annotations.Inject;

@Bean
public class Dingo
{
    private Tapir tapir;

    @Inject
    public void setTapir(final Tapir tapir)
    {
        this.tapir = tapir;
    }

    public Tapir getTapir()
    {
        return tapir;
    }
}
