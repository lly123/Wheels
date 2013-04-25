package com.freeroom.test.beans.dynamicBeanWithWormHoles.withSetterHoles;


import com.freeroom.di.annotations.Bean;
import com.freeroom.di.annotations.Inject;
import com.freeroom.di.annotations.Scope;
import com.freeroom.test.beans.dependentBeans.Mustang;

@Bean(scope = Scope.Dynamic)
public class Caterpillar
{
    private Mustang mustang;

    @Inject
    public void setMustang(Mustang mustang) {
        this.mustang = mustang;
    }

    public Mustang getMustang() {
        return mustang;
    }
}
