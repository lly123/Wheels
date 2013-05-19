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
import static com.freeroom.di.util.FuncUtils.reduce;
import static com.google.common.base.Optional.absent;
import static com.google.common.base.Optional.of;
import static com.google.common.collect.ImmutableList.copyOf;
import static com.google.common.collect.Iterables.all;
import static com.google.common.collect.Iterables.tryFind;
import static com.google.common.collect.Lists.newArrayList;

public class Hecate implements MethodInterceptor
{
    private final Hades hades;
    private final Class<?> clazz;
    private final String sql;
    private final Optional<Long> foreignKey;
    private Optional<List<Object>> original;
    private List<Object> current;

    public Hecate(final Hades hades, final Class<?> clazz, final String sql, final Optional<Long> foreignKey)
    {
        this.hades = hades;
        this.clazz = clazz;
        this.sql = sql;
        this.foreignKey = foreignKey;
        this.original = absent();
    }

    @Override
    public Object intercept(final Object bean, final Method method,
                            final Object[] args, final MethodProxy methodProxy) throws Throwable
    {
        if (!original.isPresent()) {
            current = hades.loadList(clazz, sql, foreignKey);
            original = copy(current);
        }
        return method.invoke(current, args);
    }

    private Optional<List<Object>> copy(final List<Object> current)
    {
        return of(map(current, obj -> obj));
    }

    public boolean isDirty()
    {
        return areIDsNotSame(current, original.get()) || areObjsDirty(current);
    }

    private boolean areIDsNotSame(final List<Object> current, final List<Object> original)
    {
        if (current.size() != original.size()) return true;

        final List<Long> currentIDs = map(current, obj -> {
            if (obj instanceof Factory) {
                return getPrimaryKeyValue((Factory)obj);
            } else {
                return Atlas.getPrimaryKeyNameAndValue(obj).snd;
            }
        });

        final List<Long> originalIDs = map(original, obj -> getPrimaryKeyValue((Factory)obj));

        final List<Long> sortedCurrentIDs = Ordering.natural().sortedCopy(currentIDs);
        final List<Long> sortedOriginalIDs = Ordering.natural().sortedCopy(originalIDs);

        boolean retVal = false;
        for (int i = 0; i < sortedCurrentIDs.size(); i++) {
            retVal = retVal || (sortedCurrentIDs.get(i) != sortedOriginalIDs.get(i));
        }
        return retVal;
    }

    private Long getPrimaryKeyValue(final Factory obj) {
        return ((Charon)obj.getCallback(0)).getPrimaryKeyAndValue().snd;
    }

    private boolean areObjsDirty(final List<Object> current)
    {
        return !all(copyOf(current), o -> (o instanceof Factory) && !((Charon)((Factory)o).getCallback(0)).isDirty());
    }

    public List<Factory> getRemoved()
    {
        return reduce(newArrayList(), original.get(), (s, obj) -> {
            if (notContainsInCurrent(getPrimaryKeyValue((Factory)obj))) {
                s.add((Factory)obj);
            }
            return s;
        });
    }

    public List<Object> getAdded()
    {
        return reduce(newArrayList(), current, (s, obj) -> {
            if (!(obj instanceof Factory)) {
                s.add(obj);
            }
            return s;
        });
    }

    private boolean notContainsInCurrent(final Long primaryKey)
    {
        return !tryFind(current, o ->
                (o instanceof Factory) && getPrimaryKeyValue((Factory) o) == primaryKey).isPresent();
    }

    public List<Factory> getModified()
    {
        return reduce(newArrayList(), current, (s, obj) -> {
            if ((obj instanceof Factory) && ((Charon)((Factory)obj).getCallback(0)).isDirty()) {
                s.add((Factory)obj);
            }
            return s;
        });
    }
}
