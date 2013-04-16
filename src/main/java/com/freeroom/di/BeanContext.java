package com.freeroom.di;

import com.freeroom.di.annotations.Bean;
import com.google.common.base.Predicate;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static com.google.common.collect.Iterables.filter;

public class BeanContext {
    private static BeanContext context;

    final Package beanPackage;
    private List<Object> beans = new ArrayList<>();

    private BeanContext(String packageName) {
        this.beanPackage = new Package(packageName);
    }

    public static BeanContext load(String packageName) {
        context = new BeanContext(packageName);
        context.initialize();

        return context;
    }

    private void initialize() {
        try {
            List<Class> beanClasses = beanPackage.getClasses();
            initBeansWithBeanAnnotation(beanClasses);
        } catch (IOException | ClassNotFoundException ignored) {
        }
    }

    private void initBeansWithBeanAnnotation(List<Class> beanClasses) {
        for (Class beanClass : getBeanClassesWithBeanAnnotation(beanClasses)) {
            try {
                beans.add(createBeanWithDefaultConstructor(beanClass));
            } catch (Exception ignored) {}
        }
    }

    private Object createBeanWithDefaultConstructor(Class beanClass) throws Exception {
        return beanClass.getConstructor().newInstance();
    }

    private Iterable<Class> getBeanClassesWithBeanAnnotation(List<Class> beanClasses) {
        return filter(beanClasses, new Predicate<Class>() {
            @Override
            public boolean apply(Class beanClass) {
                return beanClass.isAnnotationPresent(Bean.class);
            }
        });
    }

    public List<Object> getBeans() {
        return beans;
    }
}
