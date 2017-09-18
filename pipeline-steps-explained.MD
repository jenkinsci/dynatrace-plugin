Dynatrace AppMon Pipeline steps
-------------------------------
Requirements: Jenkins 2.0+ with <a href="https://wiki.jenkins.io/display/JENKINS/Pipeline+Plugin">Pipeline Plugin</a>.

Note: If you are using Jenkins 2.7.2 (or earlier), please ensure that your <a href="https://wiki.jenkins.io/display/JENKINS/Pipeline+Nodes+and+Processes+Plugin">Pipeline: Nodes and Processes plugin</a> version is not higher than 2.13.

Note: For Maven projects, please use <a href="https://wiki.jenkins.io/display/JENKINS/Maven+Project+Plugin">Maven Integration Plugin</a> version 2.13 (or higher).

Global configuration remains unchanged and is described [here](README.MD) in Global Settings section.

---
#### **appMonBuildEnvironment**
Corresponds to 'Use Dynatrace AppMon to monitor tests' from 'Build environment' section. Wrapper that provides constant Test Run metadata for all 'appMonRegisterTestRun' steps defined.

* Required parameter:
    * `systemProfile`
* Optional parameters:
    * `versionMajor`, `versionMinor`, `versionRevision`, `versionMilestone` - version descriptors aggregated to 'major.minor.revision.build milestone' pattern, where the build number is supplied by Jenkins
    * `marker` - string literal to provide a user-defined marker
    * `recordSession` - boolean flag to enable session recording


##### Usage example:
Minimal set of parameters required:

```groovy
appMonBuildEnvironment(systemProfile: 'test_jenkins') {
    //your pipeline logic
}
```
All TestRun properties provided:
```groovy
appMonBuildEnvironment(marker: '1', recordSession: true, systemProfile: 'test_jenkins', versionMajor: '5', versionMilestone: '2', versionMinor: '4', versionRevision: '3') {
    //your pipeline logic
}
```
---
#### **appMonRegisterTestRun**
Has the same purpose as 'Dynatrace AppMon - Register Test Run' step - registers a Test Run used to differentiate subsets of tests. Uses Test Run's metadata defined in 'appMonBuildEnvironment' wrapper step.

* Required parameter:
    * `category` - category of TestRun (unit, uidriven, performance, webapi)
* Optional parameter:
    * `platform` - platform on which tests are executed

Returns ID of a registered Test Run, which can be passed to your test execution command.
##### Usage example (with Maven):
Using Surefire or Failsafe plugin there's no need to adapt your project's configuration, except for few Jenkins pipeline script changes described below.

```groovy
def testRunId = appMonRegisterTestRun category: 'unit'
bat(<your Maven path> -DargLine="-agentpath:<path to dtagent.dll/dtagent.so>=name=<agent name>,server=<host[:port]>,loglevel=warning,optionTestRunIdJava=${testRunId}" <your Maven goals>/)
```


##### Usage example (with Ant):


Follow the instructions described [here](example-ant-with-jenkins.md) and adapt your pipeline script as follows (assuming `dt_agent_path`, `dt_agent_name` and `dt_server` are configured in your build script):
```groovy
def testRunId = appMonRegisterTestRun category: 'unit'
bat(<your Ant path> -DdtTestrunID=${testRunId} <Ant targets>/)
```
---
#### **appMonPublishTestResults**
Has the same purpose as 'Dynatrace AppMon - Finish Registered Test Runs and Publish Results' -  finish running Test Runs, fetches analysis data from the Dynatrace Server to display a chart with a trend on passed/degraded/failing/... etc. tests and also provides a detailed view for each test case showing the metrics recorded by Dynatrace Application Monitoring.

* No required parameters.
* Optional parameters:
    * `statusNameIfDegraded` - string literal to provide status name for degraded tests (UNSTABLE, FAILURE)
    * `statusNameIfVolatile` - string literal to provide status name for volatile tests (UNSTABLE, FAILURE)
    * `printXmlReportForDebug` - boolean flag to enable printing Xml report for debugging

##### Usage examples:

Without parameters:
```groovy
appMonPublishTestResults()
```
With all parameters:
```groovy
appMonPublishTestResults modifyStatusIfDegraded: true, modifyStatusIfVolatile: true, printXmlReportForDebug: true, statusNameIfDegraded: 'UNSTABLE', statusNameIfVolatile: 'UNSTABLE'
```
---

#### Sample Pipeline script:

```groovy
node {
   def mvnHome
   //Wraps a build and creates enviroment necessary to register test run and publish test results
   appMonBuildEnvironment(systemProfile: 'test_jenkins', recordSession: true) {
          stage('Preparation') {
          // Get some code from a GitHub repository
          git branch: 'master', url: '<repo_address>'
          // Get the Maven tool.
          // ** NOTE: This 'M3' Maven tool must be configured
          // **       in the global configuration.
          mvnHome = tool 'M3'
       }
       stage('Test'){
          //Registers test run
          def testRunId = appMonRegisterTestRun category: 'unit'
          //Inject testRunId into test execution
          bat(/"${mvnHome}\bin\mvn" -Dmaven.test.failure.ignore -DargLine="-agentpath:'<path_to_agent>'=name=agent,server=localhost,loglevel=warning,optionTestRunIdJava=${testRunId}" clean test/)
       }
       stage('Test2'){
          //Registers test run
          def testRunId = appMonRegisterTestRun category: 'performance'
          //Inject testRunId into test execution
          bat(/"${mvnHome}\bin\mvn" -Dmaven.test.failure.ignore -DargLine="-agentpath:'<path_to_agent>'=name=agent,server=localhost,loglevel=warning,optionTestRunIdJava=${testRunId}" clean test/)
       }
       stage('Results') {
          //Obtains results from Dynatrace server and publish in Jenkins
          appMonPublishTestResults printXmlReportForDebug: true
          junit '**/target/surefire-reports/TEST-*.xml'
          archive 'target/*.jar'
       }
}
}
```

#### Pipeline tip:
If you are not familiar with pipeline syntax, you can go to your pipeline project menu and choose 'Pipeline syntax'. It's the best place to start with pipelines in Jenkins.

<img src="/img/conf/pipeline_syntax.png" />