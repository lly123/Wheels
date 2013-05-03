package com.freeroom.web.beans;

import com.freeroom.di.annotations.Bean;
import com.freeroom.web.Model;

@Bean
public class HomeController
{
    public Model index()
    {
        return new Model("html:html/hello.html");
    }
}
