package com.freeroom.test.beans.fieldInjection;

import com.freeroom.di.annotations.Bean;
import com.freeroom.di.annotations.Inject;
import com.freeroom.test.beans.fieldInjection.Person;

@Bean("Monster")
public class Car
{
    @Inject
    private Person driver;

    public Person getDriver() {
        return driver;
    }
}
