package com.freeroom.di;

import com.freeroom.di.annotations.Inject;
import com.google.common.base.Optional;
import com.google.common.base.Predicate;
import com.google.common.collect.ImmutableList;
import com.freeroom.di.util.Pair;

import java.lang.annotation.Annotation;
import java.lang.reflect.Constructor;
import java.util.Collection;
import java.util.List;

import static com.freeroom.di.util.FuncUtils.map;
import static com.google.common.base.Optional.absent;
import static com.google.common.base.Optional.of;
import static com.google.common.base.Strings.isNullOrEmpty;
import static com.google.common.collect.ImmutableList.copyOf;
import static com.google.common.collect.Iterables.filter;
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
        for (final Pair<Class, Pair<Boolean, Optional<String>>> param : getParameters()) {
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

    private List<Pair<Class, Pair<Boolean, Optional<String>>>> getParameters()
    {
        return map(copyOf(constructor.getParameterAnnotations()),
                (i, annotations) -> Pair.of(constructor.getParameterTypes()[i],
                        constructor.isAnnotationPresent(Inject.class) ?
                                Pair.of(true, Optional.<String>absent()) :
                                getInjectInfo(annotations)));
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

    private Optional<Pod> getPodForFill(final Pair<Class, Pair<Boolean, Optional<String>>> param, final Collection<Pod> pods)
    {
        final Class paramClass = param.fst;
        final Boolean hasInjectAnnotationOnParam = param.snd.fst;
        final Optional<String> beanNameForInject = param.snd.snd;

        if (hasInjectAnnotationOnParam) {
            final List<Pod> eligiblePods = copyOf(filter(pods, new Predicate<Pod>() {
                @Override
                public boolean apply(final Pod pod) {
                    if (beanNameForInject.isPresent()) {
                        return pod.hasName(beanNameForInject.get());
                    } else {
                        return paramClass.isAssignableFrom(pod.getBeanClass());
                    }
                }
            }));
            assertPodExists(paramClass, eligiblePods);
            assertNotMoreThanOnePod(paramClass, eligiblePods);
            return of(eligiblePods.get(0));
        } else {
            return absent();
        }
    }

    private boolean isNoParameters()
    {
        return constructor.getParameterTypes().length == 0;
    }

    private Pair<Boolean, Optional<String>> getInjectInfo(final Annotation[] annotations)
    {
        final List<Annotation> injectAnnotations = copyOf(filter(copyOf(annotations), new Predicate<Annotation>() {
            @Override
            public boolean apply(final Annotation annotation) {
                return annotation.annotationType().equals(Inject.class);
            }
        }));

        if (injectAnnotations.size() > 0) {
            return Pair.of(true, getInjectBeanName(injectAnnotations.get(0)));
        }

        return Pair.of(false, Optional.<String>absent());
    }

    private Optional<String> getInjectBeanName(final Annotation annotation)
    {
        return isNullOrEmpty(((Inject)annotation).value()) ?
                Optional.<String>absent() : of(((Inject) annotation).value());
    }
}
