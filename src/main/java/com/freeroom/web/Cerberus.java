package com.freeroom.web;

import com.freeroom.di.util.FuncUtils;
import com.freeroom.di.util.Pair;
import com.google.common.base.Optional;

import java.io.UnsupportedEncodingException;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.net.URLDecoder;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.regex.Matcher;

import static com.freeroom.di.util.FuncUtils.each;
import static com.freeroom.di.util.FuncUtils.reduce;
import static com.google.common.base.Optional.absent;
import static com.google.common.base.Optional.fromNullable;
import static com.google.common.base.Optional.of;
import static com.google.common.collect.ImmutableList.copyOf;
import static com.google.common.collect.Lists.newArrayList;
import static com.sun.javafx.binding.StringFormatter.format;
import static java.lang.Boolean.parseBoolean;
import static java.lang.Double.parseDouble;
import static java.lang.Integer.parseInt;
import static java.lang.Long.parseLong;
import static java.util.regex.Pattern.compile;

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
        final Optional<Object> valueOpt = fromNullable(map.get(key));
        if (valueOpt.isPresent()) {
            return valueOpt;
        } else {
            return tryGetIndexedValues(key);
        }
    }

    private Optional<Object> tryGetIndexedValues(String key)
    {
        final Map<String, Object> indexedValues = new TreeMap<>();
        each(map.entrySet(), entry -> {
            final Matcher matcher = compile(format("^%s\\[(\\w+)\\]", key).getValue()).matcher(entry.getKey());
            if (matcher.find()) {
                indexedValues.put(matcher.group(1), entry.getValue());
            }
        });
        if (indexedValues.isEmpty()) {
            return absent();
        }
        return of((Object)newArrayList(indexedValues.values()));
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
                field.set(obj, parse(field.getType(), (String)value));
            } else if(value instanceof List<?>) {
                field.set(obj, parse(field.getGenericType(), (List<?>) value));
            } else {
                field.set(obj, value);
            }
        } catch (Exception ignored) {}
    }

    private Object parse(final Class<?> fieldType, final String value)
    {
        Object parsedValue = value;
        if (fieldType.equals(int.class) || fieldType.equals(Integer.class)) {
            parsedValue = parseInt(value);
        } if (fieldType.equals(long.class) || fieldType.equals(Long.class)) {
            parsedValue = parseLong(value);
        } if (fieldType.equals(double.class) || fieldType.equals(Double.class)) {
            parsedValue = parseDouble(value);
        } if (fieldType.equals(boolean.class) || fieldType.equals(Boolean.class)) {
            parsedValue = parseBoolean(value);
        }
        return parsedValue;
    }

    private Object parse(final Type fieldType, final List<?> values)
    {
        if (!(fieldType instanceof ParameterizedType)) {
            return values;
        }

        final Type type = ((ParameterizedType) fieldType).getActualTypeArguments()[0];
        return reduce(newArrayList(), values, (s, value) -> {
            s.add(parse((Class<?>)type, (String)value));
            return s;
        });
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
