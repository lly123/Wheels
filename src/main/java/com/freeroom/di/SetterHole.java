package com.freeroom.di;

import java.lang.reflect.Method;
import java.util.Collection;

public class SetterHole extends Hole
{
    public SetterHole(final Method method)
    {
    }

    @Override
    boolean isFilled()
    {
        return false;
    }

    @Override
    void fill(final Collection<Pod> pods)
    {
    }
}
