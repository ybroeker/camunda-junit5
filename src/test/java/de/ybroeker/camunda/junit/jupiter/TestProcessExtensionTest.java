/*
 *    Copyright 2018 the original author or authors.
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */
package de.ybroeker.camunda.junit.jupiter;

import java.lang.annotation.Annotation;
import java.lang.reflect.*;
import java.util.*;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Function;

import org.assertj.core.api.Assertions;
import org.camunda.bpm.engine.test.Deployment;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.extension.*;


class TestProcessExtensionTest {


    @Test
    void shouldUseDifferentProcessEnginesOnDifferentThreads() throws Exception {
        AtomicReference<TestProcessEngine> firstTestProcessEngine = new AtomicReference<>();
        AtomicReference<TestProcessEngine> secondTestProcessEngine = new AtomicReference<>();

        ProcessEngineExtension processEngineExtension = new ProcessEngineExtension();

        Thread firstTest = new Thread(testExecution(processEngineExtension, firstTestProcessEngine));
        Thread secondTest = new Thread(testExecution(processEngineExtension, secondTestProcessEngine));

        firstTest.start();
        secondTest.start();
        firstTest.join();
        secondTest.join();

        Assertions.assertThat(firstTestProcessEngine.get()).isNotNull();
        Assertions.assertThat(secondTestProcessEngine.get()).isNotNull();

        Assertions.assertThat(firstTestProcessEngine.get().getProcessEngine())
                .isNotSameAs(secondTestProcessEngine.get().getProcessEngine());
    }

    private Runnable testExecution(final ProcessEngineExtension processEngineExtension,
                                   final AtomicReference<TestProcessEngine> parameterStore)
            throws NoSuchMethodException {
        Method testMethod = TestCase.class.getDeclaredMethod("test", TestProcessEngine.class);
        ExtensionContext mockExtensionContext = new MockExtensionContext(testMethod);

        return () -> {
            try {
                TestCase test = new TestCase();
                processEngineExtension.beforeTestExecution(mockExtensionContext);
                ParameterContext mockParameterContext = new MockParameterContext(test, testMethod.getParameters()[0]);
                TestProcessEngine resolvedParam = (TestProcessEngine) processEngineExtension.resolveParameter(mockParameterContext, mockExtensionContext);
                parameterStore.set(resolvedParam);
            } finally {
                processEngineExtension.afterTestExecution(mockExtensionContext);
            }
        };
    }

    static class MockExtensionContext implements ExtensionContext {
        UUID uuid = UUID.randomUUID();

        Store mockStore = new MockStore();

        Method method;

        public MockExtensionContext(final Method method) {
            this.method = method;
        }

        @Override
        public Optional<ExtensionContext> getParent() {
            return Optional.empty();
        }

        @Override
        public ExtensionContext getRoot() {
            return this;
        }

        @Override
        public String getUniqueId() {
            return uuid.toString();
        }

        @Override
        public String getDisplayName() {
            return uuid.toString();

        }

        @Override
        public Set<String> getTags() {
            return Collections.emptySet();
        }

        @Override
        public Optional<AnnotatedElement> getElement() {
            return Optional.of(method);
        }

        @Override
        public Optional<Class<?>> getTestClass() {
            return Optional.of(method.getDeclaringClass());
        }

        @Override
        public Optional<Method> getTestMethod() {
            return Optional.of(method);
        }

        @Override
        public Optional<TestInstance.Lifecycle> getTestInstanceLifecycle() {
            return Optional.of(TestInstance.Lifecycle.PER_METHOD);
        }

        @Override
        public Optional<Object> getTestInstance() {
            return Optional.empty();
        }

        @Override
        public Optional<TestInstances> getTestInstances() {
            return Optional.empty();
        }

        @Override
        public Optional<Throwable> getExecutionException() {
            return Optional.empty();
        }

        @Override
        public Optional<String> getConfigurationParameter(final String key) {
            return Optional.empty();
        }

        @Override
        public void publishReportEntry(final Map<String, String> map) {
            //
        }

        @Override
        public Store getStore(final Namespace namespace) {
            //TODO: namespace
            return mockStore;
        }
    }

    static class MockParameterContext implements ParameterContext {

        Object target;

        Parameter parameter;

        public MockParameterContext(final Object target, final Parameter parameter) {
            this.target = target;
            this.parameter = parameter;
        }

        @Override
        public Parameter getParameter() {
            return parameter;
        }

        @Override
        public int getIndex() {
            // TODO Auto-generated method stub
            return 0;
        }

        @Override
        public Optional<Object> getTarget() {
            return Optional.of(target);
        }

        @Override
        public boolean isAnnotated(final Class<? extends Annotation> annotationType) {
            return parameter.isAnnotationPresent(annotationType);
        }

        @Override
        public <A extends Annotation> Optional<A> findAnnotation(final Class<A> annotationType) {
            return Optional.ofNullable(parameter.getAnnotation(annotationType));
        }

        @Override
        public <A extends Annotation> List<A> findRepeatableAnnotations(final Class<A> annotationType) {
            return Arrays.asList(parameter.getAnnotationsByType(annotationType));

        }
    }

    static class MockStore implements ExtensionContext.Store {

        private Map<Object, Object> map = new HashMap<>();

        @Override
        public Object get(final Object key) {
            return map.get(key);
        }

        @Override
        public <V> V get(final Object key, final Class<V> requiredType) {
            return requiredType.cast(map.get(key));
        }

        @SuppressWarnings("unchecked")
        @Override
        public <K, V> Object getOrComputeIfAbsent(final K key, final Function<K, V> defaultCreator) {
            return map.computeIfAbsent(key, (Function<Object, ?>) defaultCreator);
        }

        @SuppressWarnings("unchecked")
        @Override
        public <K, V> V getOrComputeIfAbsent(final K key, final Function<K, V> defaultCreator, final Class<V> requiredType) {
            return requiredType.cast(map.computeIfAbsent(key, (Function<Object, ?>) defaultCreator));
        }

        @Override
        public void put(final Object key, final Object value) {
            map.put(key, value);
        }

        @Override
        public Object remove(final Object key) {
            return map.remove(key);
        }

        @Override
        public <V> V remove(final Object key, final Class<V> requiredType) {
            return requiredType.cast(map.remove(key));
        }
    }

    static class TestCase {
        @Test
        @Deployment(resources = {"Example_Workflow.bpmn"})
        void test(final TestProcessEngine testProcessEngine) {
        }
    }

}
