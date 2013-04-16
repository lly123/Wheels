package com.freeroom.di;

import com.freeroom.di.annotations.Bean;
import com.google.common.base.Strings;

import java.lang.annotation.Annotation;

import static com.google.common.base.Strings.isNullOrEmpty;

class Pod
{
    private final Class beanClass;
    private String beanName;

    public Pod(Class beanClass) {
        this.beanClass = beanClass;
        setBeanName(beanClass);
    }

    public Object getBean() {
        return createBeanWithDefaultConstructor();
    }

    public String getBeanName() {
        return beanName;
    }

    private Object createBeanWithDefaultConstructor() {
        try {
            return beanClass.getConstructor().newInstance();
        } catch (Exception e) {
            throw new RuntimeException("Can't create bean with default constructor.", e);
        }
    }

    private void setBeanName(Class beanClass) {
        Bean beanAnnotation = (Bean) beanClass.getAnnotation(Bean.class);
        beanName = beanAnnotation.value();
        if (isNullOrEmpty(beanName)) {
            beanName = beanClass.getSimpleName();
        }
    }
}
