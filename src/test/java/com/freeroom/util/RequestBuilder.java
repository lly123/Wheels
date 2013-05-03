package com.freeroom.util;

import javax.servlet.http.HttpServletRequest;

import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;

public class RequestBuilder
{
    private final HttpServletRequest request;

    private RequestBuilder()
    {
        request = mock(HttpServletRequest.class);
    }

    public static RequestBuilder one()
    {
        return new RequestBuilder();
    }

    public RequestBuilder method(final String name)
    {
        given(request.getMethod()).willReturn(name);
        return this;
    }

    public RequestBuilder uri(final String path)
    {
        given(request.getRequestURI()).willReturn(path);
        return this;
    }

    public HttpServletRequest build()
    {
        return request;
    }
}
