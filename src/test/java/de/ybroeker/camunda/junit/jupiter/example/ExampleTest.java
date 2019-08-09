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
package de.ybroeker.camunda.junit.jupiter.example;

import java.util.concurrent.atomic.*;

import de.ybroeker.camunda.junit.jupiter.*;
import org.assertj.core.api.Assertions;
import org.camunda.bpm.application.impl.EmbeddedProcessApplication;
import org.camunda.bpm.engine.delegate.TaskListener;
import org.camunda.bpm.engine.test.Deployment;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

@ExtendWith(ProcessEngineExtension.class)
class ExampleTest {
    @Test
    @Deployment(resources = {"Example_Workflow.bpmn"})
    void testShouldInvokeExecutionListenerOnStartAndEndOfProcessInstance(final TestProcessEngine testProcessEngine) {

        final AtomicReference<String> taskIdHolder = new AtomicReference<>(null);

        final AtomicInteger completedUserTasks = new AtomicInteger();

        final EmbeddedProcessApplication processApplication = TestProcessApplicationUtil.processApplication(
                execution -> { },
                delegateTask -> {
                    if (delegateTask.getEventName().equals(TaskListener.EVENTNAME_CREATE)) {
                        taskIdHolder.set(delegateTask.getId());
                    } else if (delegateTask.getEventName().equals(TaskListener.EVENTNAME_COMPLETE)) {
                        completedUserTasks.incrementAndGet();
                    }
                });


        testProcessEngine.registerProcessApplication(processApplication);

        testProcessEngine.getRuntimeService().startProcessInstanceByKey("Example_Process");

        while (taskIdHolder.get() != null) {
            String taskId = taskIdHolder.getAndSet(null);
            //complete UserTask & blocks until next UserTask
            testProcessEngine.getTaskService().complete(taskId);
        }

        Assertions.assertThat(completedUserTasks).hasValue(1);

        testProcessEngine.unregisterProcessApplication();
    }

}
