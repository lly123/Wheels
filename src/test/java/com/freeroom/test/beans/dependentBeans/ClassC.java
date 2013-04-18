package com.freeroom.test.beans.dependentBeans;

import com.freeroom.di.annotations.Bean;
import com.freeroom.di.annotations.Inject;

@Bean
public class ClassC
{
    private ClassD classD;

    @Inject
    public ClassC(ClassD classD)
    {
        this.classD = classD;
    }

    public ClassD getClassD() {
        return classD;
    }
}
