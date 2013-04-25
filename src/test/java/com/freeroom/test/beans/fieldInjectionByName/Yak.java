package com.freeroom.test.beans.fieldInjectionByName;

import com.freeroom.di.annotations.Bean;
import com.freeroom.di.annotations.Inject;
import com.freeroom.test.beans.sameParent.Ladybug;

@Bean
public class Yak
{
    @Inject
    private Ladybug ladybug;

    public Ladybug getLadybug()
    {
        return ladybug;
    }
}
