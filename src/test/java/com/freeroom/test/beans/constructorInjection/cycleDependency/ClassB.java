package com.freeroom.test.beans.constructorInjection.cycleDependency;

import com.freeroom.di.annotations.Bean;
import com.freeroom.di.annotations.Inject;

@Bean
public class ClassB
{
    @Inject
    public ClassB(ClassA classA) {

    }
}
