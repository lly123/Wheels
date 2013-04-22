package com.freeroom.test.beans.beanFactory;

import com.freeroom.di.annotations.Bean;
import com.freeroom.di.annotations.Inject;
import com.freeroom.di.annotations.Scope;

@Bean(scope = Scope.Required)
public class Tuna
{
    private Pheasant pheasant;

    @Inject
    public void setPheasant(final Pheasant pheasant)
    {
        this.pheasant = pheasant;
    }

    public Pheasant getPheasant()
    {
        return pheasant;
    }
}
