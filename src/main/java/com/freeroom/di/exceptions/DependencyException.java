package com.freeroom.di.exceptions;

public class DependencyException extends RuntimeException
{
    public DependencyException(final String message) {
        super(message);
    }
}
