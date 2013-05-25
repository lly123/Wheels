package com.freeroom.web.example.beans;

import com.freeroom.di.annotations.Bean;
import com.freeroom.di.annotations.Inject;
import com.freeroom.web.Model;

import static com.freeroom.web.Model.render;

@Bean
public class BooksController
{
    @Inject
    private BookRepo bookRepo;

    public Model create(final Book book)
    {
        bookRepo.addBook(book);
        return render("html:example/index.html").put("books", bookRepo.getBooks());
    }

    public Model delete(final String isbn)
    {
        bookRepo.delete(isbn);
        return render("html:example/index.html").put("books", bookRepo.getBooks());
    }

    public Model search(final String bookName)
    {
        return render("html:example/index.html").put("books", bookRepo.findByName(bookName));
    }
}
