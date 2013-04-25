package com.freeroom.di;

import com.freeroom.di.annotations.Inject;
import com.google.common.base.Optional;
import com.google.common.base.Predicate;
import com.sun.tools.javac.util.Pair;

import java.lang.annotation.Annotation;
import java.lang.reflect.Constructor;
import java.util.Collection;
import java.util.List;

import static com.google.common.base.Optional.absent;
import static com.google.common.collect.ImmutableList.copyOf;
import static com.google.common.collect.Iterables.any;
import static com.google.common.collect.Iterables.tryFind;
import static com.google.common.collect.Lists.asList;
import static com.google.common.collect.Lists.newArrayList;

class ConstructorHole extends Hole
{
    private final Constructor constructor;
    private final List<Object> readyBeans = newArrayList();
    private final Collection<SoyPod> unreadyPods = newArrayList();

    public ConstructorHole(final Constructor constructor)
    {
        this.constructor = constructor;
    }

    @Override
    public boolean isFilled()
    {
        return isNoParameters() || (!readyBeans.isEmpty() && unreadyPods.isEmpty());
    }

    @Override
    public void fill(final Collection<Pod> pods)
    {
        readyBeans.clear();
        for (final Pair<Class, Boolean> param : getParameters()) {
            final Optional<Pod> pod = getPodForFill(param, pods);

            if (pod.isPresent()) {
                if (pod.get().isBeanReady()) {
                    readyBeans.add(pod.get().getBean().get());
                } else {
                    unreadyPods.add((SoyPod)pod.get());
                }
            } else {
                readyBeans.add(null);
            }
        }
    }

    private List<Pair<Class, Boolean>> getParameters()
    {
        final List<Pair<Class, Boolean>> parameters = newArrayList();
        final Annotation[][] annotations = constructor.getParameterAnnotations();
        for (int i = 0; i < annotations.length; i++) {
            parameters.add(Pair.of(constructor.getParameterTypes()[i], hasInjectAnnotation(annotations[i])));
        }
        return parameters;
    }

    public Collection<SoyPod> getUnreadyPods()
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

    private Optional<Pod> getPodForFill(final Pair<Class, Boolean> param, final Collection<Pod> pods)
    {
        if (param.snd) {
            final Optional<Pod> pod = tryFind(pods, new Predicate<Pod>() {
                @Override
                public boolean apply(final Pod pod) {
                    return param.fst.isAssignableFrom(pod.getBeanClass());
                }
            });
            assertPodExists(param.fst, pod);
            return pod;
        } else {
            return absent();
        }
    }

    private boolean isNoParameters()
    {
        return constructor.getParameterTypes().length == 0;
    }

    private boolean hasInjectAnnotation(final Annotation[] annotations)
    {
        return any(copyOf(annotations), new Predicate<Annotation>() {
            @Override
            public boolean apply(final Annotation annotation) {
                return annotation.annotationType().equals(Inject.class);
            }
        });
    }
}
