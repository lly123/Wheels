package com.freeroom.di;

import com.google.common.base.Optional;
import com.google.common.base.Predicate;

import java.lang.reflect.Type;
import java.util.Collection;

import static com.google.common.base.Optional.absent;
import static com.google.common.collect.Collections2.filter;
import static com.google.common.collect.Lists.newArrayList;

class Wormhole extends Hole
{
    protected Optional<?> bean = absent();
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
        final Collection<Pod> filteredPods = filter(pods, new Predicate<Pod>() {
            @Override
            public boolean apply(final Pod pod) {
                return clazz.isAssignableFrom(pod.getBeanClass()) && pod.isBeanReady();
            }
        });

        assertPodExists(clazz, filteredPods);
        assertNotMoreThanOnePod(clazz, filteredPods);

        bean = newArrayList(filteredPods).get(0).getBean();
    }
}
