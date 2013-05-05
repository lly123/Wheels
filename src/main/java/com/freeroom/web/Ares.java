package com.freeroom.web;

import com.google.common.base.Optional;
import com.google.common.collect.ImmutableList;
import com.thoughtworks.paranamer.BytecodeReadingParanamer;
import com.thoughtworks.paranamer.CachingParanamer;
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
import static com.freeroom.di.util.FuncUtils.reduce;
import static com.google.common.collect.ImmutableList.copyOf;
import static com.google.common.collect.Lists.newArrayList;
import static java.lang.Thread.currentThread;
import static org.apache.velocity.runtime.RuntimeConstants.FILE_RESOURCE_LOADER_PATH;

public class Ares
{
    private final Object controller;
    private final Method method;
    private final Cerberus cerberus;

    public Ares(final Object controller, final Method method, Cerberus cerberus)
    {
        this.controller = controller;
        this.method = method;
        this.cerberus = cerberus;
    }

    public String getContent()
    {
        try {
            final Object model = method.invoke(controller, resolveArgs());
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

    private Object[] resolveArgs()
    {
        final CachingParanamer pegasus = new CachingParanamer(new BytecodeReadingParanamer());
        final String[] paramNames = pegasus.lookupParameterNames(method);

        return reduce(newArrayList(), copyOf(paramNames), (s, paramName, i) -> {
            final Optional<Object> valueOpt = cerberus.getValue(paramName);

            if (valueOpt.isPresent() && valueOpt.get() instanceof Cerberus) {
                s.add(((Cerberus)valueOpt.get()).fill(method.getParameterTypes()[i]));
            } else {
                s.add(valueOpt.orNull());
            }
            return s;
        }).toArray();
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
