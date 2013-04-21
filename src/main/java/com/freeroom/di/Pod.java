package com.freeroom.di;

import com.freeroom.di.annotations.Scope;

abstract class Pod
{
    abstract String getBeanName();

    abstract Object getBean();

    abstract Class<?> getBeanClass();

    abstract Scope getScope();

    abstract boolean isBeanReady();

    abstract void removeBean();

    public boolean hasName(String name)
    {
        return getBeanName().equals(name) || getBeanName().endsWith("." + name);
    }
}
