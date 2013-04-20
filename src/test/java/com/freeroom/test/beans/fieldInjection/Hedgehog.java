package com.freeroom.test.beans.fieldInjection;

import com.freeroom.di.annotations.Bean;
import com.freeroom.di.annotations.Inject;

@Bean
public class Hedgehog
{
    @Inject
    private Squid squid;

    private Mosquito mosquito;

    public Squid getSquid() {
        return squid;
    }
}
