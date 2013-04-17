package com.freeroom.di.util;

public class FuncUtils
{
    public static <T, K> T reduce(T defaultValue, Iterable<K> values, RFunc<T, K> func)
    {
        T retVal = defaultValue;
        for (K value : values) {
            retVal = func.call(retVal, value);
        }
        return retVal;
    }

    public static <T> void each(Iterable<T> values, Action<T> action)
    {
        for (T value : values) {
            action.call(value);
        }
    }
}
