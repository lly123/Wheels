package com.freeroom.test.beans.dynamicScope;

import com.freeroom.di.annotations.Bean;
import com.freeroom.di.annotations.Inject;
import com.freeroom.di.annotations.Scope;

@Bean(scope = Scope.Dynamic)
public class Dingo
{
    private Tapir tapir;

    @Inject
    public void setTapir(Tapir tapir)
    {
        this.tapir = tapir;
    }

    public Tapir getTapir()
    {
        return tapir;
    }
}
