package com.freeroom.test.beans.constructionParameterInjection.normal;

import com.freeroom.di.annotations.Bean;
import com.freeroom.di.annotations.Inject;
import com.freeroom.test.beans.requiredScope.Otter;
import com.freeroom.test.beans.sameBeanName.Trout;

@Bean
public class Eel
{
    private final Trout trout;
    private final Otter otter;

    public Eel(@Inject final Trout trout, final Otter otter)
    {
        this.trout = trout;
        this.otter = otter;
    }

    public Trout getTrout()
    {
        return trout;
    }

    public Otter getOtter() {
        return otter;
    }
}
