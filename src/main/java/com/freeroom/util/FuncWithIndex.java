package com.freeroom.util;

public interface FuncWithIndex<T, K>
{
    T call(final T s, final K v, final int index);
}
