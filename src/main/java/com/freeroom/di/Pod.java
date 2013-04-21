package com.freeroom.di;

import com.freeroom.di.annotations.Bean;
import com.freeroom.di.annotations.Scope;

import java.lang.reflect.AnnotatedElement;

import static com.google.common.base.Strings.isNullOrEmpty;

abstract class Pod
{
    protected final Class<?> beanClass;
    private Object bean;

    public Pod(final Class<?> beanClass)
    {
        this.beanClass = beanClass;
    }

    public Object getBean()
    {
        return bean;
    }

    public Class<?> getBeanClass()
    {
        return beanClass;
    }

    abstract Scope getScope();

    abstract String getBeanName();

    abstract boolean isBeanReady();

    public void removeBean()
    {
        bean = null;
    }

    public boolean hasName(final String name)
    {
        return getBeanName().equals(name) || getBeanName().endsWith("." + name);
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
}
