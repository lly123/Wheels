package com.freeroom.di;

import org.junit.Test;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

public class PackageTest
{
    @Test
    public void should_load_beans_in_a_package()
    {
        Package beanPackage = new Package("com.freeroom.test.beans.fieldInjection");
        assertThat(beanPackage.getPods().size(), is(3));
    }
}
