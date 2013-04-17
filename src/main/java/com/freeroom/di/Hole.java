package com.freeroom.di;

import java.lang.reflect.Type;

public class Hole
{
    private Class<?> type;
    private HoleType holeType;

    public Hole(Class<?> type, HoleType holeType) {
        this.type = type;
        this.holeType = holeType;
    }

    public Type getType() {
        return type;
    }

    public HoleType getHoleType() {
        return holeType;
    }

    public boolean isFilled() {
        return false;
    }
}
