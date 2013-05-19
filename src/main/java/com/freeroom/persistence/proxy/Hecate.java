package com.freeroom.persistence.proxy;

import com.freeroom.persistence.Atlas;
import com.google.common.base.Optional;
import com.google.common.collect.Ordering;
import net.sf.cglib.proxy.Factory;
import net.sf.cglib.proxy.MethodInterceptor;
import net.sf.cglib.proxy.MethodProxy;

import java.lang.reflect.Method;
import java.util.List;

import static com.freeroom.di.util.FuncUtils.map;
import static com.google.common.base.Optional.absent;
import static com.google.common.base.Optional.of;
import static com.google.common.collect.ImmutableList.copyOf;
import static com.google.common.collect.Iterables.all;

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

    public boolean isDirty()
    {
        final List<Long> currentIDs = map(current, obj -> {
            if (obj instanceof Factory) {
                return ((Charon)((Factory)obj).getCallback(0)).getPrimaryKeyAndValue().snd;
            } else {
                return Atlas.getPrimaryKeyNameAndValue(obj).snd;
            }
        });

        return areIDsSame(currentIDs, originalIDs.get()) || areObjsDirty(current);
    }

    private boolean areIDsSame(final List<Long> currentIDs, final List<Long> originalIDs)
    {
        if (currentIDs.size() != originalIDs.size()) return true;

        final List<Long> sortedCurrentIDs = Ordering.natural().sortedCopy(currentIDs);
        final List<Long> sortedOriginalIDs = Ordering.natural().sortedCopy(originalIDs);

        boolean retVal = false;
        for (int i = 0; i < sortedCurrentIDs.size(); i++) {
            retVal = retVal || (sortedCurrentIDs.get(i) != sortedOriginalIDs.get(i));
        }
        return retVal;
    }

    private boolean areObjsDirty(final List<Object> current)
    {
        return !all(copyOf(current), o -> (o instanceof Factory) && !((Charon)((Factory)o).getCallback(0)).isDirty());
    }
}
