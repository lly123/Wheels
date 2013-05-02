package com.freeroom.web;


import com.freeroom.di.BeanContext;
import com.thoughtworks.paranamer.BytecodeReadingParanamer;
import com.thoughtworks.paranamer.CachingParanamer;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.lang.reflect.Method;

public class Apollo extends HttpServlet
{
    private final BeanContext beanContext;

    public Apollo(final String root)
    {
        this.beanContext = BeanContext.load(root);
    }

    protected void service(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException
    {
        req.getMethod();
        req.getRequestURI();
        req.getQueryString();
        req.getAttributeNames();

        new Hephaestus(req);

        try {
            req.getClass().getDeclaredMethod("getSession", boolean.class).getParameters()[0].getName();

            final Method method = HttpServlet.class.getDeclaredMethod("doGet", HttpServletRequest.class, HttpServletResponse.class);

            //final BytecodeReadingParanamer paranamer = new BytecodeReadingParanamer();
            final CachingParanamer paranamer = new CachingParanamer(new BytecodeReadingParanamer());
            paranamer.lookupParameterNames(method);
        } catch (NoSuchMethodException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }
    }
}
