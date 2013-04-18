package com.freeroom.test.beans.dependentBeans;

import com.freeroom.di.annotations.Bean;
import com.freeroom.di.annotations.Inject;

@Bean
public class ClassE
{
    @Inject
    private ClassC classC;

    @Inject
    private ClassD classD;

    public ClassC getClassC()
    {
        return classC;
    }

    public ClassD getClassD()
    {
        return classD;
    }
}
