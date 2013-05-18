package com.freeroom.persistence.proxy;

import com.freeroom.persistence.Atlas;
import com.google.common.base.Optional;
import net.sf.cglib.proxy.MethodInterceptor;
import net.sf.cglib.proxy.MethodProxy;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.List;

import static com.freeroom.di.util.FuncUtils.each;
import static com.google.common.base.Optional.absent;
import static com.google.common.base.Optional.of;
import static com.google.common.collect.Iterables.any;

public class Charon implements MethodInterceptor
{
    private final Hades hades;
    private final Class<?> clazz;
    private final long primaryKey;
    private Optional<Object> original;
    private Optional<Object> current;

    public Charon(Hades hades, Class<?> clazz, long primaryKey)
    {
        this.hades = hades;
        this.clazz = clazz;
        this.primaryKey = primaryKey;
        this.original = absent();
        this.current = absent();
    }

    @Override
    public Object intercept(final Object bean, final Method method,
                            final Object[] args, final MethodProxy methodProxy) throws Throwable
    {
        if (!original.isPresent()) {
            original = of(hades.load(clazz, primaryKey));
            current = copy(original);
        }
        return method.invoke(current.get(), args);
    }

    protected boolean isDirty()
    {
        final List<Field> fields = Atlas.getColumnPrimitiveFields(clazz);
        return any(fields, field -> {
            try {
                field.setAccessible(true);
                Object v1 = field.get(current.get());
                Object v2 = field.get(original.get());
                return !v1.equals(v2);
            } catch (Exception ignored) {}
            return false;
        });
    }

    private Optional<Object> copy(final Optional<Object> original)
    {
        final Object obj = hades.newInstance(clazz);
        final List<Field> fields = Atlas.getColumnPrimitiveFields(clazz);
        each(fields, field -> {
            try {
                field.setAccessible(true);
                field.set(obj, field.get(original.get()));
            } catch (Exception ignored) {}
        });
        return Optional.of(obj);
    }
}