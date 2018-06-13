package de.ybroeker.camunda.test;

import java.util.concurrent.atomic.AtomicReference;

import org.camunda.bpm.application.impl.EmbeddedProcessApplication;
import org.camunda.bpm.engine.delegate.ExecutionListener;
import org.camunda.bpm.engine.delegate.TaskListener;

public class TestProcessApplicationUtil {
    public static EmbeddedProcessApplication processApplication(final ExecutionListener executionListener,
                                                                final TaskListener taskListener) {

        return new EmbeddedProcessApplication() {
            @Override
            public ExecutionListener getExecutionListener() {
                return executionListener;
            }

            @Override
            public TaskListener getTaskListener() {
                return taskListener;
            }
        };
    }

    /**
     * Creates an EmbeddedProcessApplication with the executionListener and
     * updates userTaskHolder at each created Task with the current Task-Name.
     */
    public static EmbeddedProcessApplication processApplication(final ExecutionListener executionListener,
                                                                final AtomicReference<String> userTaskHolder) {

        return new EmbeddedProcessApplication() {
            @Override
            public ExecutionListener getExecutionListener() {
                return executionListener;
            }

            @Override
            public TaskListener getTaskListener() {
                return delegateTask -> {
                    if (delegateTask.getEventName().equals(TaskListener.EVENTNAME_CREATE)) {
                        userTaskHolder.set(delegateTask.getName());
                    }
                };
            }
        };
    }

}