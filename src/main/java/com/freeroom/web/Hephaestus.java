package com.freeroom.web;

import javax.servlet.http.HttpServletRequest;

public class Hephaestus
{
    private final HttpServletRequest req;

    public Hephaestus(final HttpServletRequest req)
    {
        this.req = req;
    }

    public HttpMethod getMethod()
    {
        return Enum.valueOf(HttpMethod.class, req.getMethod().toUpperCase());
    }
}
