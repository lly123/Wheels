package com.freeroom.di;

import com.freeroom.di.exceptions.NotUniqueException;
import com.freeroom.test.beans.Dummy;
import com.freeroom.test.beans.Person;
import com.freeroom.test.beans.Shape;
import org.junit.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;


public class BeanContextTest {
    @Test
    public void should_load_bean_given_bean_with_Bean_annotation() {
        BeanContext context = BeanContext.load("com.freeroom.test.beans");
        assertThat(context.getBeans().size(), is(5));
    }

    @Test
    public void should_load_nothing_given_the_package_does_not_exist() {
        BeanContext context = BeanContext.load("com.freeroom.test.notExist");
        assertThat(context.getBeans().size(), is(0));
    }

    @Test
    public void should_load_nothing_given_the_package_exists_but_no_beans() {
        BeanContext context = BeanContext.load("com.freeroom.test.nothing");
        assertThat(context.getBeans().size(), is(0));
    }

    @Test
    public void should_get_bean_given_class_instance() {
        BeanContext context = BeanContext.load("com.freeroom.test.beans");
        assertThat(context.getBean(Person.class).isPresent(), is(true));
    }

    @Test
    public void should_get_nothing_given_no_bean_with_class() {
        BeanContext context = BeanContext.load("com.freeroom.test.beans");
        assertThat(context.getBean(Dummy.class).isPresent(), is(false));
    }

    @Test(expected = NotUniqueException.class)
    public void should_get_NotUniqueException_given_beans_has_same_parent() {
        BeanContext context = BeanContext.load("com.freeroom.test.beans");
        context.getBean(Shape.class);
    }

    @Test
    public void should_get_bean_given_bean_name() {
        BeanContext context = BeanContext.load("com.freeroom.test.beans");
        assertThat(context.getBean("Person").isPresent(), is(true));
    }

    @Test
    public void should_not_get_bean_given_no_bean_with_this_name() {
        BeanContext context = BeanContext.load("com.freeroom.test.beans");
        assertThat(context.getBean("Dummy").isPresent(), is(false));
    }

    @Test
    public void should_get_bean_given_bean_customized_name() {
        BeanContext context = BeanContext.load("com.freeroom.test.beans");
        assertThat(context.getBean("Monster").isPresent(), is(true));
    }
}
