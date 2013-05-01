package com.freeroom.di;

import com.freeroom.di.annotations.Inject;
import com.freeroom.di.annotations.Scope;
import com.freeroom.di.exceptions.DependencyException;
import com.freeroom.di.exceptions.NotUniqueException;
import com.freeroom.di.util.Func;
import com.google.common.base.Optional;
import com.google.common.base.Predicate;
import com.google.common.collect.Lists;
import net.sf.cglib.proxy.Enhancer;

import java.lang.annotation.Annotation;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Collection;
import java.util.List;

import static com.freeroom.di.util.FuncUtils.reduce;
import static com.google.common.collect.ImmutableList.copyOf;
import static com.google.common.collect.Iterables.any;
import static com.google.common.collect.Iterables.find;
import static com.google.common.collect.Lists.newArrayList;

class SoyPod extends Pod
{
    private final List<Hole> holes;

    public SoyPod(final Class<?> beanClass)
    {
        super(beanClass);
        this.holes = findHoles();
    }

    @Override
    public Scope getScope()
    {
        return getScope(getBeanClass());
    }

    @Override
    public String getBeanName()
    {
        return getBeanName(getBeanClass());
    }

    public List<Hole> getHoles()
    {
        return holes;
    }

    public void fosterBean()
    {
        populateBeanFields();
        callBeanSetters();
    }

    public Hole getConstructorHole()
    {
        return find(holes, new Predicate<Hole>() {
            @Override
            public boolean apply(final Hole hole) {
                return hole instanceof ConstructorHole;
            }
        });
    }

    public void tryConstructBean(final Collection<Pod> pods)
    {
        final ConstructorHole hole = (ConstructorHole) getConstructorHole();
        hole.fill(pods);
        if (hole.isFilled()) {
            createBean(hole);
        }
    }

    @Override
    public boolean equals(final Object o)
    {
        if (o == null || !(o instanceof Pod)) {
            return false;
        }

        return getBeanName().equals(((Pod) o).getBeanName());
    }

    private void createBean(final ConstructorHole constructorHole)
    {
        if (isDynamicBean()) {
            createDynamicBean(constructorHole);
        } else {
            setBean(constructorHole.create());
        }
    }

    private void createDynamicBean(final ConstructorHole constructorHole)
    {
        final Enhancer enhancer = new Enhancer();
        enhancer.setSuperclass(getBeanClass());
        enhancer.setCallback(new DynamicBean(constructorHole));
        setBean(enhancer.create());
    }

    private List<Hole> findHoles()
    {
        final List<Hole> holes = newArrayList();
        holes.add(findConstructorHole());

        final List<Hole> fieldHoles = findFieldHoles();
        assertNoWormHolesForDynamicBean(fieldHoles, "Bean " + getBeanClass() + " can't have field dependencies.");
        holes.addAll(fieldHoles);

        final List<Hole> setterHoles = findSetterHoles();
        assertNoWormHolesForDynamicBean(setterHoles, "Bean " + getBeanClass() + " can't have setter dependencies.");
        holes.addAll(setterHoles);

        return holes;
    }

    private void assertNoWormHolesForDynamicBean(final List<Hole> wormHoles, final String message)
    {
        if (isDynamicBean() && wormHoles.size() > 0) {
            throw new DependencyException(message);
        }
    }

    private Hole findConstructorHole()
    {
         final Optional<Hole> constructorHole = reduce(Optional.<Hole>absent(),
                Lists.<Constructor>newArrayList(beanClass.getConstructors()),
                (hole, constructor) -> {
                    if (isConstructorForInjection(constructor)) {
                        assertNoConstructorHoleBefore(hole);
                        hole = Optional.<Hole>of(new ConstructorHole(constructor));
                    }
                    return hole;
                });

         return constructorHole.isPresent() ? constructorHole.get() : new ConstructorHole(getDefaultConstructor());
    }

    private boolean isConstructorForInjection(final Constructor constructor)
    {
        return any(copyOf(constructor.getParameterAnnotations()), new Predicate<Annotation[]>() {
            @Override
            public boolean apply(final Annotation[] annotations) {
                return containsInjectAnnotation(annotations);
            }
        }) || constructor.isAnnotationPresent(Inject.class);
    }

    private boolean containsInjectAnnotation(final Annotation[] annotations)
    {
        return any(copyOf(annotations), new Predicate<Annotation>() {
            @Override
            public boolean apply(final Annotation annotation) {
                return annotation.annotationType().equals(Inject.class);
            }
        });
    }

    private List<Hole> findFieldHoles()
    {
        return reduce(Lists.<Hole>newArrayList(), newArrayList(beanClass.getDeclaredFields()),
                (injectionFields, field) -> {
                    if (field.isAnnotationPresent(Inject.class)) {
                        injectionFields.add(new FieldHole(field));
                    }
                    return injectionFields;
                });
    }

    private List<Hole> findSetterHoles() {
        return reduce(Lists.<Hole>newArrayList(), newArrayList(beanClass.getDeclaredMethods()),
                (injectionSetters, method) -> {
                    if (method.isAnnotationPresent(Inject.class) && startsWithSetPrefix(method)) {
                        assertHasOnlyOneParameter(method);
                        injectionSetters.add(new SetterHole(method));
                    }
                    return injectionSetters;
                });
    }

    private void populateBeanFields()
    {
        final Collection<FieldHole> fieldHoles = getFieldHoles();
        try {
            for (final FieldHole hole : fieldHoles) {
                hole.getField().set(getBean().get(), hole.getBean());
            }
        } catch (Exception ignored) {}
    }

    private void callBeanSetters()
    {
        final Collection<SetterHole> setterHoles = getSetterHoles();
        try {
            for (final SetterHole hole : setterHoles) {
                hole.getMethod().invoke(getBean().get(), hole.getBean());
            }
        } catch (Exception ignored) {}
    }

    public List<FieldHole> getFieldHoles()
    {
        return reduce(Lists.<FieldHole>newArrayList(), holes, (fieldHoles, hole) -> {
            if (hole instanceof FieldHole) {
                fieldHoles.add((FieldHole) hole);
            }
            return fieldHoles;
        });
    }

    public List<SetterHole> getSetterHoles()
    {
        return reduce(Lists.<SetterHole>newArrayList(), holes, (setterHoles, hole) -> {
            if (hole instanceof SetterHole) {
                setterHoles.add((SetterHole) hole);
            }
            return setterHoles;
        });
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

    private Constructor<?> getDefaultConstructor()
    {
        try {
            return beanClass.getConstructor();
        } catch (Exception e) {
            throw new RuntimeException("Can't find default constructor of bean: " + getBeanClass(), e);
        }
    }

    private boolean isDynamicBean()
    {
        return getScope().equals(Scope.Dynamic);
    }
}
