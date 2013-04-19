package com.freeroom.test.beans.setterInjection;

import com.freeroom.di.annotations.Bean;
import com.freeroom.di.annotations.Inject;

@Bean
public class Camel
{
    private Mackerels mackerels;

    public Mackerels getMackerels()
    {
        return mackerels;
    }

    @Inject
    public void setMackerels(final Mackerels mackerels)
    {
        this.mackerels = mackerels;
    }
}
