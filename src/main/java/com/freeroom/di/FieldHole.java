package com.freeroom.di;

import com.google.common.base.Optional;
import com.google.common.base.Predicate;

import java.lang.reflect.Field;
import java.lang.reflect.Type;
import java.util.Collection;

import static com.google.common.collect.Iterables.tryFind;

class FieldHole extends Hole
{
    private final Field field;
    private final Class<?> clazz;
    private Optional<Object> bean = Optional.absent();

    public FieldHole(final Field field) {
        this.clazz = field.getType();
        this.field = field;
        this.field.setAccessible(true);
    }

    public Field getField() {
        return field;
    }

    public Type getHoleClass() {
        return clazz;
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
                return clazz.isAssignableFrom(pod.getBeanClass()) && pod.isBeanConstructed();
            }
        });

        assertPodExists(clazz, pod);

        this.bean = Optional.of(pod.get().getBean());
    }
}
