package com.freeroom.persistence.proxy;

import com.google.common.base.Optional;
import net.sf.cglib.proxy.Factory;
import net.sf.cglib.proxy.MethodInterceptor;
import net.sf.cglib.proxy.MethodProxy;

import java.lang.reflect.Method;
import java.util.List;

import static com.freeroom.di.util.FuncUtils.map;
import static com.google.common.base.Optional.absent;
import static com.google.common.base.Optional.of;

public class Hecate implements MethodInterceptor
{
    private final Hades hades;
    private final Class<?> clazz;
    private final String sql;
    private final Optional<Long> foreignKey;
    private Optional<List<Long>> originalIDs;
    private List<Object> current;

    public Hecate(final Hades hades, final Class<?> clazz, final String sql, final Optional<Long> foreignKey)
    {
        this.hades = hades;
        this.clazz = clazz;
        this.sql = sql;
        this.foreignKey = foreignKey;
        this.originalIDs = absent();
    }

    @Override
    public Object intercept(final Object bean, final Method method,
                            final Object[] args, final MethodProxy methodProxy) throws Throwable
    {
        if (!originalIDs.isPresent()) {
            current = hades.loadList(clazz, sql, foreignKey);
            originalIDs = extractIDs(current);
        }
        return method.invoke(current, args);
    }

    private Optional<List<Long>> extractIDs(final List<Object> current)
    {
        return of(map(current, obj -> ((Charon)((Factory)obj).getCallback(0)).getPrimaryKeyAndValue().snd));
    }
}
