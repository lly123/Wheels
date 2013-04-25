package com.freeroom.test.beans.constructionParameterInjection.withName;

import com.freeroom.di.annotations.Bean;
import com.freeroom.di.annotations.Inject;
import com.freeroom.test.beans.dummy.Dummy;
import com.freeroom.test.beans.sameParent.Ladybug;

@Bean
public class Turbot
{
    private final Ladybug ladybug;

    public Turbot(final Dummy dummy, @Inject("Termite") final Ladybug ladybug)
    {
        this.ladybug = ladybug;
    }

    public Ladybug getLadybug()
    {
        return ladybug;
    }
}
