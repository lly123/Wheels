package com.freeroom.di;

import com.freeroom.di.annotations.Scope;
import com.freeroom.di.exceptions.NotUniqueException;
import com.freeroom.di.util.Func;
import com.google.common.base.Function;
import com.google.common.base.Optional;
import com.google.common.base.Predicate;

import java.util.Collection;
import java.util.List;

import static com.freeroom.di.util.FuncUtils.reduce;
import static com.google.common.base.Optional.absent;
import static com.google.common.base.Optional.of;
import static com.google.common.collect.Collections2.filter;
import static com.google.common.collect.Collections2.transform;
import static com.google.common.collect.ImmutableList.copyOf;
import static com.google.common.collect.Lists.newArrayList;

public class BeanContext
{
    private final Optional<BeanContext> parentContext;
    private final Package beanPackage;

    public static BeanContext load(final String packageName)
    {
        return new BeanContext(packageName);
    }

    public static BeanContext load(final String packageName, final BeanContext parentContext)
    {
        return new BeanContext(packageName, parentContext);
    }

    private BeanContext(final String packageName)
    {
        this.parentContext = absent();
        this.beanPackage = new Package(packageName);
    }

    private BeanContext(final String packageName, final BeanContext parentContext)
    {
        this.parentContext = of(parentContext);
        this.beanPackage = new Package(packageName);
    }

    public Collection<?> getBeans()
    {
        makePodsReady();
        return transform(beanPackage.getPods(), new Function<Pod, Object>() {
            @Override
            public Object apply(final Pod pod) {
                return pod.getBean().get();
            }
        });
    }

    public Optional<?> getBean(final Class<?> clazz)
    {
        final Collection<?> beans = getBeansCanBeAssignedTo(clazz);

        assertNotMoreThanOneBean(clazz, beans);

        return ((beans.size() == 1) ? of(beans.toArray()[0]) : absent());
    }

    public Optional<?> getBean(final String name)
    {
        makePodsReady();

        final Collection<Pod> pods = getPodsHaveName(name);
        assertNotMoreThanOnePod(name, pods);

        return ((pods.size() == 1) ? ((Pod)pods.toArray()[0]).getBean() : absent());
    }

    Collection<Pod> getPods()
    {
        return beanPackage.getPods();
    }

    private void makePodsReady()
    {
        cleanRequiredScopeBeans();
        new Injector(preparePodsForInjection()).resolve();
    }

    private Collection<Pod> preparePodsForInjection()
    {
        List<Pod> parentPods = newArrayList(beanPackage.getPods());
        Optional<BeanContext> context = this.parentContext;
        while (context.isPresent()) {
            parentPods = excludeDuplicatedPods(parentPods, context.get().getPods());
            context = context.get().parentContext;
        }
        return parentPods;
    }

    private List<Pod> excludeDuplicatedPods(final List<Pod> pods, final Collection<Pod> parentContextPods)
    {
        return reduce(pods, parentContextPods, (selectedPods, pod) -> {
            if (!selectedPods.contains(pod)) {
                selectedPods.add(pod);
            }
            return selectedPods;
        });
    }

    private Collection<?> getBeansCanBeAssignedTo(final Class<?> clazz)
    {
        return filter(getBeans(), new Predicate<Object>() {
            @Override
            public boolean apply(final Object bean) {
                return clazz.isAssignableFrom(bean.getClass());
            }
        });
    }

    private Collection<Pod> getPodsHaveName(final String name)
    {
        return filter(beanPackage.getPods(), new Predicate<Pod>() {
            @Override
            public boolean apply(final Pod pod) {
                return pod.hasName(name);
            }
        });
    }

    private void assertNotMoreThanOneBean(final Class<?> clazz, final Collection<?> beans)
    {
        if (beans.size() > 1) {
            throw new NotUniqueException("More than one bean can be assigned to: " + clazz);
        }
    }

    private void assertNotMoreThanOnePod(final String name, final Collection<Pod> pods)
    {
        if (pods.size() > 1) {
            throw new NotUniqueException("More than one bean have name: " + name);
        }
    }

    private void cleanRequiredScopeBeans()
    {
        for (final Pod pod : beanPackage.getPods()) {
            if (pod.getScope().equals(Scope.Required)) {
                pod.removeBean();
            }
        }
    }
}
