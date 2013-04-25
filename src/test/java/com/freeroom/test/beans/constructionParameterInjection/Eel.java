package com.freeroom.test.beans.constructionParameterInjection;

import com.freeroom.di.annotations.Bean;
import com.freeroom.di.annotations.Inject;
import com.freeroom.test.beans.sameBeanName.Trout;

@Bean
public class Eel
{
    private final Trout trout;

    public Eel(@Inject final Trout trout)
    {
        this.trout = trout;
    }

    public Trout getTrout()
    {
        return trout;
    }
}
