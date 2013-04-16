package com.freeroom.di;

class Pod
{
    private final Class beanClass;

    public Pod(Class beanClass) {

        this.beanClass = beanClass;
    }

    public Object getBean() {
        return createBeanWithDefaultConstructor();
    }

    private Object createBeanWithDefaultConstructor() {
        try {
            return beanClass.getConstructor().newInstance();
        } catch (Exception e) {
            throw new RuntimeException("Can't create bean with default constructor.", e);
        }
    }
}
