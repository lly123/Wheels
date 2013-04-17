package com.freeroom.di;

import com.freeroom.test.beans.Car;
import com.freeroom.test.beans.Person;
import org.junit.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

public class PodTest
{
    @Test
    public void should_get_holes_of_field_type() {
        Pod pod = new Pod(Person.class);
        assertThat(pod.getHoles().size(), is(1));
        assertThat(pod.getHoles().get(0).getType().equals(Car.class), is(true));
        assertThat(pod.getHoles().get(0).getHoleType(), is(HoleType.FIELD));
        assertThat(pod.getHoles().get(0).isFilled(), is(false));
    }
}
