package com.freeroom.test.beans.beanFactory;

import com.freeroom.di.annotations.Bean;
import com.freeroom.di.annotations.BeanFactory;

@BeanFactory
public class Hippopotamus
{
    @Bean
    public Dove dove()
    {
        return new Dove();
    }
}
