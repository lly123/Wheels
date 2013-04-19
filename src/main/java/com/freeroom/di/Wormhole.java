package com.freeroom.di;

import com.google.common.base.Optional;
import com.google.common.base.Predicate;

import java.lang.reflect.Type;
import java.util.Collection;

import static com.google.common.base.Optional.of;
import static com.google.common.collect.Iterables.tryFind;

class Wormhole extends Hole
{
    protected Optional<?> bean = Optional.absent();
    protected final Class<?> clazz;

    public Wormhole(Class<?> clazz)
    {
        this.clazz = clazz;
    }

    public Type getHoleClass()
    {
        return clazz;
    }

    public Object getBean()
    {
        return bean.get();
    }

    @Override
    public boolean isFilled()
    {
        return bean.isPresent();
    }

    @Override
    public void fill(final Collection<Pod> pods)
    {
        Optional<Pod> pod = tryFind(pods, new Predicate<Pod>() {
            @Override
            public boolean apply(final Pod pod) {
                return clazz.isAssignableFrom(pod.getBeanClass()) && pod.isBeanReady();
            }
        });

        assertPodExists(clazz, pod);

        bean = of(pod.get().getBean());
    }
}
