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

import java.util.*;

import org.assertj.core.api.Assertions;
import org.camunda.bpm.engine.test.Deployment;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;


@Deployment
@ExtendWith(ProcessEngineExtension.class)
public class ClassResourcesTest {

    @Test
    void deploymentShouldContainResources(final TestProcessEngine testProcessEngine) {
        final String deploymentId = testProcessEngine.getDeploymentId();
        final List<String> deployedResources = testProcessEngine.getRepositoryService().getDeploymentResourceNames(deploymentId);

        Assertions.assertThat(deployedResources).containsExactly("ClassResourcesTest.bpmn");
    }

}
