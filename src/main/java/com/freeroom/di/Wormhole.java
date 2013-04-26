package com.freeroom.di;

import com.freeroom.di.annotations.Inject;
import com.google.common.base.Optional;
import com.google.common.base.Predicate;

import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Type;
import java.util.Collection;

import static com.google.common.base.Optional.absent;
import static com.google.common.base.Optional.of;
import static com.google.common.base.Strings.isNullOrEmpty;
import static com.google.common.collect.Collections2.filter;
import static com.google.common.collect.Lists.newArrayList;

class Wormhole extends Hole
{
    protected Optional<?> bean = absent();
    protected final Class<?> clazz;
    protected final Optional<String> beanName;

    public Wormhole(final Class<?> clazz, final Optional<String> beanName)
    {
        this.clazz = clazz;
        this.beanName = beanName;
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
                return pod.isBeanReady() &&
                        (beanName.isPresent() ?
                                pod.hasName(beanName.get()) :
                                clazz.isAssignableFrom(pod.getBeanClass()));
            }
        });

        assertPodExists(clazz, filteredPods);
        assertNotMoreThanOnePod(clazz, filteredPods);

        bean = newArrayList(filteredPods).get(0).getBean();
    }

    protected static Optional<String> getInjectBeanName(AnnotatedElement element)
    {
        final Inject annotation = element.getAnnotation(Inject.class);
        return isNullOrEmpty(annotation.value()) ? Optional.<String>absent() : of(annotation.value());
    }
}
