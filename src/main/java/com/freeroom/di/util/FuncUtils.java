package com.freeroom.di.util;

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
}
