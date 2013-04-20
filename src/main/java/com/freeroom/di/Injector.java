package com.freeroom.di;

import com.freeroom.di.exceptions.ConstructorCycleDependencyException;
import com.google.common.base.Predicate;

import java.util.Collection;
import java.util.Stack;

import static com.google.common.collect.Collections2.filter;

class Injector
{
    private final Stack<Pod> waitingForConstruction = new Stack<>();
    private final Stack<Pod> waitingForPopulation = new Stack<>();
    private final Collection<Pod> pods;

    public Injector(final Collection<Pod> pods)
    {
        this.pods = pods;
    }

    public Collection<Pod> resolve()
    {
        final Collection<Pod> unreadyPods = findUnreadyPods(pods);
        resolveDependencyInjection(unreadyPods);
        return pods;
    }

    private void resolveDependencyInjection(final Collection<Pod> unreadyPods)
    {
        waitingForConstruction.addAll(unreadyPods);
        resolveConstructionInjection();
        resolveFieldAndSetterInjection();
    }

    private void resolveConstructionInjection()
    {
        while (!waitingForConstruction.isEmpty()) {
            final Pod pod = waitingForConstruction.pop();

            pod.tryConstructBean(pods);
            if (pod.isBeanReady()) {
                preparePodForPopulation(pod);
            } else {
                prepareUnreadyPodsForConstruction(pod);
            }
        }
    }

    private void resolveFieldAndSetterInjection()
    {
        while (!waitingForPopulation.isEmpty()) {
            final Pod pod = waitingForPopulation.pop();
            populateFieldDependencies(pod);
            populateSetterDependencies(pod);
            pod.fosterBean();
        }
    }

    private void preparePodForPopulation(final Pod pod)
    {
        waitingForPopulation.push(pod);
    }

    private void prepareUnreadyPodsForConstruction(final Pod pod)
    {
        final ConstructorHole constructorHole = (ConstructorHole) pod.getConstructorHole().get();
        final Collection<Pod> unreadyPods = constructorHole.getUnreadyPods();

        waitingForConstruction.push(pod);
        for (final Pod unreadyPod : unreadyPods) {
            waitingForConstruction.push(unreadyPod);
            assertNoCycleDependency(pod, unreadyPod, waitingForConstruction);
        }
    }

    private void populateFieldDependencies(final Pod pod)
    {
        for (final FieldHole hole : pod.getFieldHoles()) {
            hole.fill(pods);
        }
    }

    private void populateSetterDependencies(final Pod pod)
    {
        for (final SetterHole hole : pod.getSetterHoles()) {
            hole.fill(pods);
        }
    }

    private void assertNoCycleDependency(final Pod pod, final Pod unreadyPod, final Stack<Pod> waitingForConstruction)
    {
        if (waitingForConstruction.indexOf(unreadyPod) > -1) {
            throw new ConstructorCycleDependencyException(
                    "Bean " + pod.getBeanName() + " and " + unreadyPod.getBeanName() +
                            " have constructor cycle dependencies."
            );
        }
    }

    private Collection<Pod> findUnreadyPods(final Collection<Pod> pods)
    {
        return filter(pods, new Predicate<Pod>() {
            @Override
            public boolean apply(final Pod pod) {
                return !pod.isBeanReady();
            }
        });
    }
}
