package com.freeroom.di;

import com.freeroom.di.exceptions.NotUniqueException;
import com.freeroom.test.beans.dummy.Dummy;
import com.freeroom.test.beans.fieldInjection.Person;
import com.freeroom.test.beans.sameParent.Shape;
import com.google.common.base.Optional;
import org.junit.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.sameInstance;

public class BeanContextTest
{
    @Test
    public void should_load_bean_given_bean_with_Bean_annotation()
    {
        BeanContext context = BeanContext.load("com.freeroom.test.beans.requiredScope");
        assertThat(context.getBeans().size(), is(1));
    }

    @Test
    public void should_load_nothing_given_the_package_does_not_exist()
    {
        BeanContext context = BeanContext.load("com.freeroom.test.notExist");
        assertThat(context.getBeans().size(), is(0));
    }

    @Test
    public void should_load_nothing_given_the_package_exists_but_no_beans()
    {
        BeanContext context = BeanContext.load("com.freeroom.test.nothing");
        assertThat(context.getBeans().size(), is(0));
    }

    @Test
    public void should_get_bean_given_class_instance()
    {
        BeanContext context = BeanContext.load("com.freeroom.test.beans.fieldInjection");
        assertThat(context.getBean(Person.class).isPresent(), is(true));
    }

    @Test
    public void should_get_nothing_given_no_bean_with_class()
    {
        BeanContext context = BeanContext.load("com.freeroom.test.beans.dummy");
        assertThat(context.getBean(Dummy.class).isPresent(), is(false));
    }

    @Test(expected = NotUniqueException.class)
    public void should_throw_NotUniqueException_given_beans_have_same_parent()
    {
        BeanContext context = BeanContext.load("com.freeroom.test.beans.sameParent");
        context.getBean(Shape.class);
    }

    @Test
    public void should_get_bean_given_bean_name()
    {
        BeanContext context = BeanContext.load("com.freeroom.test.beans.fieldInjection");
        assertThat(context.getBean("Person").isPresent(), is(true));
    }

    @Test
    public void should_not_get_bean_given_no_bean_with_this_name()
    {
        BeanContext context = BeanContext.load("com.freeroom.test.beans.dummy");
        assertThat(context.getBean("Dummy").isPresent(), is(false));
    }

    @Test
    public void should_get_bean_given_bean_customized_name()
    {
        BeanContext context = BeanContext.load("com.freeroom.test.beans.fieldInjection");
        assertThat(context.getBean("Monster").isPresent(), is(true));
    }

    @Test(expected = NotUniqueException.class)
    public void should_throw_NotUniqueException_given_beans_have_same_customized_name()
    {
        BeanContext.load("com.freeroom.test.beans.sameBeanName");
    }

    @Test
    public void should_create_new_beans_given_REQUIRED_scope()
    {
        BeanContext context = BeanContext.load("com.freeroom.test.beans.requiredScope");

        Optional<?> otter1 = context.getBean("Otter");
        Optional<?> otter2 = context.getBean("Otter");

        assertThat(otter1.get(), is(not(sameInstance(otter2.get()))));
    }
}
