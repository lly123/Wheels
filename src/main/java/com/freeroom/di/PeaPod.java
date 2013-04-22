package com.freeroom.di;

import com.freeroom.di.annotations.Scope;

import java.lang.reflect.Method;

class PeaPod extends Pod
{
    private final Method beanConstructor;

    public PeaPod(final Method beanConstructor)
    {
        super(beanConstructor.getReturnType());
        this.beanConstructor = beanConstructor;
    }

    @Override
    public Scope getScope()
    {
        return getScope(beanConstructor);
    }

    @Override
    public String getBeanName()
    {
        return getBeanName(beanConstructor);
    }
}
