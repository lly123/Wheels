package com.freeroom.di;

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
        PeaPod peaPod = new PeaPod(getBeanConstructor("dove"));

        assertThat(peaPod.isBeanReady(), is(true));
    }

    @Test
    public void should_get_correct_bean_class()
    {
        PeaPod peaPod = new PeaPod(getBeanConstructor("dove"));

        assertThat(peaPod.getBeanClass().equals(Dove.class), is(true));
    }

    @Test
    public void should_get_correct_default_bean_name()
    {
        PeaPod peaPod = new PeaPod(getBeanConstructor("dove"));

        assertThat(peaPod.getBeanName(), is("com.freeroom.test.beans.beanFactory.Dove"));
    }

    @Test
    public void should_get_correct_customized_bean_name()
    {
        PeaPod peaPod = new PeaPod(getBeanConstructor("toad"));

        assertThat(peaPod.getBeanName(), is("toad"));
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
