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

import java.util.Queue;
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
     *
     * @param executionListener the executionListener
     * @param userTaskHolder    the holder for the current UserTask-Name
     * @return the created EmbeddedProcessApplication
     */
    public static EmbeddedProcessApplication processApplication(final ExecutionListener executionListener,
                                                                final AtomicReference<String> userTaskHolder) {

        return processApplication(executionListener, delegateTask -> {
            if (delegateTask.getEventName().equals(TaskListener.EVENTNAME_CREATE)) {
                userTaskHolder.set(delegateTask.getName());
            }
        });
    }

    /**
     * Creates an EmbeddedProcessApplication with the executionListener and
     * adds the TaskName of each created Task to userTaskHolder.
     *
     * @param executionListener the executionListener
     * @param userTaskHolder    the queue, to which UserTask-Names are added
     * @return the created EmbeddedProcessApplication
     */
    public static EmbeddedProcessApplication processApplication(final ExecutionListener executionListener,
                                                                final Queue<String> userTaskHolder) {

        return processApplication(executionListener, delegateTask -> {
            if (delegateTask.getEventName().equals(TaskListener.EVENTNAME_CREATE)) {
                userTaskHolder.add(delegateTask.getName());
            }
        });
    }

}
