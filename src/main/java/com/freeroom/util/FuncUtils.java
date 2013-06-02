package com.freeroom.util;

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

    public static <T, K> T reduce(T defaultValue, Iterable<K> values, FuncWithIndex<T, K> func)
    {
        T retVal = defaultValue;
        int index = 0;
        for (K value : values) {
            retVal = func.call(retVal, value, index);
            index++;
        }
        return retVal;
    }

    public static <T, K> List<K> map(List<T> values, Func1<T, K> func)
    {
        List<K> retVal = newArrayList();
        for (int i = 0; i < values.size(); i++) {
            retVal.add(func.call(values.get(i)));
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
