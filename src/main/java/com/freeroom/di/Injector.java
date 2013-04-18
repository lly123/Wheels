package com.freeroom.di;

import com.freeroom.di.exceptions.ConstructorCycleDependencyException;

import java.util.Collection;
import java.util.Stack;

class Injector
{
    private final Collection<Pod> pods;
    private final Stack<Pod> waitingForConstruction = new Stack<>();
    private final Stack<Pod> waitingForPopulation = new Stack<>();

    public Injector(final Collection<Pod> pods)
    {
        this.pods = pods;
    }

    public Collection<Pod> resolve()
    {
        waitingForConstruction.addAll(pods);
        resolveDependencyInjection();
        return pods;
    }

    private void resolveDependencyInjection()
    {
        resolveConstructionInjection();
        resolveFieldInjection();
    }

    private void resolveConstructionInjection()
    {
        while (!waitingForConstruction.isEmpty()) {
            Pod pod = waitingForConstruction.pop();

            pod.tryConstructBean(pods);
            if (pod.isBeanConstructed()) {
                preparePodForPopulateFields(pod);
            } else {
                prepareUnreadyPodsForConstruction(pod);
            }
        }
    }

    private void resolveFieldInjection()
    {
        while (!waitingForPopulation.isEmpty()) {
            Pod pod = waitingForPopulation.pop();
            populateFieldDependencies(pod);
        }
    }

    private void preparePodForPopulateFields(final Pod pod)
    {
        waitingForPopulation.push(pod);
    }

    private void prepareUnreadyPodsForConstruction(final Pod pod)
    {
        ConstructorHole constructorHole = (ConstructorHole) pod.getConstructorHole().get();
        Collection<Pod> unreadyPods = constructorHole.getUnreadyPods();

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
        pod.populateBeanFields();
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
}
