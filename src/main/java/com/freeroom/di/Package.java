package com.freeroom.di;

import com.freeroom.di.annotations.Bean;
import com.freeroom.di.util.Function;
import com.freeroom.di.util.Iterables;
import com.google.common.base.Optional;
import com.google.common.collect.Lists;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Enumeration;
import java.util.List;

import static com.google.common.base.Optional.of;
import static com.google.common.collect.Lists.newArrayList;
import static java.lang.Thread.currentThread;

class Package
{
    private String packageName;

    public Package(String packageName)
    {
        this.packageName = packageName;
    }

    public List<Pod> getPods() throws IOException, ClassNotFoundException {
        Optional<URL> packagePath = getPackagePath();
        List<Pod> pods = new ArrayList<>();

        if (packagePath.isPresent()) {
            List<File> beanFiles = newArrayList(new File(packagePath.get().getFile()).listFiles());
            pods.addAll(beansWithAnnotation(beanFiles));
        }

        return pods;
    }

    private Collection<? extends Pod> beansWithAnnotation(List<File> beanFiles) {
        return Iterables.reduce(Lists.<Pod>newArrayList(), beanFiles, new Function<ArrayList<Pod>, File>() {
            @Override
            public ArrayList<Pod> call(ArrayList<Pod> pods, File file) {
                try {
                    Class beanClass = loadClass(packageName, file.getName());
                    if (beanClass.isAnnotationPresent(Bean.class)) {
                        pods.add(new Pod(beanClass));
                    }
                } catch (ClassNotFoundException ignored) {}
                return pods;
            }
        });
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
