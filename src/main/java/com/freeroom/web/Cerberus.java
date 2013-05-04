package com.freeroom.web;

import com.freeroom.di.util.Pair;
import com.google.common.base.Optional;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;

import static com.freeroom.di.util.FuncUtils.each;
import static com.google.common.base.Optional.fromNullable;
import static com.google.common.collect.ImmutableList.copyOf;
import static java.lang.Integer.parseInt;

public class Cerberus
{
    private final Map<String, Object> map = new HashMap<>();

    public Optional<Object> getValue(final String key)
    {
        return fromNullable(map.get(key));
    }

    public void add(final String keyValue)
    {
        final String[] strings = keyValue.split("=");
        final String key = strings[0];
        final String value = strings[1];

        if (isCompositeKey(key)) {
            final Pair<String, String> keys = splitKey(key);
            final Cerberus sub = map.containsKey(keys.fst) ? (Cerberus)map.get(keys.fst) : new Cerberus();
            sub.add(keys.snd + "=" + value);
            map.put(keys.fst, sub);
        } else {
            map.put(key, value);
        }
    }

    private Pair<String, String> splitKey(final String key)
    {
        return Pair.of(key.substring(0, key.indexOf("_")), key.substring(key.indexOf("_") + 1));
    }

    private boolean isCompositeKey(final String key)
    {
        return key.contains("_");
    }

    public Object fill(Class<?> clazz)
    {
        final Object obj = createAnInstance(clazz);
        each(copyOf(clazz.getDeclaredFields()), field -> {
            final Optional<Object> valueOpt = getValue(field.getName());
            if (valueOpt.isPresent()) {
                final Object value = valueOpt.get();
                if (value instanceof Cerberus) {

                } else {
                    setFieldValue(obj, field, (String)value);
                }
            }
        });
        return obj;
    }

    private void setFieldValue(final Object obj, final Field field, final String value)
    {
        try {
            field.setAccessible(true);
            final Class<?> fieldType = field.getType();
            if (fieldType.equals(String.class)) {
                field.set(obj, value);
            } if (fieldType.equals(int.class) || fieldType.equals(Integer.class)) {
                field.setInt(obj, parseInt(value));
            }
        } catch (Exception ignored) {}
    }

    private Object createAnInstance(final Class<?> clazz)
    {
        try {
            final Constructor<?> constructor = clazz.getConstructor();
            return constructor.newInstance();
        } catch (Exception e) {
            throw new RuntimeException("Get exception when creating an instance of " + clazz, e);
        }
    }
}
