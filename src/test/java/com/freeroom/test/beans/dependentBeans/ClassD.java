package com.freeroom.test.beans.dependentBeans;

import com.freeroom.di.annotations.Bean;
import com.freeroom.di.annotations.Inject;

@Bean
public class ClassD
{
    @Inject
    private ClassC classC;

    private ClassE classE;

    @Inject
    public ClassD(ClassE classE)
    {
        this.classE = classE;
    }

    public ClassC getClassC()
    {
        return classC;
    }

    public ClassE getClassE()
    {
        return classE;
    }
}
