package com.freeroom.di;

import com.google.common.base.Optional;

import java.lang.reflect.Type;

public class Hole
{
    private Class<?> type;
    private Optional<Object> bean = Optional.absent();

    public Hole(Class<?> type) {
        this.type = type;
    }

    public Type getType() {
        return type;
    }

    public boolean isFilled() {
        return bean.isPresent();
    }

    public void fill(Object bean) {
        if (!type.isAssignableFrom(bean.getClass())) {
            throw new ClassCastException("Bean " + bean + " can't be assigned to " + type);
        }
        this.bean = Optional.of(bean);
    }

    public Object getBean() {
        return bean.get();
    }
}
