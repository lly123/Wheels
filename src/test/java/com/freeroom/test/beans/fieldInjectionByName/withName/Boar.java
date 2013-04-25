package com.freeroom.test.beans.fieldInjectionByName.withName;

import com.freeroom.di.annotations.Bean;
import com.freeroom.di.annotations.Inject;
import com.freeroom.test.beans.sameParent.Ladybug;

@Bean
public class Boar
{
    @Inject("Termite")
    private Ladybug ladybug;

    public Ladybug getLadybug()
    {
        return ladybug;
    }
}
