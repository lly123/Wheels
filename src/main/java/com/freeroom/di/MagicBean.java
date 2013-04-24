package com.freeroom.di;

import net.sf.cglib.proxy.MethodInterceptor;
import net.sf.cglib.proxy.MethodProxy;

import java.lang.reflect.Method;

class MagicBean implements MethodInterceptor
{
    private final ConstructorHole constructorHole;
    private final Object bean;

    public MagicBean(ConstructorHole constructorHole)
    {
        this.constructorHole = constructorHole;
        this.bean = constructorHole.create();
    }

    @Override
    public Object intercept(final Object o, final Method method,
                            final Object[] args, final MethodProxy methodProxy) throws Throwable
    {
        //System.out.println("ssssssss>>>>>>>>>>> " + method + "  " + (args.length > 0 ? args[0] : ""));
        return method.invoke(bean, args);
    }
}
