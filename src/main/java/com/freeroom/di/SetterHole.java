package com.freeroom.di;

import java.lang.reflect.Method;

class SetterHole extends Wormhole
{
    private final Method method;

    public SetterHole(final Method method)
    {
        super(getFirstParamClass(method), getInjectBeanName(method));
        this.method = method;
    }

    private static Class<?> getFirstParamClass(final Method method)
    {
        return method.getParameterTypes()[0];
    }

    public Method getMethod()
    {
        return method;
    }
}
