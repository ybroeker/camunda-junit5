# camunda-junit5

https://github.com/<OWNER>/<REPOSITORY>/actions/workflows/<WORKFLOW_FILE>/badge.svg

[![Build Status](https://github.com/ybroeker/camunda-junit5/actions/workflows/maven.yml/badge.svg)](https://github.com/ybroeker/camunda-junit5/actions/workflows/maven.yml) [![](https://jitpack.io/v/ybroeker/camunda-junit5.svg)](https://jitpack.io/#ybroeker/camunda-junit5)


Adds Support for Camundas `@Deployment` in JUnit 5 environments.

## Usage

Just add the `ProcessEngineExtension` to the test-class and annotate class or methods with `@Deployment`:

```java
@ExtendWith(ProcessEngineExtension.class)
class ExampleTest {
    
    @Test
    @Deployment(resources = {"Example_Workflow.bpmn"})
    void example(final TestProcessEngine testProcessEngine) {
        
    }
}
```

It allows to add `TestProcessEngine` as method-parameter,  which provides access to repositories, services and ProcessEngine, and allows registration of ProcessApplications

An example can be found in `de.ybroeker.camunda.junit.jupiter.example.ExampleTest`.

## Maven

### Jitpack

Add jitpack-repository:

```xml
<repositories>
    <repository>
        <id>jitpack.io</id>
        <url>https://jitpack.io</url>
        <snapshots>
            <enabled>true</enabled>
        </snapshots>
    </repository>
</repositories>
```

Add dependency:

```xml
<dependency>
    <groupId>com.github.ybroeker</groupId>
    <artifactId>camunda-junit5</artifactId>
    <version>0.0.8</version>
    <scope>test</scope>
</dependency>
```
