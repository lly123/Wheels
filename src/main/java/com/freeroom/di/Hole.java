package com.freeroom.di;

import com.freeroom.di.exceptions.NoBeanException;
import com.freeroom.di.exceptions.NotUniqueException;

import java.util.Collection;

abstract class Hole
{
    abstract boolean isFilled();

    abstract void fill(final Collection<Pod> pods);

    protected void assertPodExists(final Class paramClass, final Collection<Pod> pods)
    {
        if (pods.isEmpty()) {
            throw new NoBeanException("Can't find bean for " + paramClass);
        }
    }

    protected void assertNotMoreThanOnePod(final Class paramClass, final Collection<Pod> pods)
    {
        if (pods.size() > 1) {
            throw new NotUniqueException("More than one bean can be assigned to: " + paramClass);
        }
    }
}
