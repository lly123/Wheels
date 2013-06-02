package com.freeroom.util;

import java.lang.reflect.Type;

import static com.google.common.base.Strings.isNullOrEmpty;
import static java.lang.Enum.valueOf;

public class EnumUtil
{
    public static boolean isEnumType(Type type)
    {
        return type instanceof Class && Enum.class.isAssignableFrom((Class)type);
    }

    public static String enumToString(Enum<?> enumVal)
    {
        return enumVal == null ? "" : enumVal.name();
    }

    public static Enum<?> stringToEnum(Class type, String value)
    {
        return isNullOrEmpty(value) ? null : valueOf(type, value);
    }
}
