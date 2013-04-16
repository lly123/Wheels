package com.freeroom.di;

import com.google.common.base.Optional;
import com.google.common.base.Predicate;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;

import static com.google.common.base.Optional.of;
import static com.google.common.collect.Iterables.filter;
import static java.lang.Thread.currentThread;

public class BeanContext {
    private static BeanContext context;

    private List<Object> beans = new ArrayList<>();
    private String packageName;

    private BeanContext(String packageName) {
        this.packageName = packageName;
    }

    public static BeanContext load(String packageName) {
        context = new BeanContext(packageName);
        context.initialize();

        return context;
    }

    private void initialize() {
        try {
            Optional<URL> packagePath = getPackagePath(packageName);
            List<Class> beanClasses = getBeanClasses(packagePath.get());
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

    private Optional<URL> getPackagePath(String packageName) throws IOException {
        Optional<URL> packagePath = Optional.absent();

        Enumeration<URL> resources = getClassLoader().getResources(toPath(packageName));
        if (resources.hasMoreElements()) {
            packagePath = of(resources.nextElement());
        }

        return packagePath;
    }

    private List<Class> getBeanClasses(URL packagePath) throws ClassNotFoundException {
        List<Class> beanNames = new ArrayList<>();
        File[] beanFiles = new File(packagePath.getFile()).listFiles();

        for (File file : beanFiles) {
            beanNames.add(loadClass(file.getName()));
        }

        return beanNames;
    }

    private Class loadClass(String beanFileName) throws ClassNotFoundException {
        return getClassLoader().loadClass(packageName + "." + getBeanName(beanFileName));
    }

    private String getBeanName(String beanFileName) {
        return beanFileName.substring(0, beanFileName.length() - ".class".length());
    }

    private ClassLoader getClassLoader() {
        return currentThread().getContextClassLoader();
    }

    private String toPath(String packageName) {
        return packageName.replaceAll("\\.", "/");
    }

    public List<Object> getBeans() {
        return beans;
    }
}
