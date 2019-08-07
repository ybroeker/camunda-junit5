# camunda-junit5

[![Build Status](https://travis-ci.org/ybroeker/camunda-junit5.svg?branch=master)](https://travis-ci.org/ybroeker/camunda-junit5)

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

An example can be found in `de.ybroeker.camunda.test.example.ExampleTest`.

## Maven

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
    <version>0.0.4</version>
    <scope>test</scope>
</dependency>
```
