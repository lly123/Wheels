package com.freeroom.di;

import com.freeroom.di.util.Action;
import com.freeroom.di.util.FuncUtils;
import com.google.common.base.Optional;

import java.util.Collection;
import java.util.Stack;

import static com.freeroom.di.util.FuncUtils.each;

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
        while (!waitingForConstruction.isEmpty()) {
            Pod pod = waitingForConstruction.pop();

            Optional<Hole> constructorHole = pod.getConstructorHole();
            if (constructorHole.isPresent()) {
                resolveBeanConstruction(pod, constructorHole);
            } else {
                pod.createBeanWithDefaultConstructor();
                waitingForPopulation.push(pod);
            }
        }

        while (!waitingForPopulation.isEmpty()) {
            Pod pod = waitingForPopulation.pop();
            populateFieldDependencies(pod);
        }
    }

    private void resolveBeanConstruction(Pod pod, Optional<Hole> constructorHole)
    {
        ConstructorHole hole = (ConstructorHole) constructorHole.get();
        hole.fill(pods);
        if (hole.isFilled()) {
            pod.createBean(hole);
            waitingForPopulation.push(pod);
        } else {
            Collection<Pod> unreadyPods = hole.getUnreadyPods();
            waitingForConstruction.push(pod);

            for (Pod unreadyPod : unreadyPods) {
                waitingForConstruction.push(unreadyPod);
                assertNoCycleDependency();
            }
        }
    }

    private void assertNoCycleDependency()
    {
        //To change body of created methods use File | Settings | File Templates.
    }

    private void populateFieldDependencies(final Pod pod)
    {
        each(pod.getFieldHoles(), new Action<FieldHole>() {
            @Override
            public void call(final FieldHole hole) {
                hole.fill(pods);
            }
        });
        pod.populateBeanFields();
    }
}
