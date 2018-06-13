package de.ybroeker.camunda.test;

import java.util.*;

import org.camunda.bpm.engine.ProcessEngine;
import org.camunda.bpm.engine.ProcessEngineConfiguration;
import org.camunda.bpm.engine.impl.ProcessEngineImpl;
import org.camunda.bpm.engine.impl.cfg.ProcessEngineConfigurationImpl;
import org.camunda.bpm.engine.impl.test.TestHelper;
import org.camunda.bpm.engine.impl.util.ClockUtil;
import org.camunda.bpm.engine.test.Deployment;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.extension.*;

import static org.junit.platform.commons.util.AnnotationUtils.findAnnotation;

/**
 * ProcessEngineRule adapted as JUnit 5-Extension.
 *
 * @see org.camunda.bpm.engine.test.ProcessEngineRule
 */
public class ProcessEngineExtension implements BeforeTestExecutionCallback,
                                               AfterTestExecutionCallback,
                                               ParameterResolver {

    private static String DEFAULT_CONFIGURATION_RESOURCE = "camunda.cfg.xml";

    //Per Instance
    private String configurationResource = "camunda.cfg.xml";

    private final boolean ensureCleanAfterTest;


    //Per Testcase
    //TODO: not Threadsafe
    private List<String> additionalDeployments = new ArrayList<String>();

    private final ThreadLocal<@NotNull ProcessEngine> processEngineHolder;

    private final ThreadLocal<String> deploymentIdHolder = new ThreadLocal<>();


    public ProcessEngineExtension() {
        this(false);
    }

    public ProcessEngineExtension(final boolean ensureCleanAfterTest) {
        this.ensureCleanAfterTest = ensureCleanAfterTest;
        this.processEngineHolder = ThreadLocal.withInitial(this::getNewProcessEngine);
    }

    public ProcessEngineExtension(final String configurationResource) {
        this(configurationResource, false);
    }

    public ProcessEngineExtension(final String configurationResource, final boolean ensureCleanAfterTest) {
        this.configurationResource = configurationResource;
        this.ensureCleanAfterTest = ensureCleanAfterTest;
        this.processEngineHolder = ThreadLocal.withInitial(this::getNewProcessEngine);
    }

    public ProcessEngineExtension(final ProcessEngine processEngine) {
        this(processEngine, false);
    }

    public ProcessEngineExtension(final ProcessEngine processEngine, final boolean ensureCleanAfterTest) {
        this.processEngineHolder = ThreadLocal.withInitial(() -> processEngine);
        this.ensureCleanAfterTest = ensureCleanAfterTest;
    }


    @Override
    public void afterTestExecution(final ExtensionContext extensionContext) {
        final ProcessEngine processEngine = processEngineHolder.get();

        processEngine.getIdentityService().clearAuthentication();
        processEngine.getProcessEngineConfiguration().setTenantCheckEnabled(true);

        this.deleteDeployments();

        if (ensureCleanAfterTest) {
            TestHelper.assertAndEnsureCleanDbAndCache(processEngine);
        }

        TestHelper.resetIdGenerator(getProcessEngineConfiguration(processEngine));
        ClockUtil.reset();

        if (ensureCleanAfterTest) {
            closeProcessEngine();
        }

    }

    /**
     * Removes Deployments and resets {@code deploymentIdHolder}.
     */
    @SuppressWarnings("PMD.DataflowAnomalyAnalysis")
    private void deleteDeployments() {
        final ProcessEngine processEngine = processEngineHolder.get();
        TestHelper.deleteDeployment(processEngine, deploymentIdHolder.get());
        for (final String additionalDeployment : additionalDeployments) {
            TestHelper.deleteDeployment(processEngine, additionalDeployment);
        }
        deploymentIdHolder.remove();
    }

    private ProcessEngineConfigurationImpl getProcessEngineConfiguration(final ProcessEngine processEngine) {
        return ((ProcessEngineImpl) processEngine).getProcessEngineConfiguration();
    }

    @Override
    public void beforeTestExecution(final ExtensionContext extensionContext) {
        final ProcessEngine processEngine = processEngineHolder.get();
        Objects.requireNonNull(processEngine);
        //TODO: check required HistoryLevel

        final Class<?> testClazz = extensionContext.getRequiredTestClass();
        final String testMethodName = extensionContext.getRequiredTestMethod().getName();
        final Deployment deployment = findAnnotation(extensionContext.getElement(), Deployment.class).orElse(null);

        final String deploymentId = TestHelper.annotationDeploymentSetUp(
                processEngine,
                testClazz,
                testMethodName,
                deployment
        );
        this.deploymentIdHolder.set(deploymentId);
    }

    private ProcessEngine getNewProcessEngine() {
        return getNewProcessEngine(this.configurationResource);
    }

    private ProcessEngine getNewProcessEngine(final String configurationResource) {
        final ProcessEngine processEngine = ProcessEngineConfiguration
                .createProcessEngineConfigurationFromResource(configurationResource)
                .setJdbcUrl("jdbc:h2:mem:")//anonymous DB for each engine
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

    public void manageDeployment(final org.camunda.bpm.engine.repository.Deployment deployment) {
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


    @SuppressWarnings("PMD.AccessorMethodGeneration")
    private TestProcessEngine getProcessEngineExtension() {
        return new TestProcessEngine() {

            private final ProcessEngine processEngine = processEngineHolder.get();

            private final String deploymentId = deploymentIdHolder.get();

            @Override
            public ProcessEngine getProcessEngine() {
                return processEngine;
            }

            @Override
            public String getDeploymentId() {
                return deploymentId;
            }
        };
    }
}
