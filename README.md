<img src="/img/logo/jenkins.png" />

# Dynatrace Jenkins Plugin

This plugin for Jenkins pulls Test Automation data from Dynatrace AppMon and displays it through charts and tables on the project and build level.

#### Table of Contents

* [Installation](#installation)  
 * [Manual Installation](#manual)
 * [Using Jenkins Update Centre](#update)
* [Configuration](#configuration)
  * [Global Settings](#global)
  * [Build configuration](#build)
    * [Option 1: Test Run Registration from Jenkins](#option1)
    * [Option 2: Test Run Registration from Maven/Ant/...](#option2)
  * [Post Build Action](#post)
* [Problems? Questions? Suggestions?](#feedback)
* [Additional Resources](#resources)
  * [Dynatrace AppMon Documentation](#doc)
  * [Blogs](#blogs)



<a name="installation"/>
## Installation

<a name="manual"/>
### Manual Installation
* in Jenkins, click on "Manage Jenkins" / "Manage Plugins"
* click on the "Advanced" tab
* upload the plugin (dynatrace-dashboard.hpi) in the section "Upload Plugin" 
* restart Jenkins when the installation is complete

<a name="update"/>
### Using Jenkins Update Centre 

not available yet for this version

<a name="configuration"/>
## Configuration

<a name="global"/>
### Global settings

The global settings for the plugin are located under Manage Jenkins / Configure System / Dynatrace Application Monitoring. The connection to the Dynatrace AppMon Server is configured in this section. The configured user needs to have the right credentials to be able to access the Test Automation REST API.

![global settings](https://github.com/Dynatrace/Dynatrace-Jenkins-Plugin/blob/master/img/conf/global_settings.png)

The advanced section enables you to set a delay before retrieving the test results from the server. Change this settings if you are not getting all the test results in Jenkins.

<a name="build"/>
### Build configuration

In the build configuration (build name / configure), first enable *Dynatrace Application Monitoring* in the **Build Environment** and fill the required fields.

![build environment](https://github.com/Dynatrace/Dynatrace-Jenkins-Plugin/blob/master/img/conf/build_environment.png)

<a name="option1"/>
#### Option 1: Test Run Registration from Jenkins

Use this option when:
* you want an easy integration and don't want to adapt your build scripts too much
* you are OK with defining the version in Jenkins to register the test run

<img src="/img/conf/process_test_run_registration_jenkins.png" />

Then, for each test category (Unit Test, Performance Test, Browser Test or Web API Test), you need to add a **build step** to register a test run to the Dynatrace AppMon server before running your tests.

![build step register testrun](https://github.com/Dynatrace/Dynatrace-Jenkins-Plugin/blob/master/img/conf/build_step_register_test_run.png)

The testrun id is available as environment variable which can be passed to the Dynatraceagent in the build script.

**Example:**
```xml
<jvmarg value="-agentpath:$/var/lib/dynatrace/agent/lib64/libdtagent.so=name=JavaAgent,
server=localhost:9998,loglevel=warning,optionTestRunIdJava=${dtTestrunID}" />
```

<a name="option2"/>
#### Option 2: Test Run Registration from Maven/Ant/...

Use this option when:
* you don't mind using an additional plug-in in your Ant/Maven scripts
* you want to re-use the Ant/Maven version to register the test run

<img src="/img/conf/process_test_run_registration_plugin.png" />

* do not add the Register Test Run step in Jenkins
* install & configure the Ant/Maven/... plug-in in your build script: https://community.dynatrace.com/community/display/DL/Automation+Library+%28Ant%2C+Maven%29+for+Dynatrace
* register the Test Run using the Ant/Maven/... plug-in and make sure to pass the Jenkins build id ${BUILD_ID} in the meta data of the test run. The Jenkins Plug-in will use this build id to retrieve the results.

<a name="post"/>
### Post Build Action

At the end of the build, add the Dynatrace AppMon **post-build action** to retrieve the test results. You can also decide if the test results will change the build status.

![post build action](https://github.com/Dynatrace/Dynatrace-Jenkins-Plugin/blob/master/img/conf/post_build_action.png)

<a name="feedback"/>
## Problems? Questions? Suggestions?

* [Jenkins Plugin FAQ / Troubleshooting Guide](FAQ.md)
* Post any problems, questions or suggestions to the Dynatrace Community's [Application Monitoring & UEM Forum](https://answers.dynatrace.com/spaces/146/index.html).

<a name="resources"/>
## Additional Resources

<a name="doc"/>
### Dynatrace AppMon Documentation

- [Continuous Delivery & Test Automation](https://community.dynatrace.com/community/pages/viewpage.action?pageId=215161284)
- [Capture Performance Data from Tests](https://community.dynatrace.com/community/display/DOCDT63/Capture+Performance+Data+from+Tests)
- [Integrate Dynatrace in Continous Integration Builds](https://community.dynatrace.com/community/display/DOCDT63/Integrate+Dynatrace+in+Continuous+Integration+Builds)

<a name="blogs"/>
### Blogs

- [Continuous Performance Validation in Continuous Integration Environments](http://apmblog.dynatrace.com/2013/11/27/continuous-performance-validation-in-continuous-integration-environments/)
- [Software Quality Metrics for your Continuous Delivery Pipeline – Part I](http://apmblog.dynatrace.com/2014/03/13/software-quality-metrics-for-your-continuous-delivery-pipeline-part-i/)
- [Software Quality Metrics for your Continuous Delivery Pipeline – Part II: Database](http://apmblog.dynatrace.com/2014/04/23/database-access-quality-metrics-for-your-continuous-delivery-pipeline/)
- [Software Quality Metrics for your Continuous Delivery Pipeline – Part III – Logging](http://apmblog.dynatrace.com/2014/06/17/software-quality-metrics-for-your-continuous-delivery-pipeline-part-iii-logging/)
- [Automated Performance Analysis for Web API Tests](http://apmblog.dynatrace.com/2014/12/23/automated-performance-analysis-web-api-tests/)


