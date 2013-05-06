package com.freeroom.web;

import com.freeroom.di.util.Pair;
import com.google.common.base.Optional;

import java.io.UnsupportedEncodingException;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.net.URLDecoder;
import java.util.HashMap;
import java.util.Map;

import static com.freeroom.di.util.FuncUtils.each;
import static com.google.common.base.Optional.fromNullable;
import static com.google.common.collect.ImmutableList.copyOf;
import static java.lang.Boolean.parseBoolean;
import static java.lang.Double.parseDouble;
import static java.lang.Integer.parseInt;
import static java.lang.Long.parseLong;

public class Cerberus
{
    private final Map<String, Object> map = new HashMap<>();
    private final String charset;

    public Cerberus(final String charset)
    {
        this.charset = charset;
    }

    public Optional<Object> getValue(final String key)
    {
        return fromNullable(map.get(key));
    }

    public Cerberus add(final String keyValue)
    {
        final String[] strings = keyValue.split("=");
        if (strings.length < 2) {
            return this;
        }

        final String key = decode(strings[0]);
        final String value = decode(strings[1]);

        if (map.containsKey(key)) {
            return this;
        }

        if (isCompositeKey(key)) {
            final Pair<String, String> keys = splitKey(key);
            final Cerberus sub = map.containsKey(keys.fst) ? (Cerberus)map.get(keys.fst) : new Cerberus(charset);
            sub.add(keys.snd + "=" + value);
            map.put(keys.fst, sub);
        } else {
            map.put(key, value);
        }
        return this;
    }

    public void addValues(final String keyValues)
    {
        each(copyOf(keyValues.split("&")), keyValue -> add(keyValue));
    }

    private String decode(final String string)
    {
        try {
            return URLDecoder.decode(string, charset);
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException("Get exception when decoding parameters.", e);
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
                    final Object filledObj = ((Cerberus) value).fill(field.getType());
                    setFieldValue(obj, field, filledObj);
                } else {
                    setFieldValue(obj, field, value);
                }
            }
        });
        return obj;
    }

    private void setFieldValue(final Object obj, final Field field, final Object value)
    {
        try {
            field.setAccessible(true);
            if (value instanceof String) {
                final Class<?> fieldType = field.getType();
                if (fieldType.equals(String.class)) {
                    field.set(obj, value);
                } if (fieldType.equals(int.class) || fieldType.equals(Integer.class)) {
                    field.setInt(obj, parseInt((String)value));
                } if (fieldType.equals(long.class) || fieldType.equals(Long.class)) {
                    field.setLong(obj, parseLong((String)value));
                } if (fieldType.equals(double.class) || fieldType.equals(Double.class)) {
                    field.setDouble(obj, parseDouble((String)value));
                } if (fieldType.equals(boolean.class) || fieldType.equals(Boolean.class)) {
                    field.setBoolean(obj, parseBoolean((String)value));
                }
            } else {
                field.set(obj, value);
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
