package com.freeroom.test.beans.dynamicBeanWithWormHoles.withFieldHoles;

import com.freeroom.di.annotations.Bean;
import com.freeroom.di.annotations.Inject;
import com.freeroom.di.annotations.Scope;
import com.freeroom.test.beans.dependentBeans.Jaguar;

@Bean(scope = Scope.Dynamic)
public class Cockroach
{
    @Inject
    private Jaguar jaguar;
}
