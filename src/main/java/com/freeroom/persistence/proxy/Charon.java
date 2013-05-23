package com.freeroom.persistence.proxy;

import com.freeroom.di.util.Pair;
import com.freeroom.persistence.Atlas;
import com.google.common.base.Optional;
import net.sf.cglib.proxy.Factory;
import net.sf.cglib.proxy.MethodInterceptor;
import net.sf.cglib.proxy.MethodProxy;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.sql.ResultSet;
import java.util.List;

import static com.google.common.base.Optional.absent;
import static com.google.common.base.Optional.of;
import static com.google.common.collect.Iterables.any;
import static java.lang.String.format;

public class Charon implements MethodInterceptor
{
    private final Hades hades;
    private final Class<?> clazz;
    private Pair<String, Long> primaryKeyAndValue;
    private final int blockSize;
    private Optional<Object> original;
    private Object current;
    private boolean removed;

    public Charon(final Hades hades, final Class<?> clazz,
                  final Pair<String, Long> primaryKeyAndValue, final int blockSize)
    {
        this.hades = hades;
        this.clazz = clazz;
        this.primaryKeyAndValue = primaryKeyAndValue;
        this.blockSize = blockSize;
        this.original = absent();
        this.current = hades.newInstance(clazz);
        this.removed = false;
    }

    @Override
    public Object intercept(final Object bean, final Method method,
                            final Object[] args, final MethodProxy methodProxy) throws Throwable
    {
        load(absent());
        return method.invoke(current, args);
    }

    protected void load(final Optional<ResultSet> resultSet)
    {
        if (isNotLoaded()) {
            if (resultSet.isPresent()) {
                original = of(hades.load(clazz, primaryKeyAndValue.snd, resultSet.get()));
            } else {
                original = of(hades.load(clazz, primaryKeyAndValue.snd));
            }
            current = copy(original.get());
        }
    }

    protected boolean isNotLoaded()
    {
        return !original.isPresent() && !removed;
    }

    protected boolean isDirty()
    {
        if (removed) return false;
        if (!original.isPresent()) return false;

        final List<Field> fields = Atlas.getBasicFields(clazz);
        return any(fields, field -> {
            try {
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
        final Object obj = copyWithoutRelations(original);
        final List<Pair<Field, Class>> oneToManyRelations = Atlas.getOneToManyRelations(clazz);
        final List<Field> oneToOneRelations = Atlas.getOneToOneRelations(clazz);

        try {
            final String sql = "SELECT %s FROM %s WHERE %s=?";

            for (final Pair<Field, Class> relation : oneToManyRelations) {
                relation.fst.set(obj, hades.createList(relation.snd,
                        format(sql, Atlas.getPrimaryKeyName(relation.snd),
                                relation.snd.getSimpleName(), clazz.getSimpleName() + "_" + primaryKeyAndValue.fst),
                        Optional.of(primaryKeyAndValue.snd), blockSize));
            }

            for (Field relation : oneToOneRelations) {
                final List<Object> objects = hades.loadList(relation.getType(),
                        format(sql, Atlas.getPrimaryKeyName(relation.getType()),
                                relation.getType().getSimpleName(), clazz.getSimpleName() + "_" + primaryKeyAndValue.fst),
                        Optional.of(primaryKeyAndValue.snd), blockSize);

                if (objects.size() > 1) {
                    throw new RuntimeException("ONE-TO-ONE relation " + relation.getType() + " has more than one object.");
                } else if (objects.size() == 1) {
                    relation.set(obj, objects.get(0));
                }
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return obj;
    }

    public Object detach()
    {
        if (!original.isPresent()) return null;

        final Object obj = copyWithoutRelations(current);

        try {
            final List<Field> oneToOneRelations = Atlas.getOneToOneRelations(clazz);
            for (Field relationField : oneToOneRelations) {
                final Object relation = relationField.get(current);
                relationField.setAccessible(true);
                if (relation instanceof Factory) {
                    relationField.set(obj, ((Charon)((Factory)relation).getCallback(0)).detach());
                }
            }

            final List<Pair<Field, Class>> oneToManyRelations = Atlas.getOneToManyRelations(clazz);
            for (Pair<Field, Class> relationField : oneToManyRelations) {
                final Object relation = relationField.fst.get(current);
                relationField.fst.setAccessible(true);
                if (relation instanceof Factory) {
                    relationField.fst.set(obj, ((Hecate) ((Factory) relation).getCallback(0)).detach());
                }
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        return obj;
    }

    private Object copyWithoutRelations(final Object original)
    {
        final Object obj = hades.newInstance(clazz);
        final Field pkField = Atlas.getPrimaryKey(clazz);
        final List<Field> fields = Atlas.getBasicFields(clazz);

        try {
            pkField.setLong(obj, pkField.getLong(original));

            for (final Field field : fields) {
                field.set(obj, field.get(original));
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        return obj;
    }
}