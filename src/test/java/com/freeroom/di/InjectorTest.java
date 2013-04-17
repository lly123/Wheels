package com.freeroom.di;

import com.freeroom.di.exceptions.NoBeanException;
import com.freeroom.test.beans.Car;
import com.freeroom.test.beans.Person;
import org.junit.Test;

import static com.google.common.collect.Lists.newArrayList;

public class InjectorTest
{
    @Test(expected = NoBeanException.class)
    public void should_throw_NoBeanException_given_can_not_find_bean_in_context() {
        Injector injector = new Injector(newArrayList(new Pod(Car.class)));
        injector.resolve();
    }

    @Test
    public void should_have_all_beans_in_context() {
        Injector injector = new Injector(newArrayList(new Pod(Car.class), new Pod(Person.class)));
        injector.resolve();
    }
}
