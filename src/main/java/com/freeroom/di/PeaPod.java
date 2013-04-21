package com.freeroom.di;

import com.freeroom.di.annotations.Scope;

import java.lang.reflect.Method;

class PeaPod implements Pod
{
    private final Method beanConstructor;

    public PeaPod(final Method beanConstructor)
    {
        this.beanConstructor = beanConstructor;
    }

    @Override
    public String getBeanName()
    {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public Object getBean()
    {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public Class<?> getBeanClass()
    {
        return beanConstructor.getReturnType();
    }

    @Override
    public Scope getScope()
    {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public boolean hasName(String name)
    {
        return false;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public boolean isBeanReady()
    {
        return true;
    }

    @Override
    public void removeBean()
    {
        //To change body of implemented methods use File | Settings | File Templates.
    }
}
