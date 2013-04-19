package com.freeroom.di;

import com.freeroom.di.annotations.Bean;
import com.freeroom.di.exceptions.NotUniqueException;
import com.freeroom.di.util.Func;
import com.google.common.base.Optional;
import com.google.common.collect.Lists;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.*;

import static com.freeroom.di.util.FuncUtils.reduce;
import static com.google.common.base.Optional.of;
import static com.google.common.collect.Lists.newArrayList;
import static java.lang.Thread.currentThread;

class Package
{
    public static final String CLASS_FILE_SUFFIX = ".class";
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
        final Stack<File> pathStack = new Stack<>();

        final List<Pod> pods = newArrayList();
        if (packagePath.isPresent()) {
            pathStack.push(new File(packagePath.get().getFile()));
            loadPods(pathStack, pods);
        }
        return pods;
    }

    private void loadPods(Stack<File> pathStack, List<Pod> pods) {
        while (!pathStack.isEmpty()) {
            File path = pathStack.pop();
            loadPodsInPath(pods, path, pathStack);
        }
    }

    private void loadPodsInPath(final List<Pod> pods, final File path, final Stack<File> pathStack)
    {
        final List<File> beanFiles = newArrayList();
        for (File file : path.listFiles()) {
            if (isDirectory(file)) {
                pathStack.push(file);
            } else if(isClassFile(file)) {
                beanFiles.add(file);
            }
        }
        pods.addAll(beansHaveAnnotation(beanFiles));
    }

    private Collection<? extends Pod> beansHaveAnnotation(final List<File> beanFiles)
    {
        return reduce(Lists.<Pod>newArrayList(), beanFiles, new Func<ArrayList<Pod>, File>() {
            @Override
            public ArrayList<Pod> call(final ArrayList<Pod> pods, final File file) {
                try {
                    final Class beanClass = loadClass(packageName, file.getAbsolutePath());
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

    private boolean isClassFile(final File file)
    {
        return file.getAbsolutePath().endsWith(CLASS_FILE_SUFFIX);
    }

    private boolean isDirectory(final File file)
    {
        return file.isDirectory();
    }

    private Class loadClass(final String packageName, final String beanFilePath) throws ClassNotFoundException
    {
        return getClassLoader().loadClass(getBeanClassName(packageName, beanFilePath));
    }

    private String getBeanClassName(final String packageName, final String beanFileName)
    {
        final String restPart = removeThePrefix(toPath(packageName), beanFileName);
        final String beanFullName = toDotSeparate(restPart);
        return beanFullName.substring(0, beanFullName.length() - CLASS_FILE_SUFFIX.length());
    }

    private String toPath(final String packageName)
    {
        return packageName.replaceAll("\\.", "/");
    }

    private String toDotSeparate(final String path) {
        return path.replaceAll("/", "\\.");
    }

    private String removeThePrefix(final String packageName, final String beanFileName) {
        return beanFileName.substring(beanFileName.indexOf(packageName));
    }
}
