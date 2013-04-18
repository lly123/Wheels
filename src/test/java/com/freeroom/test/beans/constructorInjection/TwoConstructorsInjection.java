package com.freeroom.test.beans.constructorInjection;

import com.freeroom.di.annotations.Bean;
import com.freeroom.di.annotations.Inject;

@Bean
public class TwoConstructorsInjection
{
    @Inject
    public TwoConstructorsInjection()
    {
    }

    @Inject
    public TwoConstructorsInjection(Student student)
    {
    }
}
