package com.freeroom.di.util;

import org.junit.Test;

import java.util.Collection;

import static com.freeroom.di.util.FuncUtils.reduce;
import static com.google.common.collect.Lists.newArrayList;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

public class FuncUtilsTest
{
    @Test
    public void should_sum_integers() {
        Collection<Integer> integers = newArrayList(1, 2, 3);
        Integer sum = reduce(0, integers, new RFunc<Integer, Integer>() {
            @Override
            public Integer call(final Integer s, final Integer v) {
                return s + v;
            }
        });
        assertThat(sum, is(6));
    }
}
