package com.freeroom.di;

import com.freeroom.di.exceptions.DependencyException;
import com.freeroom.di.exceptions.NotUniqueException;
import com.freeroom.test.beans.beanFactory.Dove;
import com.freeroom.test.beans.beanFactory.Pheasant;
import com.freeroom.test.beans.beanFactory.Toad;
import com.freeroom.test.beans.beanFactory.Tuna;
import com.freeroom.test.beans.constructionParameterInjection.Eel;
import com.freeroom.test.beans.constructionParameterInjection.withName.Turbot;
import com.freeroom.test.beans.constructorInjection.subPackage.Caribou;
import com.freeroom.test.beans.constructorInjection.subPackage.Marmot;
import com.freeroom.test.beans.dependOnFactory.Skunk;
import com.freeroom.test.beans.dummy.Dummy;
import com.freeroom.test.beans.dynamicScope.Dingo;
import com.freeroom.test.beans.fieldInjection.Hedgehog;
import com.freeroom.test.beans.fieldInjection.Squid;
import com.freeroom.test.beans.fieldInjectionByName.withName.Boar;
import com.freeroom.test.beans.parallelPackages.packageFive.Gecko;
import com.freeroom.test.beans.parallelPackages.packageFour.Falcon;
import com.freeroom.test.beans.parallelPackages.packageFour.subPackage.Owl;
import com.freeroom.test.beans.parallelPackages.packageOne.Rhinoceros;
import com.freeroom.test.beans.parallelPackages.packageThree.Beetle;
import com.freeroom.test.beans.parallelPackages.packageTwo.Antelope;
import com.freeroom.test.beans.sameBeanName.subPackage.Trout;
import com.freeroom.test.beans.sameParent.Ladybug;
import com.freeroom.test.beans.sameParent.Termite;
import com.freeroom.test.beans.setterInjection.subPackage.Penguin;
import com.freeroom.test.beans.setterInjection.subPackage.Raccoon;
import com.freeroom.test.beans.setterInjectionByName.Alpaca;
import com.google.common.base.Optional;
import org.junit.BeforeClass;
import org.junit.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

public class BeanContextTest
{
    private static Dingo dingo;

    @BeforeClass
    public static void beforeClass()
    {
        final BeanContext context = BeanContext.load("com.freeroom.test.beans.dynamicScope");
        dingo = (Dingo) context.getBean("Dingo").get();
    }

    @Test
    public void should_load_bean_given_bean_with_Bean_annotation()
    {
        final BeanContext context = BeanContext.load("com.freeroom.test.beans.requiredScope");
        assertThat(context.getBeans().size(), is(1));
    }

    @Test
    public void should_load_nothing_given_the_package_does_not_exist()
    {
        final BeanContext context = BeanContext.load("com.freeroom.test.notExist");
        assertThat(context.getBeans().size(), is(0));
    }

    @Test
    public void should_load_nothing_given_the_package_exists_but_no_beans()
    {
        final BeanContext context = BeanContext.load("com.freeroom.test.nothing");
        assertThat(context.getBeans().size(), is(0));
    }

    @Test
    public void should_get_bean_given_class_instance()
    {
        final BeanContext context = BeanContext.load("com.freeroom.test.beans.fieldInjection");
        assertThat(context.getBean(Hedgehog.class).isPresent(), is(true));
    }

    @Test
    public void should_get_nothing_given_no_bean_with_class()
    {
        final BeanContext context = BeanContext.load("com.freeroom.test.beans.dummy");
        assertThat(context.getBean(Dummy.class).isPresent(), is(false));
    }

    @Test(expected = NotUniqueException.class)
    public void should_throw_NotUniqueException_given_beans_have_same_parent()
    {
        final BeanContext context = BeanContext.load("com.freeroom.test.beans.sameParent");
        context.getBean(Ladybug.class);
    }

    @Test
    public void should_get_bean_given_bean_name()
    {
        final BeanContext context = BeanContext.load("com.freeroom.test.beans.fieldInjection");
        assertThat(context.getBean("Hedgehog").isPresent(), is(true));
    }

    @Test
    public void should_not_get_bean_given_no_bean_with_this_name()
    {
        final BeanContext context = BeanContext.load("com.freeroom.test.beans.dummy");
        assertThat(context.getBean("Dummy").isPresent(), is(false));
    }

    @Test
    public void should_get_bean_given_bean_customized_name()
    {
        final BeanContext context = BeanContext.load("com.freeroom.test.beans.fieldInjection");
        assertThat(context.getBean("Monster").isPresent(), is(true));
    }

    @Test(expected = NotUniqueException.class)
    public void should_throw_NotUniqueException_given_beans_have_same_simple_name()
    {
        final BeanContext context = BeanContext.load("com.freeroom.test.beans.sameBeanName");
        context.getBean("Trout");
    }

