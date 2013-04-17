package com.freeroom.di;

import com.freeroom.di.exceptions.NoBeanException;
import com.freeroom.test.beans.Car;
import com.freeroom.test.beans.Home;
import com.freeroom.test.beans.Person;
import com.freeroom.test.beans.constrcutorInjection.NoBeanForConstructor;
import com.freeroom.test.beans.constrcutorInjection.Student;
import com.freeroom.test.beans.constrcutorInjection.Teacher;
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

    @Test
    public void should_throw_NotUniqueException_given_injecting_two_constructors()
    {
    }

    @Test
    public void should_fill_hole_given_a_field_hole()
    {
        Pod pod = new Pod(Person.class);

        Hole fieldHole = pod.getHoles().get(0);
        fieldHole.fill(podsPool);

        assertThat(fieldHole.isFilled(), is(true));
    }

    @Test(expected = NoBeanException.class)
    public void should_throw_NoBeanException_given_no_bean_for_constructor_parameter()
    {
        Pod pod = new Pod(NoBeanForConstructor.class);

        Hole constructorHole = pod.getHoles().get(0);
        constructorHole.fill(podsPool);
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
    public void should_throw_NoBeanException_given_filling_bean_type_is_wrong()
    {
        Pod pod = new Pod(Person.class);

        pod.getHoles().get(0).fill(asList(new Pod(Home.class)));
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
}
