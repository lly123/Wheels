package com.freeroom.di;

import com.freeroom.di.exceptions.NotUniqueException;
import com.google.common.base.Function;
import com.google.common.base.Optional;
import com.google.common.base.Predicate;
import com.google.common.collect.Collections2;

import java.util.Collection;
import java.util.List;

import static com.google.common.base.Optional.absent;
import static com.google.common.base.Optional.of;
import static com.google.common.collect.Collections2.filter;
import static com.google.common.collect.Collections2.transform;
import static com.google.common.collect.Iterables.*;
import static com.google.common.collect.Lists.newArrayList;

public class BeanContext
{
    private static BeanContext context;
    private final Package beanPackage;

    public static BeanContext load(final String packageName) {
        context = new BeanContext(packageName);
        return context;
    }

    public Collection<Object> getBeans() {
        return transform(beanPackage.getPods(), new Function<Pod, Object>() {
            @Override
            public Object apply(com.freeroom.di.Pod pod) {
                pod.createBeanWithDefaultConstructor();
                return pod.getBean();
            }
        });
    }

    public <T> Optional<T> getBean(final Class<T> clazz) {
        Collection<Object> beans = getBeanCanBeAssignedTo(clazz);

        if (beans.size() > 1) {
            throw new NotUniqueException("More than one bean is assignable to: " + clazz.getName());
        }

        Optional<Object> retVal = absent();
        if (beans.size() == 1) {
            retVal = of(beans.toArray()[0]);
        }

        return (Optional<T>) retVal;
    }

    public Optional<Object> getBean(final String name) {
        return tryFind(beanPackage.getPods(), new Predicate<Pod>() {
            @Override
            public boolean apply(Pod pod) {
                return pod.getBeanName().equals(name);
            }
        }).transform(new Function<Pod, Object>() {
            @Override
            public Object apply(Pod pod) {
                pod.createBeanWithDefaultConstructor();
                return pod.getBean();
            }
        });
    }

    private BeanContext(final String packageName) {
        this.beanPackage = new Package(packageName);
    }

    private <T> Collection<Object> getBeanCanBeAssignedTo(final Class<T> clazz) {
        return filter(getBeans(), new Predicate<Object>() {
            @Override
            public boolean apply(Object bean) {
                return clazz.isAssignableFrom(bean.getClass());
            }
        });
    }
}
