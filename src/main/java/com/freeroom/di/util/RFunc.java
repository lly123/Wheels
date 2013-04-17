package com.freeroom.di.util;

public abstract class RFunc<T, K>
{
    public abstract T call(final T s, final K v);
}
