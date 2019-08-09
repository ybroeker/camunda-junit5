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

import java.util.*;

import org.assertj.core.api.Assertions;
import org.camunda.bpm.engine.test.Deployment;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;


@ExtendWith(ProcessEngineExtension.class)
public class ResourcePathTest {

    @Deployment(resources = "de/ybroeker/camunda/junit/jupiter/Workflow.bpmn")
    @Test
    void shouldLoadFullPath(final TestProcessEngine testProcessEngine) {
        final String deploymentId = testProcessEngine.getDeploymentId();
        final List<String> deployedResources = testProcessEngine.getRepositoryService().getDeploymentResourceNames(deploymentId);

        Assertions.assertThat(deployedResources).containsExactly("de/ybroeker/camunda/junit/jupiter/Workflow.bpmn");
    }

    @Deployment(resources = "/de/ybroeker/camunda/junit/jupiter/Workflow.bpmn")
    @Test
    void shouldLoadAbsolutePath(final TestProcessEngine testProcessEngine) {
        final String deploymentId = testProcessEngine.getDeploymentId();
        final List<String> deployedResources = testProcessEngine.getRepositoryService().getDeploymentResourceNames(deploymentId);

        Assertions.assertThat(deployedResources).containsExactly("/de/ybroeker/camunda/junit/jupiter/Workflow.bpmn");
    }

    @Deployment(resources = "Workflow.bpmn")
    @Test
    void shouldLoadRelativePath(final TestProcessEngine testProcessEngine) {
        final String deploymentId = testProcessEngine.getDeploymentId();
        final List<String> deployedResources = testProcessEngine.getRepositoryService().getDeploymentResourceNames(deploymentId);

        Assertions.assertThat(deployedResources).containsExactly("Workflow.bpmn");
    }

    @Deployment(resources = "Example_Workflow.bpmn")
    @Test
    void shouldLoadRootPath(final TestProcessEngine testProcessEngine) {
        final String deploymentId = testProcessEngine.getDeploymentId();
        final List<String> deployedResources = testProcessEngine.getRepositoryService().getDeploymentResourceNames(deploymentId);

        Assertions.assertThat(deployedResources).containsExactly("Example_Workflow.bpmn");
    }

}
