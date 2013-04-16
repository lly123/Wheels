package com.freeroom.di;

import com.freeroom.di.annotations.Bean;
import com.freeroom.di.annotations.Inject;
import com.freeroom.di.util.Function;
import com.freeroom.di.util.Iterables;
import com.google.common.collect.Lists;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import static com.freeroom.di.util.Iterables.reduce;
import static com.google.common.base.Strings.isNullOrEmpty;
import static com.google.common.collect.Lists.newArrayList;

class Pod
{
    private final Class beanClass;
    private final String beanName;

    public Pod(final Class beanClass) {
        this.beanClass = beanClass;
        this.beanName = findBeanName(beanClass);
    }

    public Object getBean() {
        return createBeanWithDefaultConstructor();
    }

    public String getBeanName() {
        return beanName;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || !(o instanceof Pod)) {
            return false;
        }

        return getBeanName().equals(((Pod) o).getBeanName());
    }

    private Object createBeanWithDefaultConstructor() {
        try {
            return beanClass.getConstructor().newInstance();
        } catch (Exception e) {
            throw new RuntimeException("Can't create bean with default constructor.", e);
        }
    }

    private String findBeanName(final Class beanClass) {
        Bean beanAnnotation = (Bean) beanClass.getAnnotation(Bean.class);
        String beanName = beanAnnotation.value();
        if (isNullOrEmpty(beanName)) {
            beanName = beanClass.getSimpleName();
        }
        return beanName;
    }

    public List<Field> getInjectionFields() {
        return reduce(Lists.<Field>newArrayList(), newArrayList(beanClass.getDeclaredFields()),
            new Function<ArrayList<Field>, Field>() {
                @Override
                public ArrayList<Field> call(ArrayList<Field> injectionFields, Field field) {
                    if (field.isAnnotationPresent(Inject.class)) {
                        injectionFields.add(field);
                    }
                    return injectionFields;
                }
            }
        );
    }
}
