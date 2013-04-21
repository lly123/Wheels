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
import static com.google.common.base.Optional.absent;
import static com.google.common.base.Optional.of;
import static com.google.common.collect.Lists.newArrayList;
import static java.lang.Thread.currentThread;

class Package
{
    public static final String CLASS_FILE_SUFFIX = ".class";
    private final String packageName;
    private final Collection<Pod> pods = new ArrayList<>();

    public Package(final String packageName)
    {
        this.packageName = packageName;
        loadPods();
    }

    public Collection<Pod> getPods()
    {
        return pods;
    }

    private void loadPods()
    {
        final Optional<URL> packagePath = getPackagePath();
        final Stack<File> pathStack = new Stack<>();

        if (packagePath.isPresent()) {
            pathStack.push(new File(packagePath.get().getFile()));
            loadPods(pathStack);
        }
    }

    private void loadPods(final Stack<File> pathStack) {
        while (!pathStack.isEmpty()) {
            loadPodsInPath(pathStack);
        }
    }

    private void loadPodsInPath(final Stack<File> pathStack)
    {
        final List<File> beanFiles = newArrayList();
        for (File file : pathStack.pop().listFiles()) {
            if (isDirectory(file)) {
                pathStack.push(file);
            } else if(isClassFile(file)) {
                beanFiles.add(file);
            }
        }
        final Collection<Pod> podsInPath = createPods(beanFiles);
        savePods(podsInPath);
    }

    private void savePods(final Collection<Pod> podsInPath)
    {
        for (final Pod pod : podsInPath) {
            assertNoPodsAreSame(pod);
            pods.add(pod);
        }
    }

    private void assertNoPodsAreSame(final Pod pod)
    {
        if (pods.contains(pod)) {
            throw new NotUniqueException("Beans with same name: " + pod.getBeanName());
        }
    }

    private Collection<Pod> createPods(final List<File> beanFiles)
    {
        return reduce(Lists.<Pod>newArrayList(), beanFiles, new Func<List<Pod>, File>() {
            @Override
            public List<Pod> call(final List<Pod> pods, final File file) {
                try {
                    final Class beanClass = loadClass(packageName, file.getAbsolutePath());
                    if (beanClass.isAnnotationPresent(Bean.class)) {
                        pods.add(new SoyPod(beanClass));
                    }
                } catch (ClassNotFoundException ignored) {}
                return pods;
            }
        });
    }

    private Optional<URL> getPackagePath()
    {
        try {
            Enumeration<URL> resources = getClassLoader().getResources(toPath(packageName));
            if (resources.hasMoreElements()) {
                return of(resources.nextElement());
            }
        } catch (IOException ex) {
            throw new RuntimeException("Can't get resources from IO.", ex);
        }

        return absent();
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
