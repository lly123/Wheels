package com.freeroom.web.beans;

import com.freeroom.di.annotations.Bean;
import com.freeroom.web.Model;

import static com.freeroom.web.Model.render;

@Bean
public class HomeController
{
    public Model index()
    {
        return render("html:html/hello.html");
    }

    public Model mirror(final String name, final String age)
    {
        return render("vm:velocity/person.vm")
                .put("name", name)
                .put("age", age);
    }
}
