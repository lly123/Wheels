package com.freeroom.di;

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

    private void resolveConstructionInjection() {
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

    private void resolveFieldInjection() {
        while (!waitingForPopulation.isEmpty()) {
            Pod pod = waitingForPopulation.pop();
            populateFieldDependencies(pod);
        }
    }

    private void prepareUnreadyPodsForConstruction(Pod pod) {
        ConstructorHole constructorHole = (ConstructorHole) pod.getConstructorHole().get();
        Collection<Pod> unreadyPods = constructorHole.getUnreadyPods();

        waitingForConstruction.push(pod);
        for (final Pod unreadyPod : unreadyPods) {
            waitingForConstruction.push(unreadyPod);
            assertNoCycleDependency();
        }
    }

    private void preparePodForPopulateFields(Pod pod) {
        waitingForPopulation.push(pod);
    }

    private void assertNoCycleDependency()
    {
        //To change body of created methods use File | Settings | File Templates.
    }

    private void populateFieldDependencies(final Pod pod)
    {
        for (final FieldHole hole : pod.getFieldHoles()) {
            hole.fill(pods);
        }
        pod.populateBeanFields();
    }
}
