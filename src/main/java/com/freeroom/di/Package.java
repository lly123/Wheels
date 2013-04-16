package com.freeroom.di;

import com.google.common.base.Optional;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;

import static com.google.common.base.Optional.of;
import static java.lang.Thread.currentThread;

class Package {
    private String packageName;

    public Package(String packageName)
    {
        this.packageName = packageName;
    }

    public List<Class> getClasses() throws IOException, ClassNotFoundException {
        Optional<URL> packagePath = getPackagePath();
        List<Class> beanClasses = new ArrayList<>();

        File[] beanFiles = new File(packagePath.get().getFile()).listFiles();

        for (File file : beanFiles) {
            beanClasses.add(loadClass(packageName, file.getName()));
        }

        return beanClasses;
    }

    private Optional<URL> getPackagePath() throws IOException {
        Optional<URL> packagePath = Optional.absent();

        Enumeration<URL> resources = getClassLoader().getResources(toPath(packageName));
        if (resources.hasMoreElements()) {
            packagePath = of(resources.nextElement());
        }

        return packagePath;
    }

    private ClassLoader getClassLoader() {
        return currentThread().getContextClassLoader();
    }

    private String toPath(String packageName) {
        return packageName.replaceAll("\\.", "/");
    }

    private Class loadClass(String packageName, String beanFileName) throws ClassNotFoundException {
        return getClassLoader().loadClass(packageName + "." + getBeanName(beanFileName));
    }

    private String getBeanName(String beanFileName) {
        return beanFileName.substring(0, beanFileName.length() - ".class".length());
    }
}
