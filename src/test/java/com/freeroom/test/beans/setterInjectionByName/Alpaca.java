package com.freeroom.test.beans.setterInjectionByName;

import com.freeroom.di.annotations.Bean;
import com.freeroom.di.annotations.Inject;
import com.freeroom.test.beans.sameParent.Ladybug;

@Bean
public class Alpaca
{
    private Ladybug ladybug;

    @Inject("Ladybug")
    public void setLadybug(final Ladybug ladybug)
    {
        this.ladybug = ladybug;
    }

    public Ladybug getLadybug() {
        return ladybug;
    }
}
