package com.freeroom.di;

import com.freeroom.test.beans.Car;
import com.freeroom.test.beans.Home;
import com.freeroom.test.beans.Person;
import org.junit.Test;

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
        assertThat(pod.getHoles().get(0).getType().equals(Car.class), is(true));
        assertThat(pod.getHoles().get(0), is(instanceOf(FieldHole.class)));
        assertThat(pod.getHoles().get(0).isFilled(), is(false));
        assertThat(((FieldHole)pod.getHoles().get(0)).getField(), is(notNullValue()));
    }

    @Test
    public void should_fill_hole_given_a_field_hole() {
        Pod pod = new Pod(Person.class);
        pod.getHoles().get(0).fill(new Car());
        assertThat(pod.getHoles().get(0).isFilled(), is(true));
    }

    @Test(expected = ClassCastException.class)
    public void should_throw_ClassCastException_given_filling_bean_type_is_wrong() {
        Pod pod = new Pod(Person.class);
        pod.getHoles().get(0).fill(new Home());
    }
}
