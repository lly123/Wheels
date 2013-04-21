package com.freeroom.di.exceptions;

public class ConstructorCycleDependencyException extends RuntimeException
{
    public ConstructorCycleDependencyException(final String message) {
        super(message);
    }
}
