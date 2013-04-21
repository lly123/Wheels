package com.freeroom.test.beans.beanFactory;

import com.freeroom.di.annotations.Bean;
import com.freeroom.di.annotations.BeanFactory;
import com.freeroom.di.annotations.Scope;

@BeanFactory
public class Hippopotamus
{
    @Bean
    public Dove dove()
    {
        return new Dove();
    }

    @Bean("toad")
    public Toad toad()
    {
        return new Toad();
    }

    @Bean(scope = Scope.Required)
    public Pheasant pheasant()
    {
        return new Pheasant();
    }
}
