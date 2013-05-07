package com.freeroom.web.example.beans;

import com.freeroom.di.annotations.Bean;

import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.List;

import static com.google.common.collect.Iterables.find;
import static com.google.common.collect.Lists.newArrayList;
import static java.lang.Long.parseLong;

@Bean
public class BookRepo
{
    private final List<Book> books = newArrayList();

    public BookRepo()
    {
        books.add(new Book(1449344852, "AngularJS", 13.83, getMilliseconds(2013, 4, 30), newArrayList(101, 102, 103)));
        books.add(new Book(1449343910, "Bootstrap", 13.83, getMilliseconds(2013, 5, 29), newArrayList(101)));
        books.add(new Book(1449360726, "Functional JavaScript", 15.81, getMilliseconds(2013, 6, 18), newArrayList(101, 104)));
        books.add(new Book(1449323391, "Testable JavaScript", 19.98, getMilliseconds(2013, 1, 31), newArrayList(104)));
        books.add(new Book(1449323073, "Learning Node", 20.98, getMilliseconds(2012, 10, 10), newArrayList(101, 102, 103, 104)));
    }

    private long getMilliseconds(final int year, final int month, final int day)
    {
        final Calendar calendar = GregorianCalendar.getInstance();
        calendar.set(year, month - 1, day);
        return calendar.getTime().getTime();
    }

    public List<Book> getBooks()
    {
        return books;
    }

    public void add(final Book book)
    {
        books.add(book);
    }

    public void delete(final String isbn)
    {
        books.remove(find(books, book -> book.getIsbn() == parseLong(isbn)));
    }
}
