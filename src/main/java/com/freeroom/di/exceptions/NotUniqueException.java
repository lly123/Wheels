package com.freeroom.di.exceptions;

public class NotUniqueException extends RuntimeException
{
    public NotUniqueException(final String message) {
        super(message);
    }
}
