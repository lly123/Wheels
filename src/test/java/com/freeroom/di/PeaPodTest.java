package com.freeroom.di;

import com.freeroom.di.annotations.Scope;
import com.freeroom.test.beans.beanFactory.Dove;
import com.freeroom.test.beans.beanFactory.Hippopotamus;
import org.junit.Test;

import java.lang.reflect.Method;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

public class PeaPodTest
{
    @Test
    public void pea_pod_must_be_always_ready()
    {
        final PeaPod peaPod = new PeaPod(getBeanConstructor("dove"));

        assertThat(peaPod.isBeanReady(), is(true));
    }

    @Test
    public void should_get_bean_class()
    {
        final PeaPod peaPod = new PeaPod(getBeanConstructor("dove"));

        assertThat(peaPod.getBeanClass().equals(Dove.class), is(true));
    }

    @Test
    public void should_get_default_bean_name()
    {
        final PeaPod peaPod = new PeaPod(getBeanConstructor("dove"));

        assertThat(peaPod.getBeanName(), is("com.freeroom.test.beans.beanFactory.Dove"));
    }

    @Test
    public void should_get_customized_bean_name()
    {
        final PeaPod peaPod = new PeaPod(getBeanConstructor("toad"));

        assertThat(peaPod.getBeanName(), is("toad"));
    }

    @Test
    public void should_get_bean_scope()
    {
        final PeaPod peaPod = new PeaPod(getBeanConstructor("pheasant"));

        assertThat(peaPod.getScope(), is(Scope.Required));
    }

    @Test
    public void should_use_canonical_class_name_as_default_name()
    {
        final PeaPod peaPod = new PeaPod(getBeanConstructor("pheasant"));

        assertThat(peaPod.hasName("com.freeroom.test.beans.beanFactory.Pheasant"), is(true));
    }

    @Test
    public void should_check_by_simple_name()
    {
        final PeaPod peaPod = new PeaPod(getBeanConstructor("pheasant"));

        assertThat(peaPod.hasName("Pheasant"), is(true));
    }

    private Method getBeanConstructor(String beanConstructorName)
    {
        try {
            return Hippopotamus.class.getMethod(beanConstructorName);
        } catch (NoSuchMethodException ignored) {
            throw new RuntimeException("Unexpected exception.", ignored);
        }
    }
}
