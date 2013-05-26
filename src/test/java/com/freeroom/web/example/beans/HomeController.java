package com.freeroom.web.example.beans;

import com.freeroom.di.annotations.Bean;
import com.freeroom.di.annotations.Inject;
import com.freeroom.web.Model;

import static com.freeroom.web.Model.render;

@Bean
public class HomeController
{
    @Inject
    private BookService bookService;

    public Model index()
    {
        return render("html:example/index.html").
                put("books", bookService.getBooks()).
                put("Tags", bookService.getTags()).
                put("Publishers", bookService.getPublishers());
    }
}
