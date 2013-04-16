package com.freeroom.test.beans;

import com.freeroom.di.annotations.Bean;
import com.freeroom.di.annotations.Inject;

@Bean("Monster")
public class Car
{
    @Inject
    private Person driver;
}
