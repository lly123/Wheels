package com.freeroom.test.beans.constrcutorInjection.cycleDependency;

import com.freeroom.di.annotations.Bean;
import com.freeroom.di.annotations.Inject;

@Bean
public class ClassA
{
    @Inject
    public ClassA(ClassB classB) {
    }
}
