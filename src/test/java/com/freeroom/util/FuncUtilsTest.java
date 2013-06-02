package com.freeroom.util;

import org.junit.Test;

import java.util.Collection;

import static com.freeroom.util.FuncUtils.reduce;
import static com.google.common.collect.Lists.newArrayList;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

public class FuncUtilsTest
{
    @Test
    public void should_sum_integers()
    {
        Collection<Integer> integers = newArrayList(1, 2, 3);
        Integer sum = reduce(0, integers, (s, v) -> s + v);
        assertThat(sum, is(6));
    }
}
