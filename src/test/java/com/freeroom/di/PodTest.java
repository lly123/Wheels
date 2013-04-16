package com.freeroom.di;

import com.freeroom.test.beans.Car;
import com.freeroom.test.beans.Home;
import com.freeroom.test.beans.Person;
import org.junit.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

public class PodTest
{
    @Test
    public void should_get_Injection_fields() {
        Pod pod = new Pod(Person.class);
        assertThat(pod.getInjectionFields().size(), is(2));
        assertThat(pod.getInjectionFields().get(0).getType().equals(Home.class), is(true));
        assertThat(pod.getInjectionFields().get(1).getType().equals(Car.class), is(true));
    }
}
