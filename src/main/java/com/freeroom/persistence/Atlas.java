package com.freeroom.persistence;

import com.freeroom.di.util.Pair;
import com.freeroom.persistence.annotations.Persist;
import com.freeroom.persistence.annotations.ID;
import com.google.common.base.Optional;

import java.lang.reflect.Field;
import java.util.List;

import static com.freeroom.di.util.FuncUtils.reduce;
import static com.google.common.collect.ImmutableList.copyOf;
import static com.google.common.collect.Iterables.tryFind;
import static com.google.common.collect.Lists.newArrayList;

public class Atlas
{
    public static String getPrimaryKeyName(final Class<?> clazz)
    {
        final Optional<Field> pk = tryFind(copyOf(clazz.getDeclaredFields()),
                field -> field.isAnnotationPresent(ID.class));

        if (!pk.isPresent()) {
            throw new RuntimeException("Can't find primary key for bean: " + clazz);
        }

        return pk.get().getName();
    }

    public static List<Field> getColumnPrimitiveFields(final Class<?> clazz)
    {
        return reduce(newArrayList(), copyOf(clazz.getDeclaredFields()), (s, field) -> {
            if (field.isAnnotationPresent(Persist.class) && isBasicField(field)) {
                s.add(field);
            }
            return s;
        });
    }

    private static boolean isBasicField(final Field field)
    {
        final Class<?> fieldType = field.getType();
        return fieldType.isPrimitive() || fieldType.equals(String.class);
    }

    public static List<Pair<String, Object>> getColumns(final Object obj)
    {
        return reduce(newArrayList(), copyOf(obj.getClass().getDeclaredFields()), (s, field) -> {
            if (field.isAnnotationPresent(Persist.class)) {
                field.setAccessible(true);
                try {
                    s.add(Pair.of(field.getName(), field.get(obj)));
                } catch (Exception ignored) {}
            }
            return s;
        });
    }
}
