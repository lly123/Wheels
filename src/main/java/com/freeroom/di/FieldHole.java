package com.freeroom.di;

import com.freeroom.di.annotations.Inject;
import com.google.common.base.Optional;

import java.lang.reflect.Field;

import static com.google.common.base.Optional.of;
import static com.google.common.base.Strings.isNullOrEmpty;

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

    private static Optional<String> getInjectBeanName(Field field)
    {
        final Inject annotation = field.getAnnotation(Inject.class);
        return isNullOrEmpty(annotation.value()) ? Optional.<String>absent() : of(annotation.value());
    }
}
