package com.freeroom.di;

import com.freeroom.di.annotations.Inject;
import com.freeroom.di.util.Pair;
import com.google.common.base.Optional;

import java.lang.annotation.Annotation;
import java.lang.reflect.Constructor;
import java.util.Collection;
import java.util.List;

import static com.freeroom.di.util.FuncUtils.each;
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
        each(getParameters(), param -> {
            final Optional<Pod> pod = getPodForFill(param, pods);

            if (pod.isPresent()) {
                if (pod.get().isBeanReady()) {
                    readyBeans.add(pod.get().getBean().get());
                    unreadyPods.remove(pod.get());
                } else {
                    unreadyPods.add((SoyPod)pod.get());
                }
            } else {
                readyBeans.add(null);
            }
        });
    }

    private List<Pair<Class, Pair<Boolean, Optional<String>>>> getParameters()
    {
        return map(copyOf(constructor.getParameterAnnotations()),
                (i, annotations) -> Pair.of(constructor.getParameterTypes()[i],
                        constructor.isAnnotationPresent(Inject.class) ?
                                Pair.of(true, absent()) :
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

        Optional<Pod> eligiblePod = absent();
        if (hasInjectAnnotationOnParam) {
            final List<Pod> eligiblePods = copyOf(filter(pods, pod ->
                    beanNameForInject.isPresent() ?
                            pod.hasName(beanNameForInject.get()) :
                            paramClass.isAssignableFrom(pod.getBeanClass())));

            assertPodExists(paramClass, eligiblePods);
            assertNotMoreThanOnePod(paramClass, eligiblePods);
            eligiblePod = of(eligiblePods.get(0));
        }
        return eligiblePod;
    }

    private boolean isNoParameters()
    {
        return constructor.getParameterTypes().length == 0;
    }

    private Pair<Boolean, Optional<String>> getInjectInfo(final Annotation[] annotations)
    {
        final List<Annotation> injectAnnotations = copyOf(filter(copyOf(annotations),
                annotation -> annotation.annotationType().equals(Inject.class)));

        return injectAnnotations.size() > 0 ?
                Pair.of(true, getInjectBeanName((Inject) injectAnnotations.get(0))) :
                Pair.of(false, absent());
    }

    private Optional<String> getInjectBeanName(final Inject annotation)
    {
        return isNullOrEmpty(annotation.value()) ?
                absent() : of(annotation.value());
    }
}
