package com.freeroom.web;

import java.io.File;
import java.io.FileInputStream;
import java.lang.reflect.Method;
import java.nio.channels.FileChannel;

import static java.lang.Thread.currentThread;

public class Ares
{
    private final Object controller;
    private final Method method;

    public Ares(final Object controller, final Method method)
    {
        this.controller = controller;
        this.method = method;
    }

    public FileChannel getContent()
    {
        try {
            final Object model = method.invoke(controller);
            assertTypeIsModel(model);

            final String file = getClassLoader().getResource(((Model)model).getPath()).getFile();
            return new FileInputStream(new File(file)).getChannel();
        } catch (Exception e) {
            throw new RuntimeException("Get exception when generate content: ", e);
        }
    }

    private void assertTypeIsModel(final Object obj)
    {
        if (!(obj instanceof Model)) {
            throw new RuntimeException("Only support Model as handler return type.");
        }
    }

    private ClassLoader getClassLoader()
    {
        return currentThread().getContextClassLoader();
    }
}
