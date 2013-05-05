package com.freeroom.web;

import com.freeroom.di.BeanContext;
import org.hamcrest.CoreMatchers;
import org.junit.Before;
import org.junit.Test;

import static com.freeroom.util.RequestBuilder.one;
import static org.hamcrest.CoreMatchers.allOf;
import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.MatcherAssert.assertThat;

public class AresTest
{
    private BeanContext beanContext;

    @Before
    public void setUp()
    {
        beanContext = BeanContext.load("com.freeroom.web.beans");
    }

    @Test
    public void should_return_plain_html()
    {
        final Hephaestus hephaestus = new Hephaestus(beanContext, one().uri("/").build());
        final Ares ares = new Ares(hephaestus.getHandler().fst, hephaestus.getHandler().snd, new Cerberus("UTF-8"));

        assertThat(ares.getContent(), containsString("Hello World!"));
    }

    @Test
    public void should_render_Velocity_template()
    {
        final Hephaestus hephaestus = new Hephaestus(beanContext, one().uri("/books").build());
        final Ares ares = new Ares(hephaestus.getHandler().fst, hephaestus.getHandler().snd, new Cerberus("UTF-8"));

        assertThat(ares.getContent(), containsString("Hello World!"));
    }

    @Test
    public void should_resolve_handler_simple_parameters()
    {
        final Hephaestus hephaestus = new Hephaestus(beanContext, one().uri("/home/mirror").build());
        final Cerberus cerberus = new Cerberus("UTF-8").add("name=lly").add("age=98");
        final Ares ares = new Ares(hephaestus.getHandler().fst, hephaestus.getHandler().snd, cerberus);

        assertThat(ares.getContent(), allOf(containsString("lly"), containsString("98")));
    }

    @Test
    public void should_resolve_nested_parameter()
    {
        final Hephaestus hephaestus = new Hephaestus(beanContext, one().uri("/books/create").build());
        final Cerberus cerberus = new Cerberus("UTF-8").add("book_name=AngularJS").add("book_pageNumber=358");

        final Ares ares = new Ares(hephaestus.getHandler().fst, hephaestus.getHandler().snd, cerberus);

        assertThat(ares.getContent(), containsString("book: AngularJS, page number: 358"));
    }
}
