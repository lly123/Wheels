package com.freeroom.di;

import com.freeroom.di.annotations.Bean;
import com.freeroom.di.exceptions.NotUniqueException;
import com.freeroom.di.util.Func;
import com.google.common.base.Optional;
import com.google.common.collect.Lists;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Enumeration;
import java.util.List;

import static com.freeroom.di.util.FuncUtils.reduce;
import static com.google.common.base.Optional.of;
import static com.google.common.collect.ImmutableList.copyOf;
import static com.google.common.collect.Lists.newArrayList;
import static java.lang.Thread.currentThread;

class Package
{
    private final String packageName;
    private final Collection<Pod> pods;

    public Package(final String packageName)
    {
        this.packageName = packageName;
        this.pods = findPods();
    }

    public Collection<Pod> getPods()
    {
        return pods;
    }

    private Collection<Pod> findPods()
    {
        final Optional<URL> packagePath = getPackagePath();
        final List<Pod> pods = newArrayList();

        if (packagePath.isPresent()) {
            List<File> beanFiles = copyOf(new File(packagePath.get().getFile()).listFiles());
            pods.addAll(beansHaveAnnotation(beanFiles));
        }

        return pods;
    }

    private Collection<? extends Pod> beansHaveAnnotation(final List<File> beanFiles)
    {
        return reduce(Lists.<Pod>newArrayList(), beanFiles, new Func<ArrayList<Pod>, File>() {
            @Override
            public ArrayList<Pod> call(final ArrayList<Pod> pods, final File file) {
                try {
                    final Class beanClass = loadClass(packageName, file.getName());
                    if (beanClass.isAnnotationPresent(Bean.class)) {
                        savePod(pods, new Pod(beanClass));
                    }
                } catch (ClassNotFoundException ignored) {}
                return pods;
            }
        });
    }

    private void savePod(final Collection<Pod> pods, final Pod pod)
    {
        if (pods.contains(pod)) {
            throw new NotUniqueException("Beans with same name: " + pod.getBeanName());
        }
        pods.add(pod);
    }

    private Optional<URL> getPackagePath()
    {
        Optional<URL> packagePath = Optional.absent();

        try {
            Enumeration<URL> resources = getClassLoader().getResources(toPath(packageName));
            if (resources.hasMoreElements()) {
                packagePath = of(resources.nextElement());
            }
        } catch (IOException ex) {
            throw new RuntimeException("Can't get resources from IO.", ex);
        }

        return packagePath;
    }

    private ClassLoader getClassLoader()
    {
        return currentThread().getContextClassLoader();
    }

    private String toPath(final String packageName)
    {
        return packageName.replaceAll("\\.", "/");
    }

    private Class loadClass(final String packageName, final String beanFileName) throws ClassNotFoundException
    {
        return getClassLoader().loadClass(packageName + "." + getBeanName(beanFileName));
    }

    private String getBeanName(final String beanFileName)
    {
        return beanFileName.substring(0, beanFileName.length() - ".class".length());
    }
}