    @Test
    public void should_get_bean_by_canonical_name()
    {
        final BeanContext context = BeanContext.load("com.freeroom.test.beans.sameBeanName");

        final Optional<?> bean = context.getBean("com.freeroom.test.beans.sameBeanName.subPackage.Trout");

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
        final BeanContext context = BeanContext.load("com.freeroom.test.beans.constructorInjection.subPackage");

        final Optional<?> caribou = context.getBean("Caribou");

        assertThat(((Caribou)caribou.get()).getMarmot(), is(instanceOf(Marmot.class)));
    }

    @Test
    public void should_resolve_bean_by_constructor_parameter_injection()
    {
        final BeanContext parentContext = BeanContext.load("com.freeroom.test.beans.sameBeanName");
        final BeanContext context = BeanContext.load("com.freeroom.test.beans.constructionParameterInjection", parentContext);

        final Optional<?> eel = context.getBean("Eel");

        assertThat(((Eel)eel.get()).getTrout(), is(instanceOf(com.freeroom.test.beans.sameBeanName.Trout.class)));
    }

    @Test
    public void should_NOT_resolve_bean_by_constructor_parameter_injection()
    {
        final BeanContext grandpaContext = BeanContext.load("com.freeroom.test.beans.requiredScope");
        final BeanContext parentContext = BeanContext.load("com.freeroom.test.beans.sameBeanName", grandpaContext);
        final BeanContext context = BeanContext.load("com.freeroom.test.beans.constructionParameterInjection", parentContext);

        final Optional<?> eel = context.getBean("Eel");

        assertThat(((Eel)eel.get()).getOtter(), is(nullValue()));
    }

    @Test
    public void should_resolve_bean_by_constructor_parameter_injection_with_name()
    {
        final BeanContext parentContext = BeanContext.load("com.freeroom.test.beans.sameParent");
        final BeanContext context = BeanContext.load("com.freeroom.test.beans.constructionParameterInjection.withName", parentContext);

        final Optional<?> turbot = context.getBean("Turbot");

        assertThat(((Turbot)turbot.get()).getLadybug(), is(instanceOf(Termite.class)));
    }

    @Test(expected = NotUniqueException.class)
    public void should_throw_Exception_given_constructor_parameter_injection_with_many_choices()
    {
        final BeanContext parentContext = BeanContext.load("com.freeroom.test.beans.sameParent");
        final BeanContext context = BeanContext.load("com.freeroom.test.beans.constructionParameterInjection.manyChoices", parentContext);

        context.getBean("Mantis");
    }

    @Test
    public void should_resolve_bean_by_field_injection()
    {
        final BeanContext context = BeanContext.load("com.freeroom.test.beans.fieldInjection");

        final Optional<?> hedgehog = context.getBean("Hedgehog");

        assertThat(((Hedgehog)hedgehog.get()).getSquid(), is(instanceOf(Squid.class)));
    }

    @Test
    public void should_resolve_field_injection_by_name()
    {
        final BeanContext parentContext = BeanContext.load("com.freeroom.test.beans.sameParent");
        final BeanContext context = BeanContext.load("com.freeroom.test.beans.fieldInjectionByName.withName", parentContext);

        final Optional<?> boar = context.getBean("Boar");

        assertThat(((Boar)boar.get()).getLadybug(), is(instanceOf(Termite.class)));
    }

    @Test(expected = NotUniqueException.class)
    public void should_throw_exception_given_more_than_one_class_can_be_assigned()
    {
        final BeanContext parentContext = BeanContext.load("com.freeroom.test.beans.sameParent");
        final BeanContext context = BeanContext.load("com.freeroom.test.beans.fieldInjectionByName", parentContext);
        context.getBean("Yak");
    }

    @Test
    public void should_resolve_bean_by_setter_injection()
    {
        final BeanContext context = BeanContext.load("com.freeroom.test.beans.setterInjection.subPackage");

        final Optional<?> penguin = context.getBean("Penguin");

        assertThat(((Penguin)penguin.get()).getRaccoon(), is(instanceOf(Raccoon.class)));
    }

    @Test
    public void should_resolve_setter_injection_by_name()
    {
        final BeanContext parentContext = BeanContext.load("com.freeroom.test.beans.sameParent");
        final BeanContext context = BeanContext.load("com.freeroom.test.beans.setterInjectionByName", parentContext);

        final Optional<?> alpaca = context.getBean("Alpaca");

        assertThat(((Alpaca)alpaca.get()).getLadybug(), is(instanceOf(Ladybug.class)));
    }

    @Test
    public void should_create_new_beans_given_REQUIRED_scope()
    {
        final BeanContext context = BeanContext.load("com.freeroom.test.beans.requiredScope");

        final Optional<?> otter1 = context.getBean("Otter");
        final Optional<?> otter2 = context.getBean("Otter");

        assertThat(otter1.get(), is(not(sameInstance(otter2.get()))));
    }

    @Test
    public void should_resolve_bean_from_parent_context()
    {
        final BeanContext parentContext = BeanContext.load("com.freeroom.test.beans.parallelPackages.packageOne");
        final BeanContext context = BeanContext.load("com.freeroom.test.beans.parallelPackages.packageTwo", parentContext);

        final Optional<?> antelope = context.getBean("Antelope");

        assertThat(((Antelope)antelope.get()).getRhinoceros(), is(instanceOf(Rhinoceros.class)));
    }

