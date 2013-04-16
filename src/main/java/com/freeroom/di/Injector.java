package com.freeroom.di;

import com.freeroom.di.exceptions.NoBeanException;
import com.freeroom.di.util.Function;
import com.google.common.base.Predicate;

import java.lang.reflect.Field;
import java.lang.reflect.Type;
import java.util.Collection;
import java.util.HashSet;

import static com.freeroom.di.util.Iterables.reduce;
import static com.google.common.collect.Iterables.all;
import static com.google.common.collect.Iterables.any;
import static com.google.common.collect.Iterables.transform;
import static com.google.common.collect.Lists.newArrayList;
import static java.lang.String.format;

public class Injector
{
    private final Collection<Pod> pods;

    public Injector(Collection<Pod> pods) {
        this.pods = pods;
    }

    public void resolve() {
        assertContainAll(pods, getUniqueTypes());
    }

    private void assertContainAll(final Collection<Pod> pods, final Collection<Type> types) {
        all(types, new Predicate<Type>() {
            @Override
            public boolean apply(final Type type) {
                if (!any(pods, new Predicate<Pod>() {
                    @Override
                    public boolean apply(Pod pod) {
                        return pod.getBean().getClass().equals(type);
                    }
                })) {
                    throw new NoBeanException(format("Can't find bean of type %s in context.", type));
                }
                return true;
            }
        });
    }

    private Collection<Type> getUniqueTypes() {
        return reduce(new HashSet<Type>(), pods, new Function<HashSet<Type>, Pod>() {
            @Override
            public HashSet<Type> call(HashSet<Type> fieldTypes, Pod pod) {
                fieldTypes.addAll(toTypes(pod.getInjectionFields()));
                return fieldTypes;
            }
        });
    }

    private Collection<Type> toTypes(final Collection<Field> fields) {
        return newArrayList(transform(fields, new com.google.common.base.Function<Field, Type>() {
            @Override
            public Type apply(Field field) {
                return field.getType();
            }
        }));
    }
}
