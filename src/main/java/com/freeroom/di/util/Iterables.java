package com.freeroom.di.util;

import java.util.Collection;

public class Iterables
{
    public static <T, K> T reduce(T defaultValue, Collection<K> values, Function<T, K> func)
    {
        T retVal = defaultValue;
        for (K value : values) {
            retVal = func.call(retVal, value);
        }
        return retVal;
    }
}
