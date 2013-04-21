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
        PeaPod peaPod = new PeaPod(givenABeanConstructor());

        assertThat(peaPod.isBeanReady(), is(true));
    }

    @Test
    public void should_get_correct_bean_class()
    {
        PeaPod peaPod = new PeaPod(givenABeanConstructor());

        assertThat(peaPod.getBeanClass().equals(Dove.class), is(true));
    }

    private Method givenABeanConstructor()
    {
        try {
            Method dove = Hippopotamus.class.getMethod("dove");
            return dove;
        } catch (NoSuchMethodException ignored) {
            throw new RuntimeException("Unexpected exception.", ignored);
        }
    }
}
