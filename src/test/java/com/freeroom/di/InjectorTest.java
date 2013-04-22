package com.freeroom.di;

import com.freeroom.di.exceptions.ConstructorCycleDependencyException;
import com.freeroom.di.exceptions.NoBeanException;
import com.freeroom.test.beans.constructorInjection.Boa;
import com.freeroom.test.beans.constructorInjection.Pangolin;
import com.freeroom.test.beans.constructorInjection.cycleDependency.Balloonfish;
import com.freeroom.test.beans.constructorInjection.cycleDependency.Swan;
import com.freeroom.test.beans.dependentBeans.Mustang;
import com.freeroom.test.beans.dependentBeans.Jaguar;
import com.freeroom.test.beans.dependentBeans.Ostrich;
import com.freeroom.test.beans.fieldInjection.Squid;
import com.freeroom.test.beans.fieldInjection.Hedgehog;
import org.junit.Test;
import org.mockito.Mockito;

import java.util.Collection;
import java.util.List;

import static com.google.common.collect.Lists.newArrayList;
import static java.util.Arrays.asList;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.Assert.fail;
import static org.mockito.BDDMockito.given;

public class InjectorTest
{
    @Test(expected = NoBeanException.class)
    public void should_throw_NoBeanException_given_can_not_find_bean_in_context()
    {
        final Injector injector = new Injector(givenABeanPackage(Squid.class).getPods());
        injector.resolve();
    }

    @Test
    public void should_have_all_beans_in_context()
    {
        final Injector injector = new Injector(givenABeanPackage(Squid.class, Hedgehog.class).getPods());
        injector.resolve();
    }

    @Test
    public void should_resolve_bean_field_cycle_dependencies()
    {
        final Injector injector = new Injector(givenABeanPackage(Squid.class, Hedgehog.class).getPods());
        final Collection<Pod> pods = injector.resolve();

        assertThat(pods.size(), is(2));

        for (final Pod pod : pods) {
            if (pod.getBeanClass().equals(Hedgehog.class)) {
                Hedgehog hedgehog = (Hedgehog) pod.getBean().get();
                assertThat(hedgehog.getSquid(), is(notNullValue()));
            } else if (pod.getBeanClass().equals(Squid.class)) {
                Squid squid = (Squid) pod.getBean().get();
                assertThat(squid.getDriver(), is(notNullValue()));
            } else {
                fail();
            }
        }
    }

    @Test
    public void should_resolve_beans_with_constructor_injection()
    {
        final Injector injector = new Injector(givenABeanPackage(Pangolin.class, Boa.class).getPods());
        final Collection<Pod> pods = injector.resolve();

        assertThat(pods.size(), is(2));

        for (final Pod pod : pods) {
            if (pod.getBeanClass().equals(Pangolin.class)) {
                final Pangolin pangolin = (Pangolin) pod.getBean().get();
                assertThat(pangolin.getBoa(), is(notNullValue()));
            }
        }
    }

    @Test(expected = ConstructorCycleDependencyException.class)
    public void should_throw_ConstructorCycleDependencyException()
    {
        final Injector injector = new Injector(givenABeanPackage(Swan.class, Balloonfish.class).getPods());
        injector.resolve();
    }

    @Test
    public void should_resolve_bean_again_given_part_of_beans_are_cleaned()
    {
        final Injector injector = new Injector(givenABeanPackage(Mustang.class, Jaguar.class, Ostrich.class).getPods());
        final Collection<Pod> readyPods = injector.resolve();
        cleanBeans(readyPods, asList(Mustang.class, Ostrich.class));

        injector.resolve();

        assertThat(readyPods.size(), is(3));
        for (final Pod pod : readyPods) {
            if (pod.getBean().get() instanceof Mustang) {
                assert_ClassC_is_ready((Mustang) pod.getBean().get());
            } else if (pod.getBean().get() instanceof Jaguar) {
                assert_ClassD_is_ready((Jaguar) pod.getBean().get());
            } else if (pod.getBean().get() instanceof Ostrich) {
                assert_ClassE_is_ready((Ostrich) pod.getBean().get());
            } else {
                fail();
            }
        }
    }

    private void cleanBeans(Collection<Pod> readyPods, Collection<Class<?>> beanClasses)
    {
        for (final Pod readyPod : readyPods) {
            for (final Class<?> beanClass : beanClasses) {
                if (readyPod.getBeanClass().equals(beanClass)) {
                    readyPod.removeBean();
                }
            }
        }
    }

    private void assert_ClassC_is_ready(final Mustang bean)
    {
        assertThat(bean.getJaguar(), is(notNullValue()));
    }

    private void assert_ClassD_is_ready(final Jaguar bean)
    {
        assertThat(bean.getMustang(), is(notNullValue()));
        assertThat(bean.getOstrich(), is(notNullValue()));
    }

    private void assert_ClassE_is_ready(final Ostrich bean)
    {
        assertThat(bean.getMustang(), is(notNullValue()));
        assertThat(bean.getJaguar(), is(notNullValue()));
    }

    private Package givenABeanPackage(Class<?>... classes)
    {
        final List<Pod> pods = newArrayList();

        for (final Class<?> clazz : classes) {
            pods.add(new SoyPod(clazz));
        }

        final Package beanPackage = Mockito.mock(Package.class);
        given(beanPackage.getPods()).willReturn(pods);
        return beanPackage;
    }
}
