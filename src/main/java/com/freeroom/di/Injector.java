package com.freeroom.di;

import com.freeroom.di.exceptions.NoBeanException;
import com.freeroom.di.util.Function;
import com.google.common.base.Predicate;
import com.google.common.collect.Collections2;
import com.google.common.collect.Iterables;

import java.lang.reflect.Type;
import java.util.Collection;
import java.util.HashSet;
import java.util.Stack;

import static com.freeroom.di.util.Iterables.reduce;
import static com.google.common.collect.Iterables.all;
import static com.google.common.collect.Iterables.any;
import static com.google.common.collect.Iterables.find;
import static java.lang.String.format;

public class Injector
{
    private final Collection<Pod> pods;

    public Injector(final Collection<Pod> pods) {
        this.pods = pods;
    }

    public Collection<Pod> resolve() {
        assertDependenciesCanBeSatisfied();

        Stack<Pod> waitingForConstruction = new Stack<>();
        Stack<Pod> waitingForPopulation = new Stack<>();

        waitingForConstruction.addAll(pods);

        resolveDependencyInjection(waitingForConstruction, waitingForPopulation);

        return pods;
    }

    private void resolveDependencyInjection(
            final Stack<Pod> waitingForConstruction,
            final Stack<Pod> waitingForPopulation)
    {
        while (!waitingForConstruction.isEmpty()) {
            Pod pod = waitingForConstruction.pop();
            pod.createBeanWithDefaultConstructor();
            waitingForPopulation.push(pod);
        }

        while (!waitingForPopulation.isEmpty()) {
            Pod pod = waitingForPopulation.pop();
            populateDependencies(pod);
        }
    }

    private void populateDependencies(final Pod pod) {
        for (FieldHole hole : pod.getFieldHoles()) {
            hole.fill(findPodWithType(hole.getType()).getBean());
        }
        pod.populateFields();
    }

    private Pod findPodWithType(final Type type) {
        return find(pods, new Predicate<Pod>() {
            @Override
            public boolean apply(Pod pod) {
                return ((Class<?>)type).isAssignableFrom(pod.getBeanClass());
            }
        });
    }

    private void assertDependenciesCanBeSatisfied() {
        Collection<Type> dependencyTypes = getUniqueDependencyTypes();

        all(dependencyTypes, new Predicate<Type>() {
            @Override
            public boolean apply(final Type type) {
                if (!any(pods, new Predicate<Pod>() {
                    @Override
                    public boolean apply(Pod pod) {
                        return pod.getBeanClass().equals(type);
                    }
                })) {
                    throw new NoBeanException(format("Can't find bean of type %s in context.", type));
                }
                return true;
            }
        });
    }

    private Collection<Type> getUniqueDependencyTypes() {
        return reduce(new HashSet<Type>(), pods, new Function<HashSet<Type>, Pod>() {
            @Override
            public HashSet<Type> call(HashSet<Type> fieldTypes, Pod pod) {
                fieldTypes.addAll(toTypes(pod.getHoles()));
                return fieldTypes;
            }
        });
    }

    private Collection<Type> toTypes(final Collection<Hole> holes) {
        return Collections2.transform(holes, new com.google.common.base.Function<Hole, Type>() {
            @Override
            public Type apply(Hole hole) {
                return hole.getType();
            }
        });
    }
}
