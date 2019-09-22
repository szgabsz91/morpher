package com.github.szgabsz91.morpher.core.services;

import org.junit.jupiter.api.Test;

import java.util.Optional;
import java.util.ServiceLoader;
import java.util.function.Function;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class ServiceProviderTest {

    @Test
    @SuppressWarnings("rawtypes")
    public void testGetWithQualifierAndKnownService() {
        Function<Class<?>, Stream<? extends ServiceLoader.Provider<?>>> serviceLoader = clazz -> {
            if (clazz.equals(MyService.class)) {
                ServiceLoader.Provider provider = mock(ServiceLoader.Provider.class);
                when(provider.type()).thenReturn(MyServiceImpl.class);
                when(provider.get()).thenReturn(new MyServiceImpl());
                return Stream.<ServiceLoader.Provider<?>>of(provider);
            }

            throw new IllegalArgumentException("Unknown service: " + clazz);
        };
        ServiceProvider serviceProvider = new ServiceProvider(serviceLoader);
        MyService myService = serviceProvider.get(MyService.class, MyServiceImpl.QUALIFIER);
        assertThat(myService).isNotNull();
        assertThat(myService).isInstanceOf(MyServiceImpl.class);
    }

    @Test
    @SuppressWarnings("rawtypes")
    public void testGetWithQualifierAndUnknownService() {
        Function<Class<?>, Stream<? extends ServiceLoader.Provider<?>>> serviceLoader = clazz -> {
            if (clazz.equals(MyService.class)) {
                ServiceLoader.Provider provider = mock(ServiceLoader.Provider.class);
                when(provider.type()).thenReturn(MyServiceImpl.class);
                when(provider.get()).thenReturn(new MyServiceImpl());
                return Stream.<ServiceLoader.Provider<?>>of(provider);
            }

            throw new IllegalArgumentException("Unknown service: " + clazz);
        };
        ServiceProvider serviceProvider = new ServiceProvider(serviceLoader);
        String qualifier = "unknown";
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> serviceProvider.get(MyService.class, qualifier));
        assertThat(exception).hasMessage("Unknown service: " + MyService.class.getName() + " (" + qualifier + ")");
    }

    @Test
    @SuppressWarnings("rawtypes")
    public void testGetWithNullQualifierAndKnownService() {
        Function<Class<?>, Stream<? extends ServiceLoader.Provider<?>>> serviceLoader = clazz -> {
            if (clazz.equals(MyService.class)) {
                ServiceLoader.Provider provider = mock(ServiceLoader.Provider.class);
                when(provider.type()).thenReturn(MyServiceImpl.class);
                when(provider.get()).thenReturn(new MyServiceImpl());
                return Stream.<ServiceLoader.Provider<?>>of(provider);
            }

            throw new IllegalArgumentException("Unknown service: " + clazz);
        };
        ServiceProvider serviceProvider = new ServiceProvider(serviceLoader);
        MyService myService = serviceProvider.get(MyService.class, null);
        assertThat(myService).isNotNull();
        assertThat(myService).isInstanceOf(MyServiceImpl.class);
    }

    @Test
    public void testGetWithoutQualifierAndUnknownService() {
        Function<Class<?>, Stream<? extends ServiceLoader.Provider<?>>> serviceLoader = clazz -> Stream.empty();
        ServiceProvider serviceProvider = new ServiceProvider(serviceLoader);
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> serviceProvider.get(MultiService.class));
        assertThat(exception).hasMessage("Unknown service: " + MultiService.class.getName());
    }

    @Test
    @SuppressWarnings("rawtypes")
    public void testGetWithoutQualifierAndMultiService() {
        Function<Class<?>, Stream<? extends ServiceLoader.Provider<?>>> serviceLoader = clazz -> {
            if (clazz.equals(MultiService.class)) {
                ServiceLoader.Provider provider1 = mock(ServiceLoader.Provider.class);
                when(provider1.type()).thenReturn(MultiServiceImpl1.class);
                when(provider1.get()).thenReturn(new MultiServiceImpl1());
                ServiceLoader.Provider provider2 = mock(ServiceLoader.Provider.class);
                when(provider2.type()).thenReturn(MultiServiceImpl2.class);
                when(provider2.get()).thenReturn(new MultiServiceImpl2());
                return Stream.<ServiceLoader.Provider<?>>of(provider1, provider2);
            }

            throw new IllegalArgumentException("Unknown service: " + clazz);
        };
        ServiceProvider serviceProvider = new ServiceProvider(serviceLoader);
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> serviceProvider.get(MultiService.class));
        String message = exception.getMessage();
        assertThat(message).startsWith("Multiple implementations found for " + MultiService.class.getName() + ": ");
        assertThat(message).contains(MultiServiceImpl1.class.getName());
        assertThat(message).contains(MultiServiceImpl2.class.getName());
    }

    @Test
    public void testGetQualifierWithInstance() {
        MyService myService = new MyServiceImpl();
        Optional<String> optionalQualifier = ServiceProvider.getQualifier(myService);
        assertThat(optionalQualifier).hasValue(MyServiceImpl.QUALIFIER);
    }

    @Test
    public void testGetQualifierWithClassAndExistingQualifier() {
        Optional<String> optionalQualifier = ServiceProvider.getQualifier(MyServiceImpl.class);
        assertThat(optionalQualifier).hasValue(MyServiceImpl.QUALIFIER);
    }

    @Test
    public void testGetQualifierWithClassAndNonExistentQualifier() {
        Optional<String> optionalQualifier = ServiceProvider.getQualifier(MyService.class);
        assertThat(optionalQualifier).isNotPresent();
    }

    private interface MyService {

    }

    @Qualifier(MyServiceImpl.QUALIFIER)
    private static class MyServiceImpl implements MyService {

        private static final String QUALIFIER = "MyServiceImpl";

    }

    private interface MultiService {

    }

    @Qualifier(MultiServiceImpl1.QUALIFIER)
    private static class MultiServiceImpl1 implements MultiService {

        private static final String QUALIFIER = "MultiServiceImpl1";

    }

    @Qualifier(MultiServiceImpl2.QUALIFIER)
    private static class MultiServiceImpl2 implements MultiService {

        private static final String QUALIFIER = "MultiServiceImpl2";

    }

}