    @Test
    public void should_resolve_bean_from_grandpa_context()
    {
        final BeanContext grandpaContext = BeanContext.load("com.freeroom.test.beans.parallelPackages.packageOne");
        final BeanContext parentContext = BeanContext.load("com.freeroom.test.beans.parallelPackages.packageTwo", grandpaContext);
        final BeanContext context = BeanContext.load("com.freeroom.test.beans.parallelPackages.packageFive", parentContext);

        final Optional<?> gecko = context.getBean("Gecko");

        assertThat(((Gecko)gecko.get()).getAntelope().getRhinoceros(), is(instanceOf(Rhinoceros.class)));
    }

    @Test
    public void should_get_same_bean_from_parent_context()
    {
        final BeanContext parentContext = BeanContext.load("com.freeroom.test.beans.parallelPackages.packageOne");
        final BeanContext childContext1 = BeanContext.load("com.freeroom.test.beans.parallelPackages.packageTwo", parentContext);
        final BeanContext childContext2 = BeanContext.load("com.freeroom.test.beans.parallelPackages.packageThree", parentContext);

        final Optional<?> antelope = childContext1.getBean("Antelope");
        final Optional<?> beetle = childContext2.getBean("Beetle");

        final Rhinoceros rhinoceros = ((Antelope)antelope.get()).getRhinoceros();
        assertThat(((Beetle)beetle.get()).getRhinoceros(), is(sameInstance(rhinoceros)));
    }

    @Test
    public void should_use_self_contained_bean_when_contexts_overlapped()
    {
        final BeanContext parentContext = BeanContext.load("com.freeroom.test.beans.parallelPackages.packageOne.subPackage");
        final Optional<?> hamster1 = parentContext.getBean("Hamster");

        final BeanContext context = BeanContext.load("com.freeroom.test.beans.parallelPackages.packageOne", parentContext);
        final Optional<?> hamster2 = context.getBean("Hamster");

        assertThat(hamster1.get(), is(not(sameInstance(hamster2.get()))));
        assertThat(((Rhinoceros)context.getBean("Rhinoceros").get()).getHamster(), is(sameInstance(hamster2.get())));
    }

    @Test
    public void should_get_new_bean_given_bean_is_REQUIRED_scope_in_parent_context()
    {
        final BeanContext parentContext = BeanContext.load("com.freeroom.test.beans.parallelPackages.packageFour.subPackage");
        final BeanContext context = BeanContext.load("com.freeroom.test.beans.parallelPackages.packageFour", parentContext);

        final Owl owl1 = ((Falcon)context.getBean("Falcon").get()).getOwl();
        final Owl owl2 = ((Falcon)context.getBean("Falcon").get()).getOwl();

        assertThat(owl1, is(not(sameInstance(owl2))));
    }

    @Test
    public void should_resolve_beans_with_factory()
    {
        final BeanContext parentContext = BeanContext.load("com.freeroom.test.beans.beanFactory");
        final BeanContext context = BeanContext.load("com.freeroom.test.beans.dependOnFactory", parentContext);

        assertThat(((Skunk)context.getBean("Skunk").get()).getDove(), is(instanceOf(Dove.class)));
        assertThat(((Skunk)context.getBean("Skunk").get()).getPheasant(), is(instanceOf(Pheasant.class)));
        assertThat(((Skunk)context.getBean("Skunk").get()).getToad(), is(instanceOf(Toad.class)));
    }

    @Test
    public void should_get_new_beans_given_beans_are_REQUIRED_scope()
    {
        final BeanContext context = BeanContext.load("com.freeroom.test.beans.beanFactory");

        final Tuna tuna1 = (Tuna) context.getBean("Tuna").get();
        final Tuna tuna2 = (Tuna) context.getBean("Tuna").get();

        assertThat(tuna1, is(not(sameInstance(tuna2))));
        assertThat(tuna1.getPheasant(), is(not(sameInstance(tuna2.getPheasant()))));
    }

    @Test(expected = DependencyException.class)
    public void should_have_no_field_holes_given_dynamic_bean()
    {
        BeanContext.load("com.freeroom.test.beans.dynamicBeanWithWormHoles.withFieldHoles");
    }

    @Test(expected = DependencyException.class)
    public void should_have_no_setter_holes_given_dynamic_bean()
    {
        BeanContext.load("com.freeroom.test.beans.dynamicBeanWithWormHoles.withSetterHoles");
    }

    @Test
    public void should_use_same_backend_instance_given_in_method_ONE()
    {
        dingo.getTapir().add(10);
        dingo.getTapir().add(20);
        assertThat(dingo.getTapir().getSum(), is(30));
    }

    @Test
    public void should_use_same_backend_instance_given_in_method_TWO()
    {
        dingo.getTapir().add(100);
        dingo.getTapir().add(200);
        assertThat(dingo.getTapir().getSum(), is(300));
    }
}
