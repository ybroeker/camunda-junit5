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

import org.camunda.bpm.application.ProcessApplicationInterface;
import org.camunda.bpm.application.ProcessApplicationReference;
import org.camunda.bpm.engine.*;
import org.camunda.bpm.engine.impl.ProcessEngineImpl;
import org.camunda.bpm.engine.impl.cfg.ProcessEngineConfigurationImpl;
import org.camunda.bpm.engine.impl.util.ClockUtil;
import org.camunda.bpm.engine.repository.Deployment;


public interface TestProcessEngine extends ProcessEngineServices {

    /**
     * @see #unregisterProcessApplication(boolean)
     */
    default void unregisterProcessApplication() {
        this.unregisterProcessApplication(true);
    }

    default void unregisterProcessApplication(boolean removeProcessDefinitionsFromCache) {
        this.getManagementService().unregisterProcessApplication(this.getDeploymentId(),
                                                                 removeProcessDefinitionsFromCache);
    }

    default void registerProcessApplication(final ProcessApplicationInterface processApplicationInterface) {
        this.registerProcessApplication(processApplicationInterface.getReference());
    }

    default void registerProcessApplication(final ProcessApplicationReference processApplicationReference) {
        this.getManagementService().registerProcessApplication(this.getDeploymentId(), processApplicationReference);
    }


    default ProcessEngineConfigurationImpl getProcessEngineConfiguration() {
        return ((ProcessEngineImpl) getProcessEngine()).getProcessEngineConfiguration();
    }

    default void setCurrentTime(Date currentTime) {
        ClockUtil.setCurrentTime(currentTime);
    }

    @Override
    default RepositoryService getRepositoryService() {
        return getProcessEngine().getRepositoryService();
    }

    @Override
    default RuntimeService getRuntimeService() {
        return getProcessEngine().getRuntimeService();
    }

    @Override
    default TaskService getTaskService() {
        return getProcessEngine().getTaskService();
    }

    @Override
    default HistoryService getHistoryService() {
        return getProcessEngine().getHistoryService();
    }

    @Override
    default IdentityService getIdentityService() {
        return getProcessEngine().getIdentityService();
    }

    @Override
    default ManagementService getManagementService() {
        return getProcessEngine().getManagementService();
    }

    @Override
    default AuthorizationService getAuthorizationService() {
        return getProcessEngine().getAuthorizationService();
    }

    @Override
    default CaseService getCaseService() {
        return getProcessEngine().getCaseService();
    }

    @Override
    default FormService getFormService() {
        return getProcessEngine().getFormService();
    }

    @Override
    default FilterService getFilterService() {
        return getProcessEngine().getFilterService();
    }

    @Override
    default ExternalTaskService getExternalTaskService() {
        return getProcessEngine().getExternalTaskService();
    }

    @Override
    default DecisionService getDecisionService() {
        return getProcessEngine().getDecisionService();
    }

    ProcessEngine getProcessEngine();

    String getDeploymentId();

    Deployment getDeployment();

}
