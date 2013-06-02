package com.freeroom.di;

import com.freeroom.di.exceptions.ConstructorCycleDependencyException;

import java.util.Collection;
import java.util.Stack;

import static com.freeroom.util.FuncUtils.each;
import static com.google.common.collect.Collections2.filter;

class Injector
{
    private final Stack<Pod> waitingForConstruction = new Stack<>();
    private final Stack<SoyPod> waitingForPopulation = new Stack<>();
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

            if (pod instanceof PeaPod) {
                ((PeaPod)pod).constructBean();
            } else {
                ((SoyPod)pod).tryConstructBean(pods);

                if (pod.isBeanReady()) {
                    preparePodForPopulation((SoyPod)pod);
                } else {
                    prepareUnreadyPodsForConstruction((SoyPod)pod);
                }
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
        final ConstructorHole constructorHole = (ConstructorHole) pod.getConstructorHole();
        final Collection<SoyPod> unreadyPods = constructorHole.getUnreadyPods();

        waitingForConstruction.push(pod);
        each(unreadyPods, unreadyPod -> {
            assertNoCycleDependency(pod, unreadyPod, waitingForConstruction);
            waitingForConstruction.remove(unreadyPod);
            waitingForConstruction.push(unreadyPod);
        });
    }

    private void populateFieldDependencies(final SoyPod pod)
    {
        each(pod.getFieldHoles(), hole -> hole.fill(pods));
    }

    private void populateSetterDependencies(final SoyPod pod)
    {
        each(pod.getSetterHoles(), hole -> hole.fill(pods));
    }

    private void assertNoCycleDependency(final SoyPod pod, final SoyPod unreadyPod, final Stack<Pod> waitingForConstruction)
    {
        if (waitingForConstruction.contains(unreadyPod)) {
            if (((ConstructorHole)unreadyPod.getConstructorHole()).getUnreadyPods().contains(pod)) {
                throw new ConstructorCycleDependencyException(
                        "Bean " + pod.getBeanName() + " and " + unreadyPod.getBeanName() +
                                " have constructor cycle dependencies."
                );
            }
        }
    }

    private Collection<Pod> findUnreadyPods(final Collection<Pod> pods)
    {
        return filter(pods, pod -> !pod.isBeanReady());
    }
}
