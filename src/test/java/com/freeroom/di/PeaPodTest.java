package com.freeroom.di;

import org.junit.Test;

import java.lang.reflect.Method;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.mock;

public class PeaPodTest
{
    @Test
    public void pea_pod_must_be_always_ready()
    {
        PeaPod peaPod = new PeaPod(givenABeanConstructor());

        assertThat(peaPod.isBeanReady(), is(true));
    }

    private Method givenABeanConstructor()
    {
        return mock(Method.class);
    }
}
