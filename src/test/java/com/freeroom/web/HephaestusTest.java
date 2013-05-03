package com.freeroom.web;

import com.freeroom.di.BeanContext;
import com.freeroom.web.beans.BooksController;
import org.junit.Before;
import org.junit.Test;

import java.lang.reflect.Method;

import static com.freeroom.util.RequestBuilder.one;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.core.Is.is;

public class HephaestusTest 
{
    private BeanContext beanContext;

    @Before
    public void setUp()
    {
        beanContext = BeanContext.load("com.freeroom.web.beans");
    }

    @Test
    public void should_get_request_method()
    {
        final Hephaestus hephaestus = new Hephaestus(beanContext, one().method("GET").build());
        assertThat(hephaestus.getMethod(), is(HttpMethod.GET));
    }

    @Test
    public void should_get_Controller_and_Method_as_default_way()
    {
        final Hephaestus hephaestus = new Hephaestus(beanContext, one().uri("/books/list").build());
        assertThat(hephaestus.getHandler().fst, is(instanceOf(BooksController.class)));
        assertThat(hephaestus.getHandler().snd, is(instanceOf(Method.class)));
        assertThat(hephaestus.getHandler().snd.getName(), is("list"));
    }
}
