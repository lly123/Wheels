package com.freeroom.di.exceptions;

public class ConstructorCycleDependencyException extends RuntimeException
{
    public ConstructorCycleDependencyException(String message) {
        super(message);
    }
}
