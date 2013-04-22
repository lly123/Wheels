package com.freeroom.di;

import com.freeroom.di.annotations.Bean;
import com.freeroom.di.annotations.Scope;
import com.google.common.base.Optional;

import java.lang.reflect.AnnotatedElement;

import static com.google.common.base.Optional.fromNullable;
import static com.google.common.base.Strings.isNullOrEmpty;

abstract class Pod
{
    protected final Class<?> beanClass;
    private Object bean;

    public Pod(final Class<?> beanClass)
    {
        this.beanClass = beanClass;
    }

    public Optional<Object> getBean()
    {
        return fromNullable(bean);
    }

    public Class<?> getBeanClass()
    {
        return beanClass;
    }

    public void removeBean()
    {
        bean = null;
    }

    public boolean hasName(final String name)
    {
        return getBeanName().equals(name) || getBeanName().endsWith("." + name);
    }

    public boolean isBeanReady()
    {
        return getBean().isPresent();
    }

    protected void setBean(final Object bean)
    {
        this.bean = bean;
    }

    protected Scope getScope(AnnotatedElement element)
    {
        final Bean beanAnnotation = element.getAnnotation(Bean.class);
        return beanAnnotation.scope();
    }

    protected String getBeanName(AnnotatedElement element)
    {
        final Bean beanAnnotation = element.getAnnotation(Bean.class);
        return isNullOrEmpty(beanAnnotation.value()) ? getBeanClass().getCanonicalName() : beanAnnotation.value();
    }

    abstract Scope getScope();

    abstract String getBeanName();
}
