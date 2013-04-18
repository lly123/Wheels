package com.freeroom.test.beans.fieldInjection;

import com.freeroom.di.annotations.Bean;
import com.freeroom.di.annotations.Inject;

@Bean
public class Person
{
    @Inject
    private Car car;

    private Home home;

    public Car getCar() {
        return car;
    }
}
