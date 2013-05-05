package com.freeroom.web;


import com.freeroom.di.BeanContext;
import com.google.common.base.Strings;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;

public class Apollo extends HttpServlet
{
    public static final String CHARSET_NAME = "UTF-8";
    private final BeanContext beanContext;

    public Apollo(final String root)
    {
        this.beanContext = BeanContext.load(root);
        this.beanContext.addBean(Prometheus.class);
    }

    protected void service(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException
    {
        final Hephaestus hephaestus = new Hephaestus(beanContext, req);
        final Cerberus cerberus = new Cerberus(CHARSET_NAME);
        setRequestInformation(req, cerberus);

        final Ares ares = new Ares(hephaestus.getHandler().fst, hephaestus.getHandler().snd, cerberus);
        final String content = ares.getContent();

        if (Strings.isNullOrEmpty(content)) {
            resp.setStatus(404);
            return;
        }

        resp.setStatus(200);
        resp.setContentType("text/html; charset=" + CHARSET_NAME);
        try(final PrintWriter out = new PrintWriter(new OutputStreamWriter(resp.getOutputStream(), CHARSET_NAME), true)) {
            out.write(content);
        }
    }

    private void setRequestInformation(final HttpServletRequest req, final Cerberus cerberus)
    {
        cerberus.add("uri=" + req.getRequestURI());
        cerberus.add("queryString=" + req.getQueryString());
    }
}
