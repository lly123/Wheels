package com.freeroom.persistence;

import com.freeroom.di.util.Pair;
import com.freeroom.persistence.annotations.ID;
import com.freeroom.persistence.annotations.Persist;
import com.google.common.base.Optional;

import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.List;

import static com.freeroom.di.util.FuncUtils.reduce;
import static com.google.common.collect.ImmutableList.copyOf;
import static com.google.common.collect.Iterables.tryFind;
import static com.google.common.collect.Lists.newArrayList;

public class Atlas
{
    public static String getPrimaryKeyName(final Class<?> clazz)
    {
        return getPrimaryKey(clazz).getName();
    }

    public static Field getPrimaryKey(final Class<?> clazz)
    {
        final Optional<Field> pk = tryFind(copyOf(clazz.getDeclaredFields()),
                field -> field.isAnnotationPresent(ID.class));

        if (!pk.isPresent()) {
            throw new RuntimeException("Can't find primary key for bean: " + clazz);
        }

        pk.get().setAccessible(true);
        return pk.get();
    }

    public static Pair<String, Long> getPrimaryKeyNameAndValue(final Object obj)
    {
        final Optional<Field> pk = tryFind(copyOf(obj.getClass().getDeclaredFields()),
                field -> field.isAnnotationPresent(ID.class));

        if (!pk.isPresent()) {
            throw new RuntimeException("Can't find primary key for bean: " + obj.getClass());
        }

        try {
            final Field pkField = pk.get();
            pkField.setAccessible(true);
            return Pair.of(pkField.getName(), pkField.getLong(obj));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static List<Field> getBasicFields(final Class<?> clazz)
    {
        return reduce(newArrayList(), copyOf(clazz.getDeclaredFields()), (s, field) -> {
            if (field.isAnnotationPresent(Persist.class) && isBasicField(field)) {
                field.setAccessible(true);
                s.add(field);
            }
            return s;
        });
    }

    public static List<Pair<Field, Object>> getBasicFieldAndValues(final Object obj)
    {
        return reduce(newArrayList(), copyOf(obj.getClass().getDeclaredFields()), (s, field) -> {
            if (field.isAnnotationPresent(Persist.class) && isBasicField(field)) {
                field.setAccessible(true);
                try {
                    Object value = field.get(obj);
                    if (isListLong(field) && value == null) {
                        value = newArrayList();
                    }
                    s.add(Pair.of(field, value));
                } catch (Exception ignored) {}
            }
            return s;
        });
    }

    private static boolean isBasicField(final Field field)
    {
        final Class<?> fieldType = field.getType();
        return fieldType.isPrimitive() || fieldType.equals(String.class) || isListLong(field);
    }

    public static boolean isListLong(final Field field)
    {
        if (field.getGenericType() instanceof ParameterizedType) {
            ParameterizedType type = (ParameterizedType)field.getGenericType();
            return type.getRawType().equals(List.class) &&
                   type.getActualTypeArguments()[0].equals(Long.class);
        }
        return false;
    }

    public static boolean isList(final Object obj)
    {
        return List.class.isAssignableFrom(obj.getClass());
    }

    public static List<Pair<Field, Class>> getOneToManyRelations(final Class<?> clazz)
    {
        return reduce(newArrayList(), copyOf(clazz.getDeclaredFields()), (s, field) -> {
            if (field.isAnnotationPresent(Persist.class) && !isBasicField(field) && isGenericListField(field)) {
                field.setAccessible(true);
                s.add(Pair.of(field, getFirstGenericParameterClass(field)));
            }
            return s;
        });
    }

    public static List<Field> getOneToOneRelations(final Class<?> clazz)
    {
        return reduce(newArrayList(), copyOf(clazz.getDeclaredFields()), (s, field) -> {
            if (field.isAnnotationPresent(Persist.class) && !isBasicField(field) && !isGenericListField(field)) {
                field.setAccessible(true);
                s.add(field);
            }
            return s;
        });
    }

    private static boolean isGenericListField(final Field field)
    {
        final Type type = field.getGenericType();
        return (type instanceof ParameterizedType) &&
            ((ParameterizedType)type).getRawType().equals(List.class) &&
            ((ParameterizedType)type).getActualTypeArguments().length > 0;
    }

    private static Class getFirstGenericParameterClass(final Field field)
    {
        return (Class)((ParameterizedType)field.getGenericType()).getActualTypeArguments()[0];
    }
}
