package com.freeroom.di;

import com.freeroom.di.annotations.Scope;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;

class PeaPod extends Pod
{
    private final Class<?> factoryClass;
    private final Method beanConstructor;

    public PeaPod(Class<?> factoryClass, final Method beanConstructor)
    {
        super(beanConstructor.getReturnType());
        this.factoryClass = factoryClass;
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

    public void constructBean()
    {
        final Object factory = createFactory(factoryClass);
        try {
            setBean(beanConstructor.invoke(factory));
        } catch (Exception ignored) {}
    }

    private Object createFactory(final Class<?> factoryClass)
    {
        try {
            final Constructor<?> defaultConstructor = factoryClass.getConstructor();
            return defaultConstructor.newInstance();
        } catch (Exception e) {
            throw new RuntimeException("Factory " + factoryClass + " must have one default constructor.");
        }
    }
}
