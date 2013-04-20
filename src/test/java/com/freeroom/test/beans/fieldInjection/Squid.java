package com.freeroom.test.beans.fieldInjection;

import com.freeroom.di.annotations.Bean;
import com.freeroom.di.annotations.Inject;

@Bean("Monster")
public class Squid
{
    @Inject
    private Hedgehog driver;

    public Hedgehog getDriver() {
        return driver;
    }
}
