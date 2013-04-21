package com.freeroom.di;

import com.freeroom.di.exceptions.NotUniqueException;
import com.freeroom.test.beans.constructorInjection.subPackage.Caribou;
import com.freeroom.test.beans.constructorInjection.subPackage.Marmot;
import com.freeroom.test.beans.dummy.Dummy;
import com.freeroom.test.beans.fieldInjection.Hedgehog;
import com.freeroom.test.beans.fieldInjection.Squid;
import com.freeroom.test.beans.parallelPackages.packageOne.Rhinoceros;
import com.freeroom.test.beans.parallelPackages.packageOne.subPackage.Hamster;
import com.freeroom.test.beans.parallelPackages.packageThree.Beetle;
import com.freeroom.test.beans.parallelPackages.packageTwo.Antelope;
import com.freeroom.test.beans.sameBeanName.subPackage.Trout;
import com.freeroom.test.beans.sameParent.Ladybug;
import com.freeroom.test.beans.setterInjection.Camel;
import com.freeroom.test.beans.setterInjection.Mackerels;
import com.freeroom.test.beans.setterInjection.subPackage.Penguin;
import com.freeroom.test.beans.setterInjection.subPackage.Raccoon;
import com.google.common.base.Optional;
import org.junit.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

public class BeanContextTest
{
    @Test
    public void should_load_bean_given_bean_with_Bean_annotation()
    {
        BeanContext context = BeanContext.load("com.freeroom.test.beans.requiredScope");
        assertThat(context.getBeans().size(), is(1));
    }

    @Test
    public void should_load_nothing_given_the_package_does_not_exist()
    {
        BeanContext context = BeanContext.load("com.freeroom.test.notExist");
        assertThat(context.getBeans().size(), is(0));
    }

    @Test
    public void should_load_nothing_given_the_package_exists_but_no_beans()
    {
        BeanContext context = BeanContext.load("com.freeroom.test.nothing");
        assertThat(context.getBeans().size(), is(0));
    }

    @Test
    public void should_get_bean_given_class_instance()
    {
        BeanContext context = BeanContext.load("com.freeroom.test.beans.fieldInjection");
        assertThat(context.getBean(Hedgehog.class).isPresent(), is(true));
    }

    @Test
    public void should_get_nothing_given_no_bean_with_class()
    {
        BeanContext context = BeanContext.load("com.freeroom.test.beans.dummy");
        assertThat(context.getBean(Dummy.class).isPresent(), is(false));
    }

    @Test(expected = NotUniqueException.class)
    public void should_throw_NotUniqueException_given_beans_have_same_parent()
    {
        BeanContext context = BeanContext.load("com.freeroom.test.beans.sameParent");
        context.getBean(Ladybug.class);
    }

    @Test
    public void should_get_bean_given_bean_name()
    {
        BeanContext context = BeanContext.load("com.freeroom.test.beans.fieldInjection");
        assertThat(context.getBean("Hedgehog").isPresent(), is(true));
    }

    @Test
    public void should_not_get_bean_given_no_bean_with_this_name()
    {
        BeanContext context = BeanContext.load("com.freeroom.test.beans.dummy");
        assertThat(context.getBean("Dummy").isPresent(), is(false));
    }

    @Test
    public void should_get_bean_given_bean_customized_name()
    {
        BeanContext context = BeanContext.load("com.freeroom.test.beans.fieldInjection");
        assertThat(context.getBean("Monster").isPresent(), is(true));
    }

    @Test(expected = NotUniqueException.class)
    public void should_throw_NotUniqueException_given_beans_have_same_simple_name()
    {
        BeanContext context = BeanContext.load("com.freeroom.test.beans.sameBeanName");
        context.getBean("Trout");
    }

    @Test
    public void should_get_bean_by_canonical_name()
    {
        BeanContext context = BeanContext.load("com.freeroom.test.beans.sameBeanName");

        Optional<?> bean = context.getBean("com.freeroom.test.beans.sameBeanName.subPackage.Trout");

        assertThat(bean.get(), is(instanceOf(Trout.class)));
    }

    @Test(expected = NotUniqueException.class)
    public void should_throw_NotUniqueException_given_beans_have_same_customized_name()
    {
        BeanContext.load("com.freeroom.test.beans.sameBeanCustomizedName");
    }

    @Test
    public void should_resolve_bean_by_constructor_injection()
    {
        BeanContext context = BeanContext.load("com.freeroom.test.beans.constructorInjection.subPackage");

        Optional<?> caribou = context.getBean("Caribou");

        assertThat(((Caribou)caribou.get()).getMarmot(), is(instanceOf(Marmot.class)));
    }

    @Test
    public void should_resolve_bean_by_field_injection()
    {
        BeanContext context = BeanContext.load("com.freeroom.test.beans.fieldInjection");

        Optional<?> hedgehog = context.getBean("Hedgehog");

        assertThat(((Hedgehog)hedgehog.get()).getSquid(), is(instanceOf(Squid.class)));
    }

    @Test
    public void should_resolve_bean_by_setter_injection()
    {
        BeanContext context = BeanContext.load("com.freeroom.test.beans.setterInjection.subPackage");

        Optional<?> penguin = context.getBean("Penguin");

        assertThat(((Penguin)penguin.get()).getRaccoon(), is(instanceOf(Raccoon.class)));
    }

    @Test
    public void should_create_new_beans_given_REQUIRED_scope()
    {
        BeanContext context = BeanContext.load("com.freeroom.test.beans.requiredScope");

        Optional<?> otter1 = context.getBean("Otter");
        Optional<?> otter2 = context.getBean("Otter");

        assertThat(otter1.get(), is(not(sameInstance(otter2.get()))));
    }

    @Test
    public void should_resolve_bean_from_parent_context()
    {
        BeanContext parentContext = BeanContext.load("com.freeroom.test.beans.parallelPackages.packageOne");
        BeanContext context = BeanContext.load("com.freeroom.test.beans.parallelPackages.packageTwo", parentContext);

        Optional<?> antelope = context.getBean("Antelope");

        assertThat(((Antelope)antelope.get()).getRhinoceros(), is(instanceOf(Rhinoceros.class)));
    }

    @Test
    public void should_get_same_bean_from_parent_context()
    {
        BeanContext parentContext = BeanContext.load("com.freeroom.test.beans.parallelPackages.packageOne");
        BeanContext childContext1 = BeanContext.load("com.freeroom.test.beans.parallelPackages.packageTwo", parentContext);
        BeanContext childContext2 = BeanContext.load("com.freeroom.test.beans.parallelPackages.packageThree", parentContext);

        Optional<?> antelope = childContext1.getBean("Antelope");
        Optional<?> beetle = childContext2.getBean("Beetle");

        Rhinoceros rhinoceros = ((Antelope)antelope.get()).getRhinoceros();
        assertThat(((Beetle)beetle.get()).getRhinoceros(), is(sameInstance(rhinoceros)));
    }

    @Test
    public void should_use_self_contained_bean_when_contexts_overlapped()
    {
        BeanContext parentContext = BeanContext.load("com.freeroom.test.beans.parallelPackages.packageOne.subPackage");
        Optional<?> hamster1 = parentContext.getBean("Hamster");

        BeanContext context = BeanContext.load("com.freeroom.test.beans.parallelPackages.packageOne", parentContext);
        Optional<?> hamster2 = context.getBean("Hamster");

        assertThat(hamster1.get(), is(not(sameInstance(hamster2.get()))));
        assertThat(((Rhinoceros)context.getBean("Rhinoceros").get()).getHamster(), is(sameInstance(hamster2.get())));
    }
}
