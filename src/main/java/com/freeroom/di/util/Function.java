package com.freeroom.di.util;

public abstract class Function<T, K>
{
    public abstract T call(T s, K v);
}
