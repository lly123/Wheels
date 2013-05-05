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

    public Model mirror(String name, String age)
    {
        return new Model("vm:velocity/person.vm")
                .put("name", name)
                .put("age", age);
    }
}
