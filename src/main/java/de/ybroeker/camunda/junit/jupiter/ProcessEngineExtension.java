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
import java.util.concurrent.atomic.*;

import de.ybroeker.camunda.junit.jupiter.impl.Deployments;
import de.ybroeker.camunda.junit.jupiter.impl.TestProcessEngineImpl;
import org.camunda.bpm.engine.ProcessEngine;
import org.camunda.bpm.engine.ProcessEngineConfiguration;
import org.camunda.bpm.engine.impl.ProcessEngineImpl;
import org.camunda.bpm.engine.impl.cfg.ProcessEngineConfigurationImpl;
import org.camunda.bpm.engine.impl.test.TestHelper;
import org.camunda.bpm.engine.impl.util.ClockUtil;
import org.camunda.bpm.engine.repository.Deployment;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.extension.*;

import static org.junit.platform.commons.support.AnnotationSupport.isAnnotated;


/**
 * ProcessEngineRule adapted as JUnit 5-Extension.
 *
 * @see org.camunda.bpm.engine.test.ProcessEngineRule
 */
public class ProcessEngineExtension implements BeforeTestExecutionCallback,
                                               AfterTestExecutionCallback,
                                               ParameterResolver {

    public static final String PROCESS_ENGINE_KEY = "PROCESS_ENGINE";

    private static final String DEFAULT_CONFIGURATION_RESOURCE = "camunda.cfg.xml";

    //Per Instance
    private String configurationResource = DEFAULT_CONFIGURATION_RESOURCE;

    //private final boolean ensureCleanAfterTest;


    //Per Testcase
    //TODO: not Threadsafe
    private List<String> additionalDeployments = new ArrayList<String>();

    private final ThreadLocal<@NotNull ProcessEngine> processEngineHolder;

    private final ThreadLocal<AtomicReference<Deployment>> deploymentHolder = ThreadLocal.withInitial(AtomicReference::new);

    public ProcessEngineExtension() {
        this.processEngineHolder = ThreadLocal.withInitial(this::getNewProcessEngine);
    }

    public ProcessEngineExtension(final String configurationResource) {
        this.configurationResource = configurationResource;
        this.processEngineHolder = ThreadLocal.withInitial(this::getNewProcessEngine);
    }

    public ProcessEngineExtension(final ProcessEngine processEngine) {
        this.processEngineHolder = ThreadLocal.withInitial(() -> processEngine);
    }


    @Override
    public void afterTestExecution(final ExtensionContext extensionContext) {
        if (!Deployments.hasDeployments(extensionContext)) {
            return;
        }

        final ProcessEngine processEngine = processEngineHolder.get();

        processEngine.getIdentityService().clearAuthentication();
        processEngine.getProcessEngineConfiguration().setTenantCheckEnabled(true);
        processEngine.getManagementService().unregisterProcessApplication(deploymentHolder.get().get().getId(), true);

        this.deleteDeployments();

        boolean ensureCleanAfterTest = isAnnotated(extensionContext.getElement(), EnsureCleanAfterTest.class);
        if (ensureCleanAfterTest) {
            TestHelper.assertAndEnsureCleanDbAndCache(processEngine);
        }

        TestHelper.resetIdGenerator(getProcessEngineConfiguration(processEngine));
        ClockUtil.reset();

        if (ensureCleanAfterTest) {
            closeProcessEngine();
        }
        getStore(extensionContext).remove(PROCESS_ENGINE_KEY);
    }

    /**
     * Removes Deployments and resets {@code deploymentIdHolder}.
     */
    @SuppressWarnings("PMD.DataflowAnomalyAnalysis")
    private void deleteDeployments() {
        final ProcessEngine processEngine = processEngineHolder.get();
        TestHelper.deleteDeployment(processEngine, deploymentHolder.get().get().getId());
        for (final String additionalDeployment : additionalDeployments) {
            TestHelper.deleteDeployment(processEngine, additionalDeployment);
        }
        deploymentHolder.remove();
    }

    private ProcessEngineConfigurationImpl getProcessEngineConfiguration(final ProcessEngine processEngine) {
        return ((ProcessEngineImpl) processEngine).getProcessEngineConfiguration();
    }

    @Override
    public void beforeTestExecution(final ExtensionContext extensionContext) {
        if (!Deployments.hasDeployments(extensionContext)) {
            return;
        }

        final ProcessEngine processEngine = processEngineHolder.get();
        Objects.requireNonNull(processEngine);
        //TODO: check required HistoryLevel

        Deployment deployment = Deployments.loadDeployments(extensionContext, processEngine);

        this.deploymentHolder.get().set(deployment);
        getStore(extensionContext).put(PROCESS_ENGINE_KEY, processEngine);
    }

    private ExtensionContext.Store getStore(ExtensionContext context) {
        return context.getStore(ExtensionContext.Namespace.create(context.getRequiredTestClass(),
                                                                  context.getRequiredTestMethod()));
    }


    private ProcessEngine getNewProcessEngine() {
        return getNewProcessEngine(this.configurationResource);
    }

    private ProcessEngine getNewProcessEngine(final String configurationResource) {
        final ProcessEngine processEngine = ProcessEngineConfiguration
                .createProcessEngineConfigurationFromResource(configurationResource)
                .setJdbcUrl("jdbc:h2:mem:"+UUID.randomUUID())//anonymous DB for each engine
                .buildProcessEngine();

        return processEngine;
    }

    private void closeProcessEngine() {
        final ProcessEngine processEngine = processEngineHolder.get();
        processEngine.close();
        processEngineHolder.remove();
    }


    public String getConfigurationResource() {
        return configurationResource;
    }

    public void manageDeployment(final Deployment deployment) {
        this.additionalDeployments.add(deployment.getId());
    }


    @Override
    public boolean supportsParameter(final ParameterContext parameterContext, final ExtensionContext extensionContext) {
        return parameterContext.getParameter().getType().equals(TestProcessEngine.class);
    }

    @Override
    public Object resolveParameter(final ParameterContext parameterContext, final ExtensionContext extensionContext) {
        return getProcessEngineExtension();
    }

    private TestProcessEngine getProcessEngineExtension() {
        return new TestProcessEngineImpl(processEngineHolder.get(), deploymentHolder.get());
    }

}
