package com.freeroom.web.example.beans;

import com.freeroom.di.annotations.Bean;
import com.freeroom.di.annotations.Inject;
import com.freeroom.web.Model;

import static com.freeroom.web.Model.render;

@Bean
public class BooksController
{
    @Inject
    private BookService bookService;

    public Model create(final Book book)
    {
        bookService.addBook(book);
        return render("html:example/index.html").
                put("books", bookService.getBooks()).
                put("Tags", bookService.getTags()).
                put("Publishers", bookService.getPublishers());
    }

    public Model delete(final String isbn)
    {
        bookService.delete(isbn);
        return render("html:example/index.html").
                put("books", bookService.getBooks()).
                put("Tags", bookService.getTags()).
                put("Publishers", bookService.getPublishers());
    }

    public Model search(final String bookName)
    {
        return render("html:example/index.html").
                put("books", bookService.findByName(bookName)).
                put("Tags", bookService.getTags()).
                put("Publishers", bookService.getPublishers());
    }
}
