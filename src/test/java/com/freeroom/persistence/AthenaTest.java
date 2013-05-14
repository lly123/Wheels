package com.freeroom.persistence;

import com.freeroom.persistence.beans.Book;

public class AthenaTest
{
    //@Test
    public void should_select_by_id()
    {
        Athena.from(Book.class).find(1);
    }

    //@Test
    public void should_insert_plain_record()
    {
        Book book = new Book();
        book.setIsbn(1449344852);
        book.setName("AngularJS");
        Athena.save(book);
    }
}
