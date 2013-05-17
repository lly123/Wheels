package com.freeroom.web.example.beans;

import com.freeroom.di.annotations.Bean;
import com.freeroom.web.Model;

import static com.freeroom.web.Model.render;

@Bean
public class AjaxController
{
    public Model index()
    {
        return render("html:example/ajax.html");
    }

    public Model get()
    {
        return render("jsonp:").put("token", "123456");
    }
}
