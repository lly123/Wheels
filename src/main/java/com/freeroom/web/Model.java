package com.freeroom.web;

import java.util.HashMap;
import java.util.Map;

public class Model
{
    private final Map<String, Object> map = new HashMap<>();
    private final String path;
    private final String templateName;

    private Model(final String view)
    {
        this.path = view.substring(view.indexOf(':') + 1);
        this.templateName = view.substring(0, view.indexOf(':'));
    }

    public static Model render(final String view)
    {
        return new Model(view);
    }

    public Model put(final String key, final Object value)
    {
        map.put(key, value);
        return this;
    }

    public String getPath()
    {
        return path;
    }

    public String getTemplateName()
    {
        return templateName;
    }

    public Map<String, Object> getMap()
    {
        return map;
    }
}
