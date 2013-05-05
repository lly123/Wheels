package com.freeroom.web.beans;

import com.freeroom.di.annotations.Bean;
import com.freeroom.web.Model;

import static com.freeroom.web.Model.render;

@Bean
public class BooksController
{
    public Model index()
    {
        return render("vm:velocity/hello.vm").put("text", "Hello World!");
    }

    public void list()
    {
    }

    public Model create(final Book book)
    {
        return render("vm:velocity/success.vm").put("text",
                "book: " + book.getName() + ", page number: " + book.getPageNumber());
    }
}
