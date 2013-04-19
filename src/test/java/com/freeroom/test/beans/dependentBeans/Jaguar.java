package com.freeroom.test.beans.dependentBeans;

import com.freeroom.di.annotations.Bean;
import com.freeroom.di.annotations.Inject;

@Bean
public class Jaguar
{
    @Inject
    private Mustang mustang;

    private Ostrich ostrich;

    @Inject
    public Jaguar(Ostrich ostrich)
    {
        this.ostrich = ostrich;
    }

    public Mustang getMustang()
    {
        return mustang;
    }

    public Ostrich getOstrich()
    {
        return ostrich;
    }
}
