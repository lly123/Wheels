package com.freeroom.test.beans.dynamicScope;

import com.freeroom.di.annotations.Bean;
import com.freeroom.di.annotations.Scope;

@Bean(scope = Scope.Dynamic)
public class Tapir
{
    private int sum = 0;

    public void add(int number)
    {
        sum += number;
    }

    public int getSum()
    {
        return sum;
    }
}
