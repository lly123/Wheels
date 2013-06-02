package com.freeroom.web;

import com.freeroom.util.Pair;
import com.google.common.base.Optional;
import com.google.gson.Gson;
import com.thoughtworks.paranamer.BytecodeReadingParanamer;
import com.thoughtworks.paranamer.CachingParanamer;
import org.apache.commons.collections.ExtendedProperties;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.VelocityEngine;

import java.io.*;
import java.lang.reflect.Method;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.Map;

import static com.freeroom.util.FuncUtils.each;
import static com.freeroom.util.FuncUtils.reduce;
import static com.google.common.base.Strings.isNullOrEmpty;
import static com.google.common.collect.ImmutableList.copyOf;
import static com.google.common.collect.Iterables.tryFind;
import static com.google.common.collect.Lists.newArrayList;
import static java.lang.Thread.currentThread;
import static org.apache.velocity.runtime.RuntimeConstants.FILE_RESOURCE_LOADER_PATH;

public class Ares
{
    public static final Map<String, String> CONTENT_TYPE = new HashMap<String, String>()
    {
        {
            put(".html", "text/html");
            put(".js", "application/x-javascript");
            put(".css", "text/css");
            put(".ico", "image/icon");
        }
    };

    private final Object controller;
    private final Method method;
    private final Cerberus cerberus;

    public Ares(final Object controller, final Method method, Cerberus cerberus)
    {
        this.controller = controller;
        this.method = method;
        this.cerberus = cerberus;
    }

    public Pair<String, String> getContent()
    {
        try {
            final Object model = method.invoke(controller, resolveArgs());
            assertTypeIsModel(model);

            final String templateName = ((Model)model).getTemplateName();
            if (templateName.equals("res")) {
                return Pair.of(
                        getContentType(((Model)model).getPath()),
                        readFromChannel(getRenderFileChannel((Model)model))
                );
            } else if (templateName.equals("html")) {
                return Pair.of(
                        "text/html",
                        renderHtmlTemplate(
                            readFromChannel(getRenderFileChannel((Model)model)),
                            (Model)model)
                );
            } else if (templateName.equals("vm")) {
                return  Pair.of(
                        "text/html",
                        renderVelocityTemplate((Model)model)
                );
            } else if (templateName.equals("jsonp")) {
                final Optional<Object> callbackFuncName = cerberus.getValue("callback");
                if (callbackFuncName.isPresent()) {
                    final String content = String.format("%s(%s)",
                            callbackFuncName.get(),
                            new Gson().toJson(((Model)model).getMap()), cerberus.getCharset());

                    return Pair.of("application/x-javascript", content);
                }
            }
            return Pair.of("text/plain", "");
        } catch (Exception e) {
            throw new RuntimeException("Get exception when generate content: ", e);
        }
    }

    private FileChannel getRenderFileChannel(final Model model) throws FileNotFoundException
    {
        final String filePath = getClassLoader().getResource(model.getPath()).getFile();
        return new FileInputStream(new File(filePath)).getChannel();
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

    private String renderHtmlTemplate(final String content, final Model model)
    {
        final StringBuilder dataBuilder = new StringBuilder();
        each(model.getMap().entrySet(), entry -> {
            dataBuilder.append(entry.getKey() + "=" + new Gson().toJson(entry.getValue()) + ";");
        });
        final String data = dataBuilder.toString();
        return isNullOrEmpty(data) ? content :
                content.replaceAll("\\[MODEL_DATA\\]", data.substring(0, data.length() - 1));
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

    private String getContentType(final String path)
    {
        Optional<Map.Entry<String, String>> contentType =
                tryFind(CONTENT_TYPE.entrySet(), entry -> path.endsWith(entry.getKey()));
        if (contentType.isPresent()) {
            return contentType.get().getValue();
        }

        return "text/plain";
    }
}
