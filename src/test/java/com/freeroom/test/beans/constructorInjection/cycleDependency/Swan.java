package com.freeroom.test.beans.constructorInjection.cycleDependency;

import com.freeroom.di.annotations.Bean;
import com.freeroom.di.annotations.Inject;

@Bean
public class Swan
{
    @Inject
    public Swan(Balloonfish balloonfish)
    {
    }
}
