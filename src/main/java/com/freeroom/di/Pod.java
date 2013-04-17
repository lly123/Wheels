package com.freeroom.di;

import com.freeroom.di.annotations.Bean;
import com.freeroom.di.annotations.Inject;
import com.freeroom.di.util.Function;
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
    private final Class<?> beanClass;
    private final String beanName;
    private final List<Hole> holes;
    private Object bean;

    public Pod(final Class<?> beanClass) {
        this.beanClass = beanClass;
        this.beanName = findBeanName(beanClass);
        this.holes = findHoles();
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

    public List<Hole> getHoles() {
        return holes;
    }

    public void createBeanWithDefaultConstructor() {
        try {
            bean = beanClass.getConstructor().newInstance();
        } catch (Exception e) {
            throw new RuntimeException("Can't create bean with default constructor.", e);
        }
    }

    public Collection<FieldHole> getFieldHoles() {
        return reduce(Lists.<FieldHole>newArrayList(), holes, new Function<ArrayList<FieldHole>, Hole>() {
            @Override
            public ArrayList<FieldHole> call(ArrayList<FieldHole> fieldHoles, Hole hole) {
                if (hole instanceof FieldHole) {
                    fieldHoles.add((FieldHole) hole);
                }
                return fieldHoles;
            }
        });
    }

    public void populateFields() {
        Collection<FieldHole> fieldHoles = getFieldHoles();
        try {
            for (FieldHole hole : fieldHoles) {
                hole.getField().set(getBean(), hole.getBean());
            }
        } catch (Exception ignored) {}
    }

    @Override
    public boolean equals(final Object o) {
        if (o == null || !(o instanceof Pod)) {
            return false;
        }

        return getBeanName().equals(((Pod) o).getBeanName());
    }

    private List<Hole> findHoles() {
        List<Field> fields = findInjectionFields();

        return newArrayList(Lists.transform(fields, new com.google.common.base.Function<Field, Hole>() {
            @Override
            public Hole apply(Field field) {
                return new FieldHole(field);
            }
        }));
    }

    private List<Field> findInjectionFields() {
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

    private String findBeanName(final Class<?> beanClass) {
        Bean beanAnnotation = beanClass.getAnnotation(Bean.class);
        String beanName = beanAnnotation.value();
        if (isNullOrEmpty(beanName)) {
            beanName = beanClass.getSimpleName();
        }
        return beanName;
    }
}
