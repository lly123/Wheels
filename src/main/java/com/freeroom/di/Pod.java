package com.freeroom.di;

import com.freeroom.di.annotations.Scope;

interface Pod
{
    String getBeanName();

    Object getBean();

    Class<?> getBeanClass();

    Scope getScope();

    boolean hasName(final String name);

    boolean isBeanReady();

    void removeBean();
}
