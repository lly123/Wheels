package com.freeroom.web;

import org.junit.Test;

import static com.freeroom.util.RequestBuilder.one;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;

public class HephaestusTest 
{
    @Test
    public void should_get_request_method()
    {
        final Hephaestus hephaestus = new Hephaestus(one().method("GET").build());
        assertThat(hephaestus.getMethod(), is(HttpMethod.GET));
    }
}
