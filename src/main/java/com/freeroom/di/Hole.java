package com.freeroom.di;

import com.freeroom.di.exceptions.NoBeanException;
import com.google.common.base.Optional;

import java.util.Collection;

abstract class Hole
{
    abstract boolean isFilled();
    abstract void fill(final Collection<Pod> pods);

    protected void assertPodExists(Class paramClass, Optional<Pod> pod)
    {
        if (!pod.isPresent()) {
            throw new NoBeanException("Can't find bean for " + paramClass);
        }
    }
}
