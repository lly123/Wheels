package com.freeroom.di;

import com.google.common.base.Optional;
import com.google.common.base.Predicate;

import java.lang.reflect.Field;
import java.lang.reflect.Type;
import java.util.Collection;

import static com.google.common.collect.Iterables.tryFind;

public class FieldHole implements Hole
{
    private final Field field;
    private Class<?> type;
    private Optional<Object> bean = Optional.absent();

    public FieldHole(Field field) {
        this.type = field.getType();
        this.field = field;
        this.field.setAccessible(true);
    }

    public Field getField() {
        return field;
    }

    public Type getType() {
        return type;
    }

    public Object getBean() {
        return bean.get();
    }

    @Override
    public boolean isFilled() {
        return bean.isPresent();
    }

    @Override
    public void fill(final Collection<Pod> pods) {
        Optional<Pod> pod = tryFind(pods, new Predicate<Pod>() {
            @Override
            public boolean apply(Pod pod) {
                return type.isAssignableFrom(pod.getBeanClass());
            }
        });

        if (!pod.isPresent()) {
            throw new ClassCastException("Bean " + bean + " can't be assigned to " + type);
        }
        this.bean = Optional.of(pod.get().getBean());
    }
}
