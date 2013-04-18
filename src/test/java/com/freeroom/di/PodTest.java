package com.freeroom.di;

import com.freeroom.di.annotations.Scope;
import com.freeroom.di.exceptions.NoBeanException;
import com.freeroom.di.exceptions.NotUniqueException;
import com.freeroom.test.beans.fieldInjection.Car;
import com.freeroom.test.beans.EmptyBean;
import com.freeroom.test.beans.fieldInjection.Home;
import com.freeroom.test.beans.fieldInjection.Person;
import com.freeroom.test.beans.constructorInjection.*;
import com.freeroom.test.beans.requiredScope.Otter;
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
        Pod pod = new Pod(EmptyBean.class);

        pod.createBeanWithDefaultConstructor();

        assertThat(pod.getBean(), is(instanceOf(EmptyBean.class)));
    }

    @Test
    public void should_get_holes_of_field_type()
    {
        Pod pod = new Pod(Person.class);

        assertThat(pod.getHoles().size(), is(1));
        assertThat(pod.getHoles().get(0), is(instanceOf(FieldHole.class)));

        FieldHole hole = (FieldHole) pod.getHoles().get(0);
        assertThat(hole.getHoleClass().equals(Car.class), is(true));
        assertThat(hole.isFilled(), is(false));
        assertThat(hole.getField(), is(notNullValue()));
    }

    @Test
    public void should_get_holes_of_constructor_type()
    {
        Pod pod = new Pod(Student.class);
        assertThat(pod.getHoles().size(), is(1));

        Hole constructorHole = pod.getHoles().get(0);
        assertThat(constructorHole, is(instanceOf(ConstructorHole.class)));
        assertThat(constructorHole.isFilled(), is(false));
    }

    @Test(expected = NotUniqueException.class)
    public void should_throw_NotUniqueException_given_injecting_two_constructors()
    {
        new Pod(TwoConstructorsInjection.class);
    }

    @Test
    public void should_fill_hole_given_a_field_hole()
    {
        Pod pod = new Pod(Person.class);

        Hole fieldHole = pod.getHoles().get(0);
        fieldHole.fill(podsPool);

        assertThat(fieldHole.isFilled(), is(true));
    }

    @Test
    public void should_fill_holes_given_a_constructor_hole()
    {
        Pod pod = new Pod(Student.class);

        Hole constructorHole = pod.getHoles().get(0);
        constructorHole.fill(podsPool);

        assertThat(constructorHole.isFilled(), is(true));
    }

    @Test(expected = NoBeanException.class)
    public void should_throw_NoBeanException_given_no_bean_for_constructor_parameter()
    {
        Pod pod = new Pod(NoBeanForConstructor.class);

        Hole constructorHole = pod.getHoles().get(0);
        constructorHole.fill(podsPool);
    }

    @Test(expected = NoBeanException.class)
    public void should_throw_NoBeanException_given_filling_bean_type_is_wrong()
    {
        Pod pod = new Pod(Person.class);

        pod.getHoles().get(0).fill(asList(new Pod(Home.class)));
    }

    @Test
    public void should_not_fill_constructor_holes_given_beans_not_ready()
    {
        Pod pod = new Pod(FamilyStudy.class);

        ConstructorHole hole = (ConstructorHole) pod.getHoles().get(0);
        hole.fill(studentPodIsUnready());

        assertThat(hole.isFilled(), is(false));
        assertThat(hole.getUnreadyPods().size(), is(1));

        Pod unreadyPod = (Pod) hole.getUnreadyPods().toArray()[0];
        assertThat(unreadyPod.getBeanClass().equals(Student.class), is(true));
    }

    @Test
    public void should_populate_bean_injection_fields()
    {
        Pod pod = new Pod(Person.class);
        pod.getHoles().get(0).fill(podsPool);
        pod.createBeanWithDefaultConstructor();

        pod.populateBeanFields();

        assertThat(((Person)pod.getBean()).getCar(), is(notNullValue()));
    }

    @Test
    public void should_get_required_scope()
    {
        Pod pod = new Pod(Otter.class);
        assertThat(pod.getScope(), is(Scope.Required));
    }

    private Collection<Pod> generatePods()
    {
        Pod carPod = new Pod(Car.class);
        carPod.createBeanWithDefaultConstructor();

        Pod homePod = new Pod(Home.class);
        homePod.createBeanWithDefaultConstructor();

        Pod teacherPod = new Pod(Teacher.class);
        teacherPod.createBeanWithDefaultConstructor();

        return asList(carPod, homePod, teacherPod);
    }

    private Collection<Pod> studentPodIsUnready()
    {
        Pod teacherPod = new Pod(Teacher.class);
        teacherPod.createBeanWithDefaultConstructor();

        Pod studentPod = new Pod(Student.class);

        return asList(teacherPod, studentPod);
    }
}
