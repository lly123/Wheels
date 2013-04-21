package com.freeroom.di;

import com.freeroom.di.annotations.Scope;
import com.freeroom.di.exceptions.NoBeanException;
import com.freeroom.di.exceptions.NotUniqueException;
import com.freeroom.test.beans.fieldInjection.Mosquito;
import com.freeroom.test.beans.fieldInjection.Squid;
import com.freeroom.test.beans.dummy.EmptyBean;
import com.freeroom.test.beans.fieldInjection.Hedgehog;
import com.freeroom.test.beans.constructorInjection.*;
import com.freeroom.test.beans.requiredScope.Otter;
import com.freeroom.test.beans.setterInjection.Camel;
import com.freeroom.test.beans.setterInjection.Leopard;
import com.freeroom.test.beans.setterInjection.Mackerels;
import org.junit.Before;
import org.junit.Test;

import java.util.Collection;

import static java.util.Arrays.asList;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

public class PodTest
{
    private Collection<Pod> podsPool;

    @Before
    public void setUp()
    {
        podsPool = generatePods();
    }

    @Test
    public void should_create_bean_by_default_constructor()
    {
        final Pod pod = new Pod(EmptyBean.class);

        pod.createBeanWithDefaultConstructor();

        assertThat(pod.getBean(), is(instanceOf(EmptyBean.class)));
    }

    @Test
    public void should_get_holes_of_field_type()
    {
        final Pod pod = new Pod(Hedgehog.class);

        assertThat(pod.getHoles().size(), is(1));
        assertThat(pod.getHoles().get(0), is(instanceOf(FieldHole.class)));

        final FieldHole hole = (FieldHole) pod.getHoles().get(0);
        assertThat(hole.getHoleClass().equals(Squid.class), is(true));
        assertThat(hole.isFilled(), is(false));
        assertThat(hole.getField(), is(notNullValue()));
    }

    @Test
    public void should_get_holes_of_constructor_type()
    {
        final Pod pod = new Pod(Pangolin.class);
        assertThat(pod.getHoles().size(), is(1));

        final Hole constructorHole = pod.getHoles().get(0);
        assertThat(constructorHole, is(instanceOf(ConstructorHole.class)));
        assertThat(constructorHole.isFilled(), is(false));
    }

    @Test
    public void should_get_holes_of_setter_type()
    {
        final Pod pod = new Pod(Camel.class);
        assertThat(pod.getHoles().size(), is(1));

        final Hole setterHole = pod.getHoles().get(0);
        assertThat(setterHole, is(instanceOf(SetterHole.class)));
        assertThat(setterHole.isFilled(), is(false));
    }

    @Test
    public void should_get_setter_hole_given_injection_method_begins_with_SET_prefix()
    {
        final Pod pod = new Pod(Mackerels.class);
        assertThat(pod.getHoles().size(), is(1));

        final SetterHole setterHole = (SetterHole) pod.getHoles().get(0);
        assertThat(setterHole.getMethod().getName(), is(startsWith("set")));
    }

    @Test(expected = NotUniqueException.class)
    public void should_throw_NotUniqueException_given_injection_method_has_more_than_one_parameter()
    {
        new Pod(Leopard.class);
    }

    @Test(expected = NotUniqueException.class)
    public void should_throw_NotUniqueException_given_injecting_two_constructors()
    {
        new Pod(Jellyfish.class);
    }

    @Test
    public void should_fill_hole_given_a_field_hole()
    {
        final Pod pod = new Pod(Hedgehog.class);

        final Hole fieldHole = pod.getHoles().get(0);
        fieldHole.fill(podsPool);

        assertThat(fieldHole.isFilled(), is(true));
    }

    @Test
    public void should_fill_holes_given_a_constructor_hole()
    {
        final Pod pod = new Pod(Pangolin.class);

        final Hole constructorHole = pod.getHoles().get(0);
        constructorHole.fill(podsPool);

        assertThat(constructorHole.isFilled(), is(true));
    }

    @Test
    public void should_fill_hole_given_a_setter_hole()
    {
        final Pod pod = new Pod(Camel.class);

        final Hole setterHole = pod.getHoles().get(0);
        setterHole.fill(podsPool);

        assertThat(setterHole.isFilled(), is(true));
    }

    @Test(expected = NoBeanException.class)
    public void should_throw_NoBeanException_given_no_bean_for_constructor_parameter()
    {
        final Pod pod = new Pod(Flamingo.class);

        final Hole constructorHole = pod.getHoles().get(0);
        constructorHole.fill(podsPool);
    }

    @Test(expected = NoBeanException.class)
    public void should_throw_NoBeanException_given_filling_bean_type_is_wrong()
    {
        final Pod pod = new Pod(Hedgehog.class);

        pod.getHoles().get(0).fill(asList(new Pod(Mosquito.class)));
    }

    @Test
    public void should_not_fill_constructor_holes_given_beans_not_ready()
    {
        final Pod pod = new Pod(Tarsier.class);

        final ConstructorHole hole = (ConstructorHole) pod.getHoles().get(0);
        hole.fill(studentPodIsUnready());

        assertThat(hole.isFilled(), is(false));
        assertThat(hole.getUnreadyPods().size(), is(1));

        final Pod unreadyPod = (Pod) hole.getUnreadyPods().toArray()[0];
        assertThat(unreadyPod.getBeanClass().equals(Pangolin.class), is(true));
    }

    @Test
    public void should_populate_bean_injection_fields()
    {
        final Pod pod = new Pod(Hedgehog.class);
        pod.getHoles().get(0).fill(podsPool);
        pod.createBeanWithDefaultConstructor();

        pod.fosterBean();

        assertThat(((Hedgehog)pod.getBean()).getSquid(), is(notNullValue()));
    }

    @Test
    public void should_inject_bean_by_setters()
    {
        final Pod pod = new Pod(Camel.class);
        pod.getHoles().get(0).fill(podsPool);
        pod.createBeanWithDefaultConstructor();

        pod.fosterBean();

        assertThat(((Camel)pod.getBean()).getMackerels(), is(notNullValue()));
    }

    @Test
    public void should_get_required_scope()
    {
        final Pod pod = new Pod(Otter.class);
        assertThat(pod.getScope(), is(Scope.Required));
    }

    private Collection<Pod> generatePods()
    {
        final Pod carPod = new Pod(Squid.class);
        carPod.createBeanWithDefaultConstructor();

        final Pod homePod = new Pod(Mosquito.class);
        homePod.createBeanWithDefaultConstructor();

        final Pod teacherPod = new Pod(Boa.class);
        teacherPod.createBeanWithDefaultConstructor();

        final Pod mackerelsPod = new Pod(Mackerels.class);
        mackerelsPod.createBeanWithDefaultConstructor();

        return asList(carPod, homePod, teacherPod, mackerelsPod);
    }

    private Collection<Pod> studentPodIsUnready()
    {
        final Pod teacherPod = new Pod(Boa.class);
        teacherPod.createBeanWithDefaultConstructor();

        final Pod studentPod = new Pod(Pangolin.class);

        return asList(teacherPod, studentPod);
    }
}
