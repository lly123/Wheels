package com.freeroom.di;

import com.freeroom.di.annotations.Bean;
import com.freeroom.di.annotations.Scope;

import java.lang.reflect.Method;

import static com.google.common.base.Strings.isNullOrEmpty;

class PeaPod extends Pod
{
    private final Method beanConstructor;

    public PeaPod(final Method beanConstructor)
    {
        this.beanConstructor = beanConstructor;
    }

    @Override
    public String getBeanName()
    {
        final Bean beanAnnotation = beanConstructor.getAnnotation(Bean.class);
        return isNullOrEmpty(beanAnnotation.value()) ? getBeanClass().getCanonicalName() : beanAnnotation.value();
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
        final Bean beanAnnotation = beanConstructor.getAnnotation(Bean.class);
        return beanAnnotation.scope();
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
