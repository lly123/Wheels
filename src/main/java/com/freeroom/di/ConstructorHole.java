package com.freeroom.di;

import com.google.common.base.Optional;
import com.google.common.base.Predicate;

import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import static com.google.common.collect.Iterables.tryFind;

class ConstructorHole extends Hole
{
    private final Constructor constructor;
    private final List<Object> readyBeans;
    private final Collection<Pod> unreadyPods;

    public ConstructorHole(Constructor constructor)
    {
        this.constructor = constructor;
        this.readyBeans = new ArrayList<>();
        this.unreadyPods = new ArrayList<>();
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
        for (Class paramClass : constructor.getParameterTypes()) {
            Optional<Pod> pod = getPodForFill(paramClass, pods);
            assertPodExists(paramClass, pod);

            if (pod.get().isBeanConstructed()) {
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

    public Object create() {
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
            public boolean apply(Pod pod) {
                return paramClass.isAssignableFrom(pod.getBeanClass());
            }
        });
    }
}
