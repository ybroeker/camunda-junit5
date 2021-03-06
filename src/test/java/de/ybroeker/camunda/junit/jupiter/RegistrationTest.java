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

import de.ybroeker.camunda.junit.jupiter.TestProcessEngine.Registration;
import org.camunda.bpm.application.impl.EmbeddedProcessApplication;
import org.camunda.bpm.engine.test.Deployment;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import static de.ybroeker.camunda.junit.jupiter.TestProcessApplicationUtil.processApplication;
import static org.assertj.core.api.Assertions.assertThat;


@ExtendWith(ProcessEngineExtension.class)
public class RegistrationTest {

    @Test
    @SuppressWarnings("try")
    @Deployment(resources = "Example_Workflow.bpmn")
    void shouldUnregisterProcessApplication(final TestProcessEngine testProcessEngine) {
        final EmbeddedProcessApplication processApplication = processApplication(execution -> {}, delegateTask -> {});

        try (Registration r = testProcessEngine.registerProcessApplication(processApplication)) {
            testProcessEngine.getRuntimeService().startProcessInstanceByKey("Example_Process");
        }

        assertThat(testProcessEngine.getProcessEngineConfiguration().getProcessApplicationManager().hasRegistrations())
                .isFalse();
    }

}
