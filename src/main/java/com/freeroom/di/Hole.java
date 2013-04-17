package com.freeroom.di;

import java.util.Collection;

interface Hole
{
    boolean isFilled();
    void fill(final Collection<Pod> pods);
}
