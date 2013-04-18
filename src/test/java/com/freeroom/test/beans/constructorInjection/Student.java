package com.freeroom.test.beans.constructorInjection;

import com.freeroom.di.annotations.Bean;
import com.freeroom.di.annotations.Inject;

@Bean
public class Student
{
    private Teacher teacher;

    @Inject
    public Student(Teacher teacher) {
        this.teacher = teacher;
    }

    public Teacher getTeacher() {
        return teacher;
    }
}
