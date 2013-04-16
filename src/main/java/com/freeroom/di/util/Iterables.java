package com.freeroom.di.util;

public class Iterables
{
    public static <T, K> T reduce(T defaultValue, Iterable<K> values, Function<T, K> func)
    {
        T retVal = defaultValue;
        for (K value : values) {
            retVal = func.call(retVal, value);
        }
        return retVal;
    }
}
