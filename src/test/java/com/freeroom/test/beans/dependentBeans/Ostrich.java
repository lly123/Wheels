package com.freeroom.test.beans.dependentBeans;

import com.freeroom.di.annotations.Bean;
import com.freeroom.di.annotations.Inject;

@Bean
public class Ostrich
{
    @Inject
    private Mustang mustang;

    @Inject
    private Jaguar jaguar;

    public Mustang getMustang()
    {
        return mustang;
    }

    public Jaguar getJaguar()
    {
        return jaguar;
    }
}
