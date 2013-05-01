package com.freeroom.di.util;

import java.util.List;

import static com.google.common.collect.Lists.newArrayList;

public class FuncUtils
{
    public static <T, K> T reduce(T defaultValue, Iterable<K> values, Func<T, K> func)
    {
        T retVal = defaultValue;
        for (K value : values) {
            retVal = func.call(retVal, value);
        }
        return retVal;
    }

    public static <T, K> List<K> map(List<T> values, Func2<Integer, T, K> func)
    {
        List<K> retVal = newArrayList();
        for (int i = 0; i < values.size(); i++) {
            retVal.add(func.call(i, values.get(i)));
        }
        return retVal;
    }

    public static <T> void each(Iterable<T> values, Func0<T> func)
    {
        for (T value : values) {
            func.call(value);
        }
    }
}
