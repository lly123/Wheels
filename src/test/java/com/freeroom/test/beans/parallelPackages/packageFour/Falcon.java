package com.freeroom.test.beans.parallelPackages.packageFour;

import com.freeroom.di.annotations.Bean;
import com.freeroom.di.annotations.Inject;
import com.freeroom.di.annotations.Scope;
import com.freeroom.test.beans.parallelPackages.packageFour.subPackage.Owl;

@Bean(scope = Scope.Required)
public class Falcon
{
    private Owl owl;

    @Inject
    public void setOwl(final Owl owl)
    {
        this.owl = owl;
    }

    public Owl getOwl()
    {
        return owl;
    }
}
