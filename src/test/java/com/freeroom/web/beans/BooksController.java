package com.freeroom.web.beans;

import com.freeroom.di.annotations.Bean;
import com.freeroom.web.Model;

@Bean
public class BooksController
{
    public Model index()
    {
        return new Model("vm:velocity/hello.vm").put("text", "Hello World!");
    }

    public void list()
    {
    }
}