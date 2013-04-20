package com.freeroom.test.beans.parallelPackages.packageTwo;

import com.freeroom.di.annotations.Bean;
import com.freeroom.test.beans.parallelPackages.packageOne.Rhinoceros;

@Bean
public class Antelope
{
    private Rhinoceros rhinoceros;
}
