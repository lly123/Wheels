package com.freeroom.test.beans.constructionParameterInjection.manyChoices;

import com.freeroom.di.annotations.Bean;
import com.freeroom.di.annotations.Inject;
import com.freeroom.test.beans.dummy.Dummy;
import com.freeroom.test.beans.sameParent.Ladybug;

@Bean
public class Mantis
{
    private final Ladybug ladybug;

    public Mantis(final Dummy dummy, @Inject final Ladybug ladybug)
    {
        this.ladybug = ladybug;
    }

    public Ladybug getLadybug()
    {
        return ladybug;
    }
}
