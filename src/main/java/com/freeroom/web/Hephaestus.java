package com.freeroom.web;

import com.freeroom.di.BeanContext;
import com.freeroom.di.util.Pair;
import com.freeroom.web.exceptions.NotFoundException;
import com.google.common.base.Optional;

import javax.servlet.http.HttpServletRequest;
import java.lang.reflect.Method;

import static com.google.common.collect.ImmutableList.copyOf;
import static com.google.common.collect.Iterables.any;
import static com.google.common.collect.Iterables.tryFind;

public class Hephaestus
{
    private static final String CONTROLLER_BEAN_SUFFIX = "Controller";
    private static final String[] STATIC_FILE_SUFFIX = new String[]{".js", ".css"};
    private final BeanContext beanContext;
    private final HttpServletRequest req;

    public Hephaestus(final BeanContext beanContext, final HttpServletRequest req)
    {
        this.beanContext = beanContext;
        this.req = req;
    }

    public HttpMethod getMethod()
    {
        return Enum.valueOf(HttpMethod.class, req.getMethod().toUpperCase());
    }

    public Pair<Object, Method> getHandler()
    {
        final String[] parts = req.getRequestURI().split("/");

        Object controller;
        String handlerName;
        if (isStaticResource(parts)) {
            controller = beanContext.getBean(Prometheus.class).get();
            handlerName = "index";
        } else {
            controller = getController(getControllerPrefix(parts));
            handlerName = getHandlerName(parts);
        }
        return Pair.of(controller, getControllerMethod(controller, handlerName));
    }

    private boolean isStaticResource(final String[] parts)
    {
        return any(copyOf(STATIC_FILE_SUFFIX), suffix -> parts.length > 0 && parts[parts.length - 1].endsWith(suffix));
    }

    private String getControllerPrefix(final String[] parts)
    {
        return parts.length < 2 ? "Home" : parts[1];
    }

    private String getHandlerName(final String[] parts)
    {
        return parts.length < 3 ? "index" : parts[2];
    }

    private Method getControllerMethod(final Object controller, final String methodName)
    {
        final Optional<Method> method = tryFind(copyOf(controller.getClass().getDeclaredMethods()),
                m -> m.getName().equals(methodName));
        if (!method.isPresent()) {
            throw new NotFoundException("Can't find any handler in " + controller + " for request: " + req.getRequestURI());
        }
        return method.get();
    }

    private Object getController(final String controllerPrefix)
    {
        final Optional<?> controller = beanContext.getBean(upperFirstChar(controllerPrefix) + CONTROLLER_BEAN_SUFFIX);
        if (!controller.isPresent()) {
            throw new NotFoundException("Can't find any controller for request: " + req.getRequestURI());
        }
        return controller.get();
    }

    private String upperFirstChar(final String str)
    {
        return str.substring(0, 1).toUpperCase() + str.substring(1);
    }
}
