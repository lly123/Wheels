package com.freeroom.di;

import java.lang.reflect.Field;

class FieldHole extends Wormhole
{
    private final Field field;

    public FieldHole(final Field field)
    {
        super(field.getType(), getInjectBeanName(field));
        this.field = field;
        this.field.setAccessible(true);
    }

    public Field getField()
    {
        return field;
    }
}
