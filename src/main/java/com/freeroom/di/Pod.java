package com.freeroom.di;

import com.freeroom.di.annotations.Bean;
import com.freeroom.di.annotations.Inject;
import com.freeroom.di.util.Function;
import com.google.common.collect.Lists;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

import static com.freeroom.di.util.Iterables.reduce;
import static com.google.common.base.Strings.isNullOrEmpty;
import static com.google.common.collect.Lists.newArrayList;

class Pod
{
    private final Class<?> beanClass;
    private final String beanName;
    private Object bean;

    public Pod(final Class<?> beanClass) {
        this.beanClass = beanClass;
        this.beanName = findBeanName(beanClass);
    }

    public String getBeanName() {
        return beanName;
    }

    public Class<?> getBeanClass() {
        return beanClass;
    }

    public Object getBean() {
        return bean;
    }

    @Override
    public boolean equals(final Object o) {
        if (o == null || !(o instanceof Pod)) {
            return false;
        }

        return getBeanName().equals(((Pod) o).getBeanName());
    }

    public void createBeanWithDefaultConstructor() {
        try {
            bean = beanClass.getConstructor().newInstance();
        } catch (Exception e) {
            throw new RuntimeException("Can't create bean with default constructor.", e);
        }
    }

    private List<Field> getInjectionFields() {
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

    public List<Hole> getHoles() {
        List<Field> fields = getInjectionFields();

        return Lists.transform(fields, new com.google.common.base.Function<Field, Hole>() {
            @Override
            public Hole apply(Field field) {
                return new Hole(field.getType(), HoleType.FIELD);
            }
        });
    }

    private String findBeanName(final Class<?> beanClass) {
        Bean beanAnnotation = beanClass.getAnnotation(Bean.class);
        String beanName = beanAnnotation.value();
        if (isNullOrEmpty(beanName)) {
            beanName = beanClass.getSimpleName();
        }
        return beanName;
    }
}
