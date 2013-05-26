package com.freeroom.web.example.beans;

import com.freeroom.di.annotations.Bean;
import com.freeroom.di.annotations.Inject;
import com.freeroom.persistence.Athena;
import com.google.common.base.Optional;

import java.util.List;

@Bean
public class BookService
{
    @Inject
    private Athena athena;

    public List<Book> getBooks()
    {
        final List<Object> books = athena.from(Book.class).all();
        for (int i = 0; i < books.size(); i++) {
            Book book = (Book)books.get(i);
            book.getPublisher().getName();
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

    public List<Book> findByName(final String bookName)
    {
        final List<Object> books = athena.from(Book.class).find("UPPER(name) like '%" + bookName.toUpperCase() + "%'");
        for (int i = 0; i < books.size(); i++) {
            Book book = (Book)books.get(i);
            book.getPublisher().getName();
        }
        return (List<Book>)athena.detach(books);
    }

    public List<Tag> getTags()
    {
        final List<Object> tags = athena.from(Tag.class).all();
        for (int i = 0; i < tags.size(); i++) {
            Tag tag = (Tag)tags.get(i);
            tag.getLabel();
        }
        return (List<Tag>)athena.detach(tags);
    }

    public List<Publisher> getPublishers()
    {
        final List<Object> publishers = athena.from(Publisher.class).all();
        for (int i = 0; i < publishers.size(); i++) {
            Publisher publisher = (Publisher)publishers.get(i);
            publisher.getName();
        }
        return (List<Publisher>)athena.detach(publishers);
    }
}
