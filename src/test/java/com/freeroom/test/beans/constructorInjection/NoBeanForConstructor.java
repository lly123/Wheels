package com.freeroom.test.beans.constructorInjection;

import com.freeroom.di.annotations.Bean;
import com.freeroom.di.annotations.Inject;
import com.freeroom.test.beans.Dummy;

@Bean
public class NoBeanForConstructor
{
    @Inject
    public NoBeanForConstructor(Dummy dummy)
    {
    }
}
