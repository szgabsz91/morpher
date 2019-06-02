package com.github.szgabsz91.morpher.core.services;

import org.junit.jupiter.api.Test;

import java.lang.reflect.InvocationTargetException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class ClassBasedServiceProviderTest {

    @Test
    public void testType() {
        ClassBasedServiceProvider<AccessibleService> provider = new ClassBasedServiceProvider<>(AccessibleService.class);
        Class<? extends AccessibleService> type = provider.type();
        assertThat(type).isEqualTo(AccessibleService.class);
    }

    @Test
    public void testGetWithAccessibleConstructor() {
        ClassBasedServiceProvider<AccessibleService> provider = new ClassBasedServiceProvider<>(AccessibleService.class);
        AccessibleService service = provider.get();
        assertThat(service).isNotNull();
    }

    @Test
    public void testGetWithPrivateConstructor() {
        ClassBasedServiceProvider<PrivateService> provider = new ClassBasedServiceProvider<>(PrivateService.class);
        IllegalStateException exception = assertThrows(IllegalStateException.class, provider::get);
        assertThat(exception).hasCauseExactlyInstanceOf(IllegalAccessException.class);
    }

    @Test
    public void testGetWithNoDefaultConstructor() {
        ClassBasedServiceProvider<ComplexService> provider = new ClassBasedServiceProvider<>(ComplexService.class);
        IllegalStateException exception = assertThrows(IllegalStateException.class, provider::get);
        assertThat(exception).hasCauseExactlyInstanceOf(NoSuchMethodException.class);
    }

    @Test
    public void testGetWithThrowingConstructor() {
        ClassBasedServiceProvider<ProblematicService> provider = new ClassBasedServiceProvider<>(ProblematicService.class);
        IllegalStateException exception = assertThrows(IllegalStateException.class, provider::get);
        assertThat(exception).hasCauseExactlyInstanceOf(InvocationTargetException.class);
    }

    @Test
    public void testGetWithAbstractClass() {
        ClassBasedServiceProvider<AbstractService> provider = new ClassBasedServiceProvider<>(AbstractService.class);
        IllegalStateException exception = assertThrows(IllegalStateException.class, provider::get);
        assertThat(exception).hasCauseExactlyInstanceOf(InstantiationException.class);
    }

    static class AccessibleService {

    }

    private static class PrivateService {

    }

    private static class ComplexService {

        public ComplexService(int parameter) {

        }

    }

    private static class ProblematicService {

        public ProblematicService() {
            throw new RuntimeException();
        }

    }

    static abstract class AbstractService {

    }

}
