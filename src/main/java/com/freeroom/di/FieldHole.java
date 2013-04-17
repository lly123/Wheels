package com.freeroom.di;

import java.lang.reflect.Field;

public class FieldHole extends Hole
{
    private final Field field;

    public FieldHole(Field field) {
        super(field.getType());
        this.field = field;
    }

    public Field getField() {
        return field;
    }
}
