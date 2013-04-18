package com.freeroom.di;

import com.freeroom.di.exceptions.ConstructorCycleDependencyException;
import com.freeroom.di.exceptions.NoBeanException;
import com.freeroom.test.beans.Car;
import com.freeroom.test.beans.Person;
import com.freeroom.test.beans.constrcutorInjection.Student;
import com.freeroom.test.beans.constrcutorInjection.Teacher;
import com.freeroom.test.beans.constrcutorInjection.cycleDependency.ClassA;
import com.freeroom.test.beans.constrcutorInjection.cycleDependency.ClassB;
import org.junit.Test;

import java.util.Collection;

import static com.google.common.collect.Lists.newArrayList;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.Assert.fail;

public class InjectorTest
{
    @Test(expected = NoBeanException.class)
    public void should_throw_NoBeanException_given_can_not_find_bean_in_context()
    {
        Injector injector = new Injector(newArrayList(new Pod(Car.class)));
        injector.resolve();
    }

    @Test
    public void should_have_all_beans_in_context()
    {
        Injector injector = new Injector(newArrayList(new Pod(Car.class), new Pod(Person.class)));
        injector.resolve();
    }

    @Test
    public void should_resolve_bean_field_cycle_dependencies()
    {
        Injector injector = new Injector(newArrayList(new Pod(Car.class), new Pod(Person.class)));
        Collection<Pod> pods = injector.resolve();

        assertThat(pods.size(), is(2));

        for (Pod pod : pods) {
            if (pod.getBeanClass().equals(Person.class)) {
                Person person = (Person) pod.getBean();
                assertThat(person.getCar(), is(notNullValue()));
            } else if (pod.getBeanClass().equals(Car.class)) {
                Car person = (Car) pod.getBean();
                assertThat(person.getDriver(), is(notNullValue()));
            } else {
                fail();
            }
        }
    }

    @Test
    public void should_resolve_beans_with_constructor_injection()
    {
        Injector injector = new Injector(newArrayList(new Pod(Student.class), new Pod(Teacher.class)));
        Collection<Pod> pods = injector.resolve();

        assertThat(pods.size(), is(2));

        for (Pod pod : pods) {
            if (pod.getBeanClass().equals(Student.class)) {
                Student person = (Student) pod.getBean();
                assertThat(person.getTeacher(), is(notNullValue()));
            }
        }
    }

    @Test(expected = ConstructorCycleDependencyException.class)
    public void should_throw_ConstructorCycleDependencyException()
    {
        Injector injector = new Injector(newArrayList(new Pod(ClassA.class), new Pod(ClassB.class)));
        injector.resolve();
    }
}
