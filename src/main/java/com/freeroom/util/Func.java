package com.freeroom.util;

public interface Func<T, K>
{
    T call(final T s, final K v);
}
