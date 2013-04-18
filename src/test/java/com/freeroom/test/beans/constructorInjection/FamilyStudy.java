package com.freeroom.test.beans.constructorInjection;

import com.freeroom.di.annotations.Bean;
import com.freeroom.di.annotations.Inject;

@Bean
public class FamilyStudy
{
    @Inject
    public FamilyStudy(Teacher teacher, Student student) {
    }
}
