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

import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import static java.util.Arrays.asList;
import static java.util.Collections.EMPTY_LIST;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

public class SoyPodTest
{
    private Collection<Pod> podsPool;

    @Before
    public void setUp()
    {
        podsPool = generateSoyPods();
    }

    @Test
    public void should_create_bean_by_default_constructor()
    {
        final SoyPod pod = new SoyPod(EmptyBean.class);

        pod.tryConstructBean(EMPTY_LIST);

        assertThat(pod.getBean(), is(instanceOf(EmptyBean.class)));
    }

    @Test
    public void should_get_holes_of_field_type()
    {
        final SoyPod pod = new SoyPod(Hedgehog.class);

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
        final SoyPod pod = new SoyPod(Pangolin.class);
        assertThat(pod.getHoles().size(), is(1));

        final Hole constructorHole = pod.getHoles().get(0);
        assertThat(constructorHole, is(instanceOf(ConstructorHole.class)));
        assertThat(constructorHole.isFilled(), is(false));
    }

    @Test
    public void should_get_holes_of_setter_type()
    {
        final SoyPod pod = new SoyPod(Camel.class);
        assertThat(pod.getHoles().size(), is(1));

        final Hole setterHole = pod.getHoles().get(0);
        assertThat(setterHole, is(instanceOf(SetterHole.class)));
        assertThat(setterHole.isFilled(), is(false));
    }

    @Test
    public void should_get_setter_hole_given_injection_method_begins_with_SET_prefix()
    {
        final SoyPod pod = new SoyPod(Mackerels.class);
        assertThat(pod.getHoles().size(), is(1));

        final SetterHole setterHole = (SetterHole) pod.getHoles().get(0);
        assertThat(setterHole.getMethod().getName(), is(startsWith("set")));
    }

    @Test(expected = NotUniqueException.class)
    public void should_throw_NotUniqueException_given_injection_method_has_more_than_one_parameter()
    {
        new SoyPod(Leopard.class);
    }

    @Test(expected = NotUniqueException.class)
    public void should_throw_NotUniqueException_given_injecting_two_constructors()
    {
        new SoyPod(Jellyfish.class);
    }

    @Test
    public void should_fill_hole_given_a_field_hole()
    {
        final SoyPod pod = new SoyPod(Hedgehog.class);

        final Hole fieldHole = pod.getHoles().get(0);
        fieldHole.fill(podsPool);

        assertThat(fieldHole.isFilled(), is(true));
    }

    @Test
    public void should_fill_holes_given_a_constructor_hole()
    {
        final SoyPod pod = new SoyPod(Pangolin.class);

        final Hole constructorHole = pod.getHoles().get(0);
        constructorHole.fill(podsPool);

        assertThat(constructorHole.isFilled(), is(true));
    }

    @Test
    public void should_fill_hole_given_a_setter_hole()
    {
        final SoyPod pod = new SoyPod(Camel.class);

        final Hole setterHole = pod.getHoles().get(0);
        setterHole.fill(podsPool);

        assertThat(setterHole.isFilled(), is(true));
    }

    @Test(expected = NoBeanException.class)
    public void should_throw_NoBeanException_given_no_bean_for_constructor_parameter()
    {
        final SoyPod pod = new SoyPod(Flamingo.class);

        final Hole constructorHole = pod.getHoles().get(0);
        constructorHole.fill(podsPool);
    }

    @Test(expected = NoBeanException.class)
    public void should_throw_NoBeanException_given_filling_bean_type_is_wrong()
    {
        final SoyPod pod = new SoyPod(Hedgehog.class);

        pod.getHoles().get(0).fill(Arrays.<Pod>asList(new SoyPod(Mosquito.class)));
    }

    @Test
    public void should_not_fill_constructor_holes_given_beans_not_ready()
    {
        final SoyPod pod = new SoyPod(Tarsier.class);

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
        final SoyPod pod = new SoyPod(Hedgehog.class);
        pod.getHoles().get(0).fill(podsPool);
        pod.tryConstructBean(EMPTY_LIST);

        pod.fosterBean();

        assertThat(((Hedgehog)pod.getBean()).getSquid(), is(notNullValue()));
    }

    @Test
    public void should_inject_bean_by_setters()
    {
        final SoyPod pod = new SoyPod(Camel.class);
        pod.getHoles().get(0).fill(podsPool);
        pod.tryConstructBean(EMPTY_LIST);

        pod.fosterBean();

        assertThat(((Camel)pod.getBean()).getMackerels(), is(notNullValue()));
    }

    @Test
    public void should_get_required_scope()
    {
        final Pod pod = new SoyPod(Otter.class);
        assertThat(pod.getScope(), is(Scope.Required));
    }

    private List<Pod> generateSoyPods()
    {
        final Pod squidPod = new SoyPod(Squid.class);
        ((SoyPod)squidPod).tryConstructBean(EMPTY_LIST);

        final Pod mosquitoPod = new SoyPod(Mosquito.class);
        ((SoyPod)mosquitoPod).tryConstructBean(EMPTY_LIST);

        final Pod boaPod = new SoyPod(Boa.class);
        ((SoyPod)boaPod).tryConstructBean(EMPTY_LIST);

        final Pod mackerelsPod = new SoyPod(Mackerels.class);
        ((SoyPod)mackerelsPod).tryConstructBean(EMPTY_LIST);

        return asList(squidPod, mosquitoPod, boaPod, mackerelsPod);
    }

    private Collection<Pod> studentPodIsUnready()
    {
        final Pod boaPod = new SoyPod(Boa.class);
        ((SoyPod)boaPod).tryConstructBean(EMPTY_LIST);

        final Pod pangolinPod = new SoyPod(Pangolin.class);

        return asList(boaPod, pangolinPod);
    }
}
