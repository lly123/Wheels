package com.freeroom.persistence.proxy;

import com.freeroom.di.util.Pair;
import com.freeroom.persistence.Atlas;
import com.google.common.base.Optional;
import net.sf.cglib.proxy.MethodInterceptor;
import net.sf.cglib.proxy.MethodProxy;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.List;

import static com.google.common.base.Optional.absent;
import static com.google.common.base.Optional.of;
import static com.google.common.collect.Iterables.any;

public class Charon implements MethodInterceptor
{
    private final Hades hades;
    private final Class<?> clazz;
    private Pair<String, Long> primaryKeyAndValue;
    private Optional<Object> original;
    private Object current;
    private boolean removed;

    public Charon(Hades hades, Class<?> clazz, final Pair<String, Long> primaryKeyAndValue)
    {
        this.hades = hades;
        this.clazz = clazz;
        this.primaryKeyAndValue = primaryKeyAndValue;
        this.original = absent();
        this.current = hades.newInstance(clazz);
        this.removed = false;
    }

    @Override
    public Object intercept(final Object bean, final Method method,
                            final Object[] args, final MethodProxy methodProxy) throws Throwable
    {
        if (!original.isPresent() && !removed) {
            original = of(hades.load(clazz, primaryKeyAndValue.snd));
            current = copy(original.get());
        }
        return method.invoke(current, args);
    }

    protected boolean isDirty()
    {
        if (removed) return false;

        final List<Field> fields = Atlas.getBasicFields(clazz);
        return any(fields, field -> {
            try {
                field.setAccessible(true);
                Object v1 = field.get(current);
                Object v2 = field.get(original.get());
                return !v1.equals(v2);
            } catch (Exception ignored) {}
            return false;
        });
    }

    protected Pair<String, Long> getPrimaryKeyAndValue()
    {
        return primaryKeyAndValue;
    }

    protected Object getCurrent()
    {
        return current;
    }

    protected String getPersistBeanName()
    {
        return clazz.getSimpleName();
    }

    protected void removed()
    {
        this.removed = true;
    }

    private Object copy(final Object original)
    {
        final Object obj = hades.newInstance(clazz);
        final Field pkField = Atlas.getPrimaryKey(clazz);
        final List<Field> fields = Atlas.getBasicFields(clazz);

        try {
            pkField.setLong(obj, pkField.getLong(original));
            for (final Field field : fields) {
                field.setAccessible(true);
                field.set(obj, field.get(original));
            }
        } catch (Exception ignored) {}
        return obj;
    }
}