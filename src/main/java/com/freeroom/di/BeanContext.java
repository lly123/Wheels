package com.freeroom.di;

import com.freeroom.di.exceptions.NotUniqueException;
import com.google.common.base.Function;
import com.google.common.base.Optional;
import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;

import java.util.ArrayList;
import java.util.List;

import static com.google.common.base.Optional.absent;
import static com.google.common.base.Optional.fromNullable;
import static com.google.common.base.Optional.of;
import static com.google.common.collect.Iterables.*;
import static com.google.common.collect.Lists.newArrayList;

public class BeanContext
{
    private static BeanContext context;

    final Package beanPackage;
    private List<Pod> pods = new ArrayList<>();

    private BeanContext(String packageName) {
        this.beanPackage = new Package(packageName);
    }

    public static BeanContext load(String packageName) {
        context = new BeanContext(packageName);
        context.initialize();

        return context;
    }

    private void initialize() {
        try {
            this.pods = beanPackage.getPods();
        } catch (Exception ignored) {}
    }

    public List<Object> getBeans() {
        return newArrayList(transform(pods, new Function<Pod, Object>() {
            @Override
            public Object apply(com.freeroom.di.Pod pod) {
                return pod.getBean();
            }
        }));
    }

    public <T> Optional<T> getBean(final Class<T> clazz) {
        List<Object> beans = newArrayList(getBeanCanBeAssignedTo(clazz));

        if (beans.size() > 1) {
            throw new NotUniqueException("More than one bean is assignable to: " + clazz.getName());
        }

        Optional<Object> retVal = absent();
        if (beans.size() == 1) {
            retVal = of(beans.get(0));
        }

        return (Optional<T>) retVal;
    }

    private <T> Iterable<Object> getBeanCanBeAssignedTo(final Class<T> clazz) {
        return filter(getBeans(), new Predicate<Object>() {
            @Override
            public boolean apply(Object bean) {
                return clazz.isAssignableFrom(bean.getClass());
            }
        });
    }

    public Optional<Object> getBean(final String name) {
        return tryFind(pods, new Predicate<Pod>() {
            @Override
            public boolean apply(Pod pod) {
                return pod.getBeanName().equals(name);
            }
        }).transform(new Function<Pod, Object>() {
            @Override
            public Object apply(Pod pod) {
                return pod.getBean();
            }
        });
    }
}
