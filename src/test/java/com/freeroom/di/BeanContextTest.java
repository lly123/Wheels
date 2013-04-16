package com.freeroom.di;

import org.junit.Test;

import java.io.IOException;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.greaterThan;


public class BeanContextTest {
    @Test
    public void should_load_bean_given_bean_with_Bean_Annotation() throws IOException, ClassNotFoundException {
        BeanContext context = BeanContext.load("com.freeroom.beans");
        assertThat(context.getBeans().size(), greaterThan(0));
    }
}
