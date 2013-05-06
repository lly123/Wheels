package com.freeroom.web;

import com.freeroom.di.annotations.Bean;

import static com.freeroom.web.Model.render;

@Bean
public class Prometheus
{
    public Model index(final String uri)
    {
        return render("res:" + uri.substring(1));
    }
}
