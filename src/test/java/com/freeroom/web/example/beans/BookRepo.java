package com.freeroom.web.example.beans;

import com.freeroom.di.annotations.Bean;
import com.freeroom.di.annotations.Inject;
import com.freeroom.persistence.Athena;
import com.google.common.base.Optional;

import java.util.List;

@Bean
public class BookRepo
{
    @Inject
    private Athena athena;

    public List<Book> getBooks()
    {
        final List<Object> books = athena.from(Book.class).all();
        for (Object book : books) {
            ((Book)book).getIsbn();
        }
        return (List<Book>)athena.detach(books);
    }

    public void addBook(final Book book)
    {
        athena.persist(book);
    }

    public void delete(final String isbn)
    {
        final Optional<Object> book = athena.findOnly("isbn=" + isbn);
        if (book.isPresent()) {
            athena.remove(book.get());
        }
    }
}
