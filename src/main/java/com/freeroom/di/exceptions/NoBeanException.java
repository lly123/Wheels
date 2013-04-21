package com.freeroom.di.exceptions;

public class NoBeanException extends RuntimeException
{
    public NoBeanException(final String message) {
        super(message);
    }
}
