package com.freeroom.di;

import com.freeroom.di.exceptions.ConstructorCycleDependencyException;
import com.google.common.base.Predicate;
import com.google.common.collect.Lists;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Stack;

import static com.google.common.collect.Collections2.filter;
import static com.google.common.collect.Lists.newArrayList;

class Injector
{
    private final Stack<SoyPod> waitingForConstruction = new Stack<>();
    private final Stack<SoyPod> waitingForPopulation = new Stack<>();
    private final Collection<Pod> pods;

    public Injector(final Collection<Pod> pods)
    {
        this.pods = pods;
    }

    public Collection<Pod> resolve()
    {
        final Collection<SoyPod> unreadyPods = findUnreadySoyPods(pods);
        resolveDependencyInjection(unreadyPods);
        return pods;
    }

    private void resolveDependencyInjection(final Collection<SoyPod> unreadyPods)
    {
        waitingForConstruction.addAll(unreadyPods);
        resolveConstructionInjection();
        resolveFieldAndSetterInjection();
    }

    private void resolveConstructionInjection()
    {
        while (!waitingForConstruction.isEmpty()) {
            final SoyPod pod = waitingForConstruction.pop();

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
            final SoyPod pod = waitingForPopulation.pop();
            populateFieldDependencies(pod);
            populateSetterDependencies(pod);
            pod.fosterBean();
        }
    }

    private void preparePodForPopulation(final SoyPod pod)
    {
        waitingForPopulation.push(pod);
    }

    private void prepareUnreadyPodsForConstruction(final SoyPod pod)
    {
        final ConstructorHole constructorHole = (ConstructorHole) pod.getConstructorHole().get();
        final Collection<SoyPod> unreadyPods = constructorHole.getUnreadyPods();

        waitingForConstruction.push(pod);
        for (final SoyPod unreadyPod : unreadyPods) {
            waitingForConstruction.push(unreadyPod);
            assertNoCycleDependency(pod, unreadyPod, waitingForConstruction);
        }
    }

    private void populateFieldDependencies(final SoyPod pod)
    {
        for (final FieldHole hole : pod.getFieldHoles()) {
            hole.fill(pods);
        }
    }

    private void populateSetterDependencies(final SoyPod pod)
    {
        for (final SetterHole hole : pod.getSetterHoles()) {
            hole.fill(pods);
        }
    }

    private void assertNoCycleDependency(final SoyPod pod, final SoyPod unreadyPod, final Stack<SoyPod> waitingForConstruction)
    {
        if (waitingForConstruction.indexOf(unreadyPod) > -1) {
            throw new ConstructorCycleDependencyException(
                    "Bean " + pod.getBeanName() + " and " + unreadyPod.getBeanName() +
                            " have constructor cycle dependencies."
            );
        }
    }

    private Collection<SoyPod> findUnreadySoyPods(final Collection<Pod> pods)
    {
        return filter(findSoyPods(pods), new Predicate<Pod>() {
            @Override
            public boolean apply(final Pod pod) {
                return !pod.isBeanReady();
            }
        });
    }

    private Collection<SoyPod> findSoyPods(final Collection<Pod> pods)
    {
        final List<SoyPod> soyPods = newArrayList();
        for (final Pod pod : pods) {
            if (pod instanceof SoyPod) {
                soyPods.add((SoyPod) pod);
            }
        }
        return soyPods;
    }
}
