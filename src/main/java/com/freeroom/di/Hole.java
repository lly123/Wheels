package com.freeroom.di;

import java.util.Collection;

interface Hole
{
    boolean isFilled();
    void fill(Collection<Pod> pods);
}
