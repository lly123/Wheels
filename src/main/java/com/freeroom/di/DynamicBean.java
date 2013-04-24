package com.freeroom.di;

import com.google.common.base.Optional;
import net.sf.cglib.proxy.MethodInterceptor;
import net.sf.cglib.proxy.MethodProxy;

import java.lang.reflect.Method;

import static com.google.common.base.Optional.absent;
import static com.google.common.base.Optional.of;
import static java.lang.Thread.currentThread;

class DynamicBean implements MethodInterceptor
{
    private final ConstructorHole constructorHole;
    private Object bean;
    private Optional<String> key = absent();

    public DynamicBean(ConstructorHole constructorHole)
    {
        this.constructorHole = constructorHole;
    }

    @Override
    public Object intercept(final Object bean, final Method method,
                            final Object[] args, final MethodProxy methodProxy) throws Throwable
    {
        final String currentKey = generateKey(findCallerMethodName(bean.getClass()));

        if (!key.isPresent() || !key.get().equals(currentKey)) {
            this.bean = constructorHole.create();
            this.key = of(currentKey);
        }

        return method.invoke(this.bean, args);
    }

    private String generateKey(final Optional<String> callerMethodName)
    {
        return currentThread().getId() + ":" + callerMethodName.or("absent");
    }

    private Optional<String> findCallerMethodName(final Class<?> clazz)
    {
        final StackTraceElement[] stackTrace = currentThread().getStackTrace();
        for (int i = 0; i < stackTrace.length; i++) {
            if (stackTrace[i].getClassName().startsWith(clazz.getCanonicalName())) {
                return of(stackTrace[i + 1].getMethodName());
            }
        }
        return absent();
    }
}
