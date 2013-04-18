package com.freeroom.di;

import com.freeroom.di.annotations.Scope;
import com.freeroom.di.exceptions.NotUniqueException;
import com.google.common.base.Function;
import com.google.common.base.Optional;
import com.google.common.base.Predicate;

import java.util.Collection;

import static com.google.common.base.Optional.absent;
import static com.google.common.base.Optional.of;
import static com.google.common.collect.Collections2.filter;
import static com.google.common.collect.Collections2.transform;
import static com.google.common.collect.Iterables.tryFind;

public class BeanContext
{
    private final Package beanPackage;

    public static BeanContext load(final String packageName)
    {
        return new BeanContext(packageName);
    }

    private BeanContext(final String packageName)
    {
        beanPackage = new Package(packageName);
        podsInjection();
    }

    public Collection<?> getBeans()
    {
        podsInjection();
        return transform(beanPackage.getPods(), new Function<Pod, Object>() {
            @Override
            public Object apply(final Pod pod) {
                return pod.getBean();
            }
        });
    }

    public Optional<?> getBean(final Class<?> clazz)
    {
        final Collection<?> beans = getBeanCanBeAssignedTo(clazz);

        assertNotMoreThanOneBean(clazz, beans);

        return ((beans.size() == 1) ? of(beans.toArray()[0]) : absent());
    }

    public Optional<?> getBean(final String name)
    {
        podsInjection();
        return tryFind(beanPackage.getPods(), new Predicate<Pod>() {
            @Override
            public boolean apply(final Pod pod) {
                return pod.getBeanName().equals(name);
            }
        }).transform(new Function<Pod, Object>() {
            @Override
            public Object apply(final Pod pod) {
                return pod.getBean();
            }
        });
    }

    private Collection<?> getBeanCanBeAssignedTo(final Class<?> clazz)
    {
        return filter(getBeans(), new Predicate<Object>() {
            @Override
            public boolean apply(final Object bean) {
                return clazz.isAssignableFrom(bean.getClass());
            }
        });
    }

    private void assertNotMoreThanOneBean(final Class<?> clazz, final Collection<?> beans)
    {
        if (beans.size() > 1) {
            throw new NotUniqueException("More than one bean can be assigned to: " + clazz);
        }
    }

    private void podsInjection()
    {
        cleanRequiredScopeBeans();
        new Injector(beanPackage).resolve();
    }

    private void cleanRequiredScopeBeans()
    {
        for (Pod pod : beanPackage.getPods()) {
            if (pod.getScope().equals(Scope.Required)) {
                pod.removeBean();
            }
        }
    }
}
