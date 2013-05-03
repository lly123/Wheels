package com.freeroom.web;

import org.apache.commons.collections.ExtendedProperties;
import org.apache.velocity.Template;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.VelocityEngine;
import org.apache.velocity.runtime.RuntimeConstants;

import java.io.*;
import java.lang.reflect.Method;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.charset.Charset;
import java.util.Map;

import static com.freeroom.di.util.FuncUtils.each;
import static java.lang.Thread.currentThread;
import static org.apache.velocity.runtime.RuntimeConstants.FILE_RESOURCE_LOADER_PATH;

public class Ares
{
    private final Object controller;
    private final Method method;

    public Ares(final Object controller, final Method method)
    {
        this.controller = controller;
        this.method = method;
    }

    public String getContent()
    {
        try {
            final Object model = method.invoke(controller);
            assertTypeIsModel(model);

            final String templateName = ((Model)model).getTemplateName();
            if (templateName.equals("html")) {
                final String file = getClassLoader().getResource(((Model)model).getPath()).getFile();
                return readFromChannel(new FileInputStream(new File(file)).getChannel());
            } else if (templateName.equals("vm")) {
                return renderVelocityTemplate((Model)model);
            }
            return "";
        } catch (Exception e) {
            throw new RuntimeException("Get exception when generate content: ", e);
        }
    }

    private String renderVelocityTemplate(final Model model)
    {
        final VelocityContext context = new VelocityContext();
        setData(context, model.getMap());

        try (final StringWriter writer = new StringWriter()) {
            final ExtendedProperties props = new ExtendedProperties();
            props.setProperty(FILE_RESOURCE_LOADER_PATH, getClassLoader().getResource(".").getFile());

            final VelocityEngine velocityEngine = new VelocityEngine();
            velocityEngine.setExtendedProperties(props);
            velocityEngine.getTemplate(model.getPath()).merge(context, writer);
            return writer.getBuffer().toString();
        } catch (IOException e) {
            throw new RuntimeException("Unexpected exception.", e);
        }
    }

    private void setData(final VelocityContext context, final Map<String, Object> map)
    {
        each(map.entrySet(), entry -> context.put(entry.getKey(), entry.getValue()));
    }

    private String readFromChannel(final FileChannel content)
    {
        final StringBuilder sb = new StringBuilder();
        try {
            final ByteBuffer byteBuffer = ByteBuffer.allocate(1024);
            final Charset charset = Charset.forName("UTF-8");
            while(content.read(byteBuffer) != -1) {
                byteBuffer.flip();
                sb.append(charset.decode(byteBuffer));
                byteBuffer.clear();
            }
            content.close();
        } catch (Exception ignored) {}
        return sb.toString();
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
