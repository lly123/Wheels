package com.freeroom.di;

import com.freeroom.di.annotations.Bean;

import static com.google.common.base.Strings.isNullOrEmpty;

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
}
