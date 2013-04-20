package com.freeroom.test.beans.constructorInjection;

import com.freeroom.di.annotations.Bean;
import com.freeroom.di.annotations.Inject;

@Bean
public class Pangolin
{
    private Boa boa;

    @Inject
    public Pangolin(Boa boa) {
        this.boa = boa;
    }

    public Boa getBoa() {
        return boa;
    }
}
