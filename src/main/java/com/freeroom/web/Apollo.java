package com.freeroom.web;


import com.freeroom.di.BeanContext;
import com.freeroom.util.Pair;
import com.freeroom.persistence.Athena;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.Reader;
import java.nio.CharBuffer;

import static com.google.common.base.Strings.isNullOrEmpty;

public class Apollo extends HttpServlet
{
    public static final String CHARSET_NAME = "UTF-8";
    private final BeanContext beanContext;

    public Apollo(final String root)
    {
        this.beanContext = BeanContext.load(root);
        this.beanContext.addBean(Prometheus.class);
        this.beanContext.addBean(Athena.class);
    }

    protected void service(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException
    {
        final Hephaestus hephaestus = new Hephaestus(beanContext, req);
        final Cerberus cerberus = new Cerberus(CHARSET_NAME);
        setRequestInformation(req, cerberus);

        final Ares ares = new Ares(hephaestus.getHandler().fst, hephaestus.getHandler().snd, cerberus);
        final Pair<String, String> content = ares.getContent();

        if (isNullOrEmpty(content.snd)) {
            resp.setStatus(404);
            return;
        }

        resp.setStatus(200);
        resp.setCharacterEncoding(CHARSET_NAME);
        resp.setContentType(content.fst);
        try(final PrintWriter out = new PrintWriter(new OutputStreamWriter(resp.getOutputStream(), CHARSET_NAME), true)) {
            out.write(content.snd);
        }
    }

    private void setRequestInformation(final HttpServletRequest req, final Cerberus cerberus)
    {
        cerberus.add("uri=" + req.getRequestURI());

        final String queryString = req.getQueryString();
        if (!isNullOrEmpty(queryString)) {
            cerberus.add("queryString=" + queryString);
            cerberus.addValues(queryString);
        }

        cerberus.addValues(readPostData(req));
    }

    private String readPostData(final HttpServletRequest req)
    {
        final StringBuilder stringBuilder = new StringBuilder();
        try {
            final CharBuffer buf = CharBuffer.allocate(1024);
            try(final Reader reader = req.getReader()) {
                while (reader.read(buf) >= 0 ) {
                    buf.flip();
                    stringBuilder.append(buf);
                    buf.clear();
                }
            }
        } catch (IOException ignored) {}
        return stringBuilder.toString();
    }
}
