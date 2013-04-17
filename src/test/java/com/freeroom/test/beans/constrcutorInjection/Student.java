package com.freeroom.test.beans.constrcutorInjection;

import com.freeroom.di.annotations.Bean;
import com.freeroom.di.annotations.Inject;

@Bean
public class Student
{
    @Inject
    public Student(Teacher teacher) {
    }
}
