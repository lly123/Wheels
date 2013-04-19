package com.freeroom.di;

import com.freeroom.di.annotations.Bean;
import com.freeroom.di.annotations.Inject;
import com.freeroom.di.annotations.Scope;
import com.freeroom.di.exceptions.NotUniqueException;
import com.freeroom.di.util.Func;
import com.google.common.base.Function;
import com.google.common.base.Optional;
import com.google.common.base.Predicate;
import com.google.common.collect.Lists;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

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
    private final Scope scope;
    private Object bean;

    public Pod(final Class<?> beanClass)
    {
        this.beanClass = beanClass;
        this.beanName = findBeanName();
        this.holes = findHoles();
        this.scope = findScope();
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

    public boolean isBeanReady()
    {
        return bean != null;
    }

    public List<Hole> getHoles()
    {
        return holes;
    }

    public Scope getScope()
    {
        return scope;
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
        return reduce(Lists.<FieldHole>newArrayList(), holes, new Func<ArrayList<FieldHole>, Hole>() {
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
        final Collection<FieldHole> fieldHoles = getFieldHoles();
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

    public void createBean(ConstructorHole constructorHole)
    {
        bean = constructorHole.create();
    }

    public void tryConstructBean(final Collection<Pod> pods)
    {
        final Optional<Hole> constructorHole = getConstructorHole();

        if (constructorHole.isPresent()) {
            final ConstructorHole hole = (ConstructorHole) constructorHole.get();
            hole.fill(pods);
            if (hole.isFilled()) {
                createBean(hole);
            }
        } else {
            createBeanWithDefaultConstructor();
        }
    }

    public void removeBean()
    {
        bean = null;
    }

    private List<Hole> findHoles()
    {
        final List<Hole> holes = newArrayList();
        final Optional<Hole> hole = findConstructorHole();

        if (hole.isPresent()) {
            holes.add(hole.get());
        }
        holes.addAll(findFieldHoles());
        holes.addAll(findSetterHoles());

        return holes;
    }

    private Optional<Hole> findConstructorHole()
    {
        return reduce(Optional.<Hole>absent(), Lists.<Constructor>newArrayList(beanClass.getConstructors()),
            new Func<Optional<Hole>, Constructor>() {
                @Override
                public Optional<Hole> call(Optional<Hole> hole, final Constructor constructor) {
                    if (constructor.isAnnotationPresent(Inject.class)) {
                        assertNoConstructorHoleBefore(hole);
                        hole = Optional.<Hole>of(new ConstructorHole(constructor));
                    }
                    return hole;
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

    private List<Hole> findSetterHoles() {
        return reduce(Lists.<Hole>newArrayList(), findInjectionSetters(), new Func<ArrayList<Hole>, Method>() {
            @Override
            public ArrayList<Hole> call(final ArrayList<Hole> holes, final Method method) {
                if (startsWithSetPrefix(method)) {
                    assertHasOnlyOneParameter(method);
                    holes.add(new SetterHole(method));
                }
                return holes;
            }
        });
    }

    private List<Field> findInjectionFields()
    {
        return reduce(Lists.<Field>newArrayList(), newArrayList(beanClass.getDeclaredFields()),
            new Func<ArrayList<Field>, Field>() {
                @Override
                public ArrayList<Field> call(final ArrayList<Field> injectionFields, final Field field) {
                    if (field.isAnnotationPresent(Inject.class)) {
                        injectionFields.add(field);
                    }
                    return injectionFields;
                }
            });
    }

    private List<Method> findInjectionSetters() {
        return reduce(Lists.<Method>newArrayList(), newArrayList(beanClass.getDeclaredMethods()),
                new Func<ArrayList<Method>, Method>() {
                    @Override
                    public ArrayList<Method> call(final ArrayList<Method> injectionSetters, final Method method) {
                        if (method.isAnnotationPresent(Inject.class)) {
                            injectionSetters.add(method);
                        }
                        return injectionSetters;
                    }
                });
    }

    private String findBeanName()
    {
        final Bean beanAnnotation = beanClass.getAnnotation(Bean.class);
        return isNullOrEmpty(beanAnnotation.value()) ? beanClass.getSimpleName() : beanAnnotation.value();
    }

    private Scope findScope()
    {
        final Bean beanAnnotation = beanClass.getAnnotation(Bean.class);
        return beanAnnotation.scope();
    }

    private void assertNoConstructorHoleBefore(final Optional<Hole> hole)
    {
        if (hole.isPresent()) {
            throw new NotUniqueException("Has more than one constructor injection of bean: " + getBeanClass());
        }
    }

    private boolean startsWithSetPrefix(final Method method)
    {
        return method.getName().startsWith("set");
    }

    private void assertHasOnlyOneParameter(final Method method)
    {
        if (method.getParameterTypes().length != 1) {
            throw new NotUniqueException("Method " + method.getName() + " of bean " + getBeanClass() +
                " must have one parameter.");
        }
    }
}
