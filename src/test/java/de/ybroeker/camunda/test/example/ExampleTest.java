package de.ybroeker.camunda.test.example;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

import de.ybroeker.camunda.test.*;
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
