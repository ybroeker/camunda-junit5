package de.ybroeker.camunda.test;

import java.util.Date;

import org.camunda.bpm.application.ProcessApplicationInterface;
import org.camunda.bpm.application.ProcessApplicationReference;
import org.camunda.bpm.engine.*;
import org.camunda.bpm.engine.impl.ProcessEngineImpl;
import org.camunda.bpm.engine.impl.cfg.ProcessEngineConfigurationImpl;
import org.camunda.bpm.engine.impl.util.ClockUtil;

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

}
