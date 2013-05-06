package com.freeroom.web.example.beans;

import com.freeroom.di.annotations.Bean;

import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.List;

import static com.google.common.collect.Lists.newArrayList;

@Bean
public class BookRepo
{
    private final List<Book> books = newArrayList();

    public BookRepo()
    {
        books.add(new Book(1449343910, "Bootstrap", 13.83, getMilliseconds(2013, 5, 29)));
        books.add(new Book(1449360726, "Functional JavaScript", 15.81, getMilliseconds(2013, 6, 18)));
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
}
