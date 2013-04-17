package com.freeroom.di;

import com.freeroom.di.annotations.Bean;
import com.freeroom.di.annotations.Inject;
import com.freeroom.di.util.Action;
import com.freeroom.di.util.RFunc;
import com.google.common.base.Function;
import com.google.common.base.Optional;
import com.google.common.base.Predicate;
import com.google.common.collect.Lists;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import static com.freeroom.di.util.FuncUtils.each;
import static com.freeroom.di.util.FuncUtils.reduce;
import static com.google.common.base.Strings.isNullOrEmpty;
import static com.google.common.collect.Iterables.tryFind;
import static com.google.common.collect.Lists.newArrayList;
import static com.google.common.collect.Lists.transform;

class Pod
{
    private final Class<?> beanClass;
    private final String beanName;
    private final List<Hole> holes;
    private Object bean;

    public Pod(final Class<?> beanClass)
    {
        this.beanClass = beanClass;
        this.beanName = findBeanName(beanClass);
        this.holes = findHoles();
    }

    public String getBeanName()
    {
        return beanName;
    }

    public Class<?> getBeanClass()
    {
        return beanClass;
    }

    public Object getBean()
    {
        return bean;
    }

    public boolean isBeanConstructed()
    {
        return bean != null;
    }

    public List<Hole> getHoles()
    {
        return holes;
    }

    public void createBeanWithDefaultConstructor()
    {
        try {
            bean = beanClass.getConstructor().newInstance();
        } catch (Exception e) {
            throw new RuntimeException("Can't create bean with default constructor.", e);
        }
    }

    public Collection<FieldHole> getFieldHoles()
    {
        return reduce(Lists.<FieldHole>newArrayList(), holes, new RFunc<ArrayList<FieldHole>, Hole>() {
            @Override
            public ArrayList<FieldHole> call(final ArrayList<FieldHole> fieldHoles, final Hole hole) {
                if (hole instanceof FieldHole) {
                    fieldHoles.add((FieldHole) hole);
                }
                return fieldHoles;
            }
        });
    }

    public void populateBeanFields()
    {
        Collection<FieldHole> fieldHoles = getFieldHoles();
        try {
            for (FieldHole hole : fieldHoles) {
                hole.getField().set(getBean(), hole.getBean());
            }
        } catch (Exception ignored) {}
    }

    public Optional<Hole> getConstructorHole()
    {
        return tryFind(holes, new Predicate<Hole>() {
            @Override
            public boolean apply(final Hole hole) {
                return hole instanceof ConstructorHole;
            }
        });
    }

    @Override
    public boolean equals(final Object o)
    {
        if (o == null || !(o instanceof Pod)) {
            return false;
        }

        return getBeanName().equals(((Pod) o).getBeanName());
    }

    private List<Hole> findHoles()
    {
        List<Hole> holes = newArrayList();

        Optional<Hole> hole = findConstructorHole();
        if (hole.isPresent()) {
            holes.add(hole.get());
        }
        holes.addAll(findFieldHoles());

        return holes;
    }

    private Optional<Hole> findConstructorHole()
    {
        return reduce(Optional.<Hole>absent(), Lists.<Constructor>newArrayList(beanClass.getConstructors()),
            new RFunc<Optional<Hole>, Constructor>() {
                @Override
                public Optional<Hole> call(final Optional<Hole> hole, final Constructor constructor) {
                    return constructor.isAnnotationPresent(Inject.class) ?
                        Optional.<Hole>of(new ConstructorHole(constructor)) :
                        hole;
                }
            });
    }

    private List<Hole> findFieldHoles()
    {
        return transform(findInjectionFields(), new Function<Field, Hole>() {
            @Override
            public Hole apply(final Field field) {
                return new FieldHole(field);
            }
        });
    }

    private List<Field> findInjectionFields()
    {
        return reduce(Lists.<Field>newArrayList(), newArrayList(beanClass.getDeclaredFields()),
            new RFunc<ArrayList<Field>, Field>() {
                @Override
                public ArrayList<Field> call(final ArrayList<Field> injectionFields, final Field field) {
                    if (field.isAnnotationPresent(Inject.class)) {
                        injectionFields.add(field);
                    }
                    return injectionFields;
                }
            });
    }

    private String findBeanName(final Class<?> beanClass)
    {
        Bean beanAnnotation = beanClass.getAnnotation(Bean.class);

        return isNullOrEmpty(beanAnnotation.value()) ? beanClass.getSimpleName() : beanAnnotation.value();
    }

    public void createBean(ConstructorHole constructorHole)
    {
        bean = constructorHole.create();
    }
}
