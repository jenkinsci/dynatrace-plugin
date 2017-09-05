Dynatrace AppMon Pipeline steps
-------------------------------
Requirements: Jenkins 2.0+ with <a href="https://wiki.jenkins.io/display/JENKINS/Pipeline+Plugin">Pipeline Plugin</a> is supported. Older versions could work, but are not officially supported.

Warning: If you are using Jenkins in version lower than 2.7.3(2016/08/31), please ensure you have Pipeline: Nodes and Processes plugin in version at most 2.13.

Warning: To have 'Test result trend' and 'Latest Test Result' links on Maven project main page, please use <a href="https://wiki.jenkins.io/display/JENKINS/Maven+Project+Plugin">Maven Integration Plugin</a> in version 2.13+.

Global configuration is the same as for freestyle projects. It's described [here](README.MD) in Global Settings section.
> When not specified, assume step parameter is literal type.

---
#### **appMonBuildEnvironment**
Corresponds to 'Use Dynatrace AppMon to monitor tests' from 'Build environment' section. It is a wrapper - all other `appMon*` steps should be surrounded by `appMonBuildEnvironment`.

* Required parameter:
    * `systemProfile`
* Optional parameters:
    * `versionMajor`
    * `versionMinor`
    * `versionRevision`
    * `versionMilestone`
    * `marker`
    * `recordSession` [boolean]

##### Examples of usage:
With required parameter only:

```groovy
appMonBuildEnvironment(systemProfile: 'test_jenkins'){
    //your pipeline logic
}
```
With all parameters:
```groovy
appMonBuildEnvironment(marker: '1', recordSession: true, systemProfile: 'test_jenkins', versionMajor: '5', versionMilestone: '2', versionMinor: '4', versionRevision: '3'){
    //your pipeline logic
}
```
---
#### **appMonRegisterTestRun**
Corresponds to 'Dynatrace AppMon - Register Test Run' step from freestyle projects.

* Required parameter:
    * `category` (unit, uidriven, performance, webapi, external)
* Optional parameter:
    * `platform`

Returns registered `testRunId`, which must be injected into test execution.
##### Example of usage (with Maven):
With Maven and Surefire or Failsafe plugin you don't need additional configuration in your project. Everything is done from jenkins pipeline script.

```groovy
def testRunId = appMonRegisterTestRun category: 'unit'
bat(<your Maven path> -DargLine="-agentpath:<path to dtagent.dll/dtagent.so>=name=<agent name>,server=<host[:port]>,loglevel=warning,optionTestRunIdJava=${testRunId}" <your Maven goals>/)
```


##### Example of usage (with Ant):

With Ant it is needed to add few lines to build script, as described [here](example-ant-with-jenkins.md).
Then you could invoke your test from pipeline script this way (assuming `dt_agent_path`, `dt_agent_name` and `dt_server` are configured in your build script):
```groovy
def testRunId = appMonRegisterTestRun category: 'unit'
bat(<your Ant path> -DdtTestrunID=${testRunId} <Ant targets>/)
```
---
#### **appMonPublishTestResults**
Corresponds to 'Dynatrace AppMon - Publish Test Results' post-build step from freestyle projects.

* No required parameters.
* Optional parameters:
    * `modifyStatusIfDegraded`
    * `statusNameIfDegraded`
    * `modifyStatusIfVolatile`
    * `statusNameIfVolatile`
    * `printXmlReportForDebug` [boolean]

##### Examples of usage:

Without parameters:
```groovy
appMonPublishTestResults()
```
With all parameters:
```groovy
appMonPublishTestResults modifyStatusIfDegraded: true, modifyStatusIfVolatile: true, printXmlReportForDebug: true, statusNameIfDegraded: 'UNSTABLE', statusNameIfVolatile: 'UNSTABLE'
```
---
#### Pipeline tip:
If you are not familiar with pipeline syntax, you could go to your pipeline project menu and choose 'Pipeline syntax'. It's the best place to begin work with pipelines in Jenkins.

<img src="/img/conf/pipeline_syntax.png" />