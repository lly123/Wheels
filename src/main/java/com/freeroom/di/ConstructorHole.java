package com.freeroom.di;

import com.freeroom.di.annotations.Inject;
import com.freeroom.di.util.Func2;
import com.google.common.base.Optional;
import com.google.common.base.Predicate;
import com.google.common.collect.ImmutableList;
import com.sun.tools.javac.util.Pair;

import java.lang.annotation.Annotation;
import java.lang.reflect.Constructor;
import java.util.Collection;
import java.util.List;

import static com.freeroom.di.util.FuncUtils.mapWithIndex;
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
        return mapWithIndex(ImmutableList.<Annotation[]>copyOf(constructor.getParameterAnnotations()),
                new Func2<Integer, Annotation[], Pair<Class, Pair<Boolean, Optional<String>>>>() {
                    @Override
                    public Pair<Class, Pair<Boolean, Optional<String>>> call(Integer i, Annotation[] annotations) {
                        return Pair.of(constructor.getParameterTypes()[i], getInjectInfo(annotations));
                    }
                });
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
        if (param.snd.fst) {
            final List<Pod> eligiblePods = copyOf(filter(pods, new Predicate<Pod>() {
                @Override
                public boolean apply(final Pod pod) {
                    if (param.snd.snd.isPresent()) {
                        return pod.hasName(param.snd.snd.get());
                    } else {
                        return param.fst.isAssignableFrom(pod.getBeanClass());
                    }
                }
            }));
            assertPodExists(param.fst, eligiblePods);
            assertNotMoreThanOnePod(param.fst, eligiblePods);
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
