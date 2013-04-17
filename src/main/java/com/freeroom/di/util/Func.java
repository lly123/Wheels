package com.freeroom.di.util;

public abstract class Func<T, K>
{
    public abstract T call(final T s, final K v);
}
