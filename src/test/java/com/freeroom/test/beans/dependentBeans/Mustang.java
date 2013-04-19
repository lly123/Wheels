package com.freeroom.test.beans.dependentBeans;

import com.freeroom.di.annotations.Bean;
import com.freeroom.di.annotations.Inject;

@Bean
public class Mustang
{
    private Jaguar jaguar;

    @Inject
    public Mustang(Jaguar jaguar)
    {
        this.jaguar = jaguar;
    }

    public Jaguar getJaguar() {
        return jaguar;
    }
}
