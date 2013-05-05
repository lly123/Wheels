package com.freeroom.web.example.beans;

import com.freeroom.di.annotations.Bean;
import com.freeroom.web.Model;

import static com.freeroom.web.Model.render;

@Bean
public class HomeController
{
    public Model index()
    {
        return render("html:example/index.html");
    }
}
