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
package de.ybroeker.camunda.test;

import java.io.InputStream;
import java.lang.reflect.Method;
import java.util.*;

import org.camunda.bpm.engine.ProcessEngine;
import org.camunda.bpm.engine.impl.test.TestHelper;
import org.camunda.bpm.engine.repository.DeploymentBuilder;
import org.camunda.bpm.engine.test.Deployment;
import org.junit.jupiter.api.extension.ExtensionContext;

import static org.junit.platform.commons.util.AnnotationUtils.findAnnotation;


public class Deployments {

    /*
     * Annotated Method:
     * 1. explicit resources
     * 2. /ClassName.methodName.bpmn
     * Annotated Class:
     * 1. explicit resources
     * 2. ClassName.bpmn
     */


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
        final Deployment deployment = findDeploymentAnnotation(extensionContext);

        Map<String, InputStream> resources = new HashMap<>();
        resources.putAll(findExplicitResources(extensionContext.getRequiredTestClass(), deployment.resources()));
        resources.putAll(findMethodResources(extensionContext.getRequiredTestClass(), extensionContext.getRequiredTestMethod()));
        resources.putAll(findClassResources(extensionContext.getRequiredTestClass()));
        return resources;
    }

    private static Deployment findDeploymentAnnotation(final ExtensionContext extensionContext) {
        final Deployment deployment = findAnnotation(extensionContext.getElement(), Deployment.class)
                .orElseGet(() ->
                                   findAnnotation(extensionContext.getTestClass(), Deployment.class)
                                           .orElseThrow(() -> new IllegalArgumentException("Deployment not present!"))
                );

        return deployment;
    }

    private static Map<String, InputStream> findExplicitResources(Class<?> testClazz, final String... resources) {
        Map<String, InputStream> map = new HashMap<>();

        for (final String resource : resources) {
            {
                final InputStream classPathResource = testClazz.getClassLoader().getResourceAsStream(resource);
                if (classPathResource != null) {
                    map.put(resource, classPathResource);
                }
            }

            {
                InputStream classRelativeResource = testClazz.getResourceAsStream(resource);
                if (classRelativeResource != null) {
                    map.put(resource, classRelativeResource);
                }
            }
        }

        return map;
    }

    private static Map<String, InputStream> findMethodResources(Class<?> testClazz, Method method) {
        if (!method.isAnnotationPresent(Deployment.class)) {
            return Collections.emptyMap();
        }

        Map<String, InputStream> map = new HashMap<>();
        for (final String suffix : TestHelper.RESOURCE_SUFFIXES) {
            String resource = testClazz.getSimpleName() + "." + method.getName() + "." + suffix;
            InputStream stream = testClazz.getResourceAsStream(resource);
            if (stream != null) {
                map.put(resource, stream);
            }
        }
        return map;
    }

    private static Map<String, InputStream> findClassResources(Class<?> testClazz) {
        if (!testClazz.isAnnotationPresent(Deployment.class)) {
            return Collections.emptyMap();
        }

        Map<String, InputStream> map = new HashMap<>();
        for (final String suffix : TestHelper.RESOURCE_SUFFIXES) {
            String resource = testClazz.getSimpleName() + "." + suffix;
            InputStream stream = testClazz.getResourceAsStream(resource);
            if (stream != null) {
                map.put(resource, stream);
            }
        }
        return map;
    }

}
