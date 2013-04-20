package com.freeroom.di;

import com.google.common.base.Optional;
import com.google.common.base.Predicate;

import java.lang.reflect.Constructor;
import java.util.Collection;
import java.util.List;

import static com.google.common.collect.Iterables.tryFind;
import static com.google.common.collect.Lists.newArrayList;

class ConstructorHole extends Hole
{
    private final Constructor constructor;
    private final List<Object> readyBeans = newArrayList();
    private final Collection<Pod> unreadyPods = newArrayList();

    public ConstructorHole(final Constructor constructor)
    {
        this.constructor = constructor;
    }

    @Override
    public boolean isFilled()
    {
        return !readyBeans.isEmpty() && unreadyPods.isEmpty();
    }

    @Override
    public void fill(final Collection<Pod> pods)
    {
        readyBeans.clear();
        for (final Class paramClass : constructor.getParameterTypes()) {
            final Optional<Pod> pod = getPodForFill(paramClass, pods);
            assertPodExists(paramClass, pod);

            if (pod.get().isBeanReady()) {
                readyBeans.add(pod.get().getBean());
            } else {
                unreadyPods.add(pod.get());
            }
        }
    }

    public Collection<Pod> getUnreadyPods()
    {
        return unreadyPods;
    }

    public Object create()
    {
        try {
            return constructor.newInstance(readyBeans.toArray());
        } catch (Exception e) {
            throw new RuntimeException("Unexpected exception when creating bean.", e);
        }
    }

    private Optional<Pod> getPodForFill(final Class paramClass, final Collection<Pod> pods)
    {
        return tryFind(pods, new Predicate<Pod>() {
            @Override
            public boolean apply(final Pod pod) {
                return paramClass.isAssignableFrom(pod.getBeanClass());
            }
        });
    }
}
