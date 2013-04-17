package com.freeroom.di;

import com.freeroom.test.beans.Car;
import com.freeroom.test.beans.Home;
import com.freeroom.test.beans.Person;
import org.junit.Test;

import java.util.List;

import static java.util.Arrays.asList;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;

public class PodTest
{
    @Test
    public void should_get_holes_of_field_type() {
        Pod pod = new Pod(Person.class);

        assertThat(pod.getHoles().size(), is(1));
        assertThat(pod.getHoles().get(0), is(instanceOf(FieldHole.class)));

        FieldHole hole = (FieldHole) pod.getHoles().get(0);
        assertThat(hole.getHoleClass().equals(Car.class), is(true));
        assertThat(hole.isFilled(), is(false));
        assertThat(hole.getField(), is(notNullValue()));
    }

//    @Test
//    public void should_get_holes_of_constructor_type() {
//        Pod pod = new Pod(Student.class);
//        assertThat(pod.getHoles().size(), is(1));
//        assertThat(pod.getHoles().get(0).getType().equals(Teacher.class), is(true));
//        assertThat(pod.getHoles().get(0), is(instanceOf(ConstructorHole.class)));
//        assertThat(pod.getHoles().get(0).isFilled(), is(false));
//    }

    @Test
    public void should_fill_hole_given_a_field_hole() {
        Pod pod = new Pod(Person.class);

        pod.getHoles().get(0).fill(getDependentPod());

        assertThat(pod.getHoles().get(0).isFilled(), is(true));
    }

    @Test(expected = ClassCastException.class)
    public void should_throw_ClassCastException_given_filling_bean_type_is_wrong() {
        Pod pod = new Pod(Person.class);

        pod.getHoles().get(0).fill(asList(new Pod(Home.class)));
    }

    @Test
    public void should_populate_bean_injection_fields() {
        Pod pod = new Pod(Person.class);
        pod.getHoles().get(0).fill(getDependentPod());
        pod.createBeanWithDefaultConstructor();

        pod.populateFields();

        assertThat(((Person)pod.getBean()).getCar(), is(notNullValue()));
    }

    private List<Pod> getDependentPod() {
        Pod dependentPod = new Pod(Car.class);
        dependentPod.createBeanWithDefaultConstructor();
        return asList(dependentPod);
    }
}
