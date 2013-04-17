package com.freeroom.di;

import com.google.common.base.Optional;
import com.google.common.base.Predicate;

import java.lang.reflect.Type;
import java.util.Collection;

import static com.google.common.collect.Iterables.tryFind;

abstract class PinHole implements Hole
{
    protected Class<?> clazz;
    protected Optional<Object> bean = Optional.absent();

    public PinHole(Class<?> clazz) {
        this.clazz = clazz;
    }

    public Type getHoleClass() {
        return clazz;
    }

    public Object getBean() {
        return bean.get();
    }

    @Override
    public boolean isFilled() {
        return bean.isPresent();
    }

    @Override
    public void fill(final Collection<Pod> pods) {
        Optional<Pod> pod = tryFind(pods, new Predicate<Pod>() {
            @Override
            public boolean apply(Pod pod) {
                return clazz.isAssignableFrom(pod.getBeanClass());
            }
        });

        if (!pod.isPresent()) {
            throw new ClassCastException("Bean " + bean + " can't be assigned to " + clazz);
        }
        this.bean = Optional.of(pod.get().getBean());
    }
}
