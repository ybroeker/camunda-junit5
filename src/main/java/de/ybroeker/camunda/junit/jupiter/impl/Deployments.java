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
package de.ybroeker.camunda.junit.jupiter.impl;

import java.io.InputStream;
import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Method;
import java.util.*;
import java.util.function.*;
import java.util.stream.*;

import org.camunda.bpm.engine.ProcessEngine;
import org.camunda.bpm.engine.impl.test.TestHelper;
import org.camunda.bpm.engine.repository.DeploymentBuilder;
import org.camunda.bpm.engine.test.Deployment;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.extension.ExtensionContext;

import static org.junit.platform.commons.support.AnnotationSupport.findAnnotation;

public class Deployments {

    public static boolean hasDeployments(final ExtensionContext extensionContext) {
        return findDeploymentAnnotation(extensionContext).isPresent();
    }

    public static org.camunda.bpm.engine.repository.Deployment loadDeployments(final ExtensionContext extensionContext, final ProcessEngine processEngine) {
        Map<String, InputStream> resources = findResources(extensionContext);

        DeploymentBuilder deploymentBuilder = processEngine.getRepositoryService()
                .createDeployment()
                .name(extensionContext.getUniqueId());

        for (final Map.Entry<String, InputStream> resource : resources.entrySet()) {
            deploymentBuilder.addInputStream(resource.getKey(), resource.getValue());
        }

        return deploymentBuilder.deploy();
    }

    private static Map<String, InputStream> findResources(final ExtensionContext extensionContext) {
        final Optional<Deployment> deployment = findDeploymentAnnotation(extensionContext);
        if (!deployment.isPresent()) {
            return Collections.emptyMap();
        }

        Map<String, InputStream> resources = new HashMap<>();
        resources.putAll(findExplicitResources(extensionContext.getRequiredTestClass(), deployment.get().resources()));
        resources.putAll(findMethodResources(extensionContext.getRequiredTestClass(), extensionContext.getRequiredTestMethod()));
        resources.putAll(findClassResources(extensionContext.getRequiredTestClass()));
        return resources;
    }

    private static Optional<Deployment> findDeploymentAnnotation(final ExtensionContext extensionContext) {
        return Stream.of(extensionContext.getElement(), extensionContext.getTestMethod(), extensionContext.getTestClass())
                .map(element -> findAnnotation(element, Deployment.class))
                .filter(Optional::isPresent).map(Optional::get)
                .findFirst();
    }


    private static Map<String, InputStream> findExplicitResources(Class<?> testClazz, final String... resources) {
        Map<String, InputStream> map = new HashMap<>();

        for (final String resource : resources) {
            findResource(resource, testClazz.getClassLoader()::getResourceAsStream)
                    .ifPresent(stream -> map.put(resource, stream));

            findResource(resource, testClazz::getResourceAsStream)
                    .ifPresent(stream -> map.put(resource, stream));
        }

        return map;
    }

    private static Map<String, InputStream> findMethodResources(Class<?> testClazz, Method method) {
        String resourceName = testClazz.getSimpleName() + "." + method.getName();
        return findResources(method, testClazz, resourceName);
    }

    private static Map<String, InputStream> findClassResources(Class<?> testClazz) {
        final String resourceName = testClazz.getSimpleName();
        return findResources(testClazz, testClazz, resourceName);
    }

    @NotNull
    private static Map<String, InputStream> findResources(final AnnotatedElement element, final Class<?> testClazz, final String resourceName) {
        if (!element.isAnnotationPresent(Deployment.class)) {
            return Collections.emptyMap();
        }

        Map<String, InputStream> map = new HashMap<>();
        for (final String suffix : TestHelper.RESOURCE_SUFFIXES) {
            String resource = resourceName + "." + suffix;
            InputStream stream = testClazz.getResourceAsStream(resource);
            if (stream != null) {
                map.put(resource, stream);
            }
        }
        return map;
    }

    private static Optional<InputStream> findResource(String name, Function<String, InputStream> loader) {
        InputStream stream = loader.apply(name);
        return Optional.ofNullable(stream);
    }

}
