<img src="/img/logo/jenkins.png" width="300" />

# Dynatrace Jenkins Plugin

This plugin for Jenkins pulls Test Automation data from Dynatrace AppMon and displays it through charts and tables on the project and build level.

* [Download Latest Release](https://github.com/jenkinsci/dynatrace-plugin/releases)
* [Install from Jenkins Plugin Center](https://wiki.jenkins-ci.org/display/JENKINS/Dynatrace+Plugin)
* [Jenkins Plugin on the Dynatrace Community](https://community.dynatrace.com/community/display/DL/Test+Automation+Plugin+for+Jenkins)

#### Table of Contents

* [Installation](#installation)  
    * [Using Jenkins Update Center](#update)
    * [Manual Installation](#manual)
* [Configuration](#configuration)
    * [Global Settings](#global)
    * [Build configuration](#build)
    * [Post Build Action](#post)
* [Examples](#examples)
    * [Maven, with Test Run registered in Jenkins](#maven1)
    * [Maven, with Dynatrace Maven plugin](#maven2)
    * [Ant, with Test Run registered in Jenkins](#ant1)
    * [Ant, with Dynatrace Ant plugin](#ant2)
    * [NAnt, with Test Run registered in Jenkins](#nant1)
* [Problems? Questions? Suggestions?](#feedback)
* [Additional Resources](#resources)
    * [Dynatrace AppMon Documentation](#doc)
    * [Recorded Webinar](#webinar)
    * [Blogs](#blogs)


## <a name="installation"/> Installation

### <a name="update"/> Using Jenkins Update Center 

The recommended way of installing the plugin is by the Update Center (plugin directory). Navigate to `Manage Jenkins -> Manage Plugins` page and switch to the `Available` tab. Search for the "Dynatrace" keyword and install the plugin.

### <a name="manual"/> Manual Installation

This procedure is meant for developers who want to install locally built plugin version.

* build the plugin from source using `mvn package` command
* in Jenkins, go to `Manage Jenkins -> Manage Plugins` page
* switch to the `Advanced` tab
* upload the built plugin package from `target/dynatrace-dashboard.hpi` path in the `Upload Plugin` section
* restart Jenkins when the installation is complete

## <a name="configuration"/> Configuration

If you are using Jenkins 2.5 or higher, you need to enable the use of environment variables - see [Jenkins 2.5+ build step execution failed with java.lang.NullPointerException](FAQ.md#jenkins2.5)

### <a name="global"/> Global settings

The global settings for the plugin are located under `Manage Jenkins -> Configure System -> Dynatrace Application Monitoring`. The connection to the Dynatrace AppMon Server is configured in this section. The configured user needs to have the right credentials to be able to access the Test Automation REST API.

![global settings](/img/conf/global_settings.png)

The advanced section enables you to set a delay before retrieving the test results from the server. Change this settings if you are not getting all the test results in Jenkins.

### <a name="build"/> Build configuration

In the build configuration (`<Project> -> Configure`), first enable *Use Dynatrace AppMon to monitor tests* in the `Build Environment` tab and fill the required fields.

![build environment](/img/conf/build_environment.png)

You can then choose one of two options to register Test Run:
* [Option 1](build-config-testrun-jenkins.md), when:
    * you want an easy integration and don't want to adapt your build scripts too much
    * you are OK with defining the version in Jenkins to register the Test Run
* [Option 2](build-config-testrun-maven-ant-gradle.md), when:
    * you don't mind using an additional plug-in in your Ant/Maven scripts
    * you want to re-use the Ant/Maven version to register the Test Run

### <a name="post"/> Post Build Action

At the end of the build, add the Dynatrace AppMon **post-build action** to retrieve the test results. You can also decide if the test results will change the build status.

![post build action](/img/conf/post_build_action.png)

## <a name="examples"/> Examples

<a name="maven1"/>[Passing Test Run id registered from Jenkins to Maven](example-maven-with-jenkins.md)

<a name="maven2"/>[Registering Test Run using Dynatrace Maven plugin](example-maven-with-plugin.md)

<a name="ant1"/>[Passing Test Run id registered from Jenkins to Ant](example-ant-with-jenkins.md)

<a name="ant2"/>[Registering Test Run using Dynatrace Ant plugin](example-ant-with-plugin.md)

<a name="nant1"/>[Passing Test Run id registered from Jenkins to NAnt](example-nant-with-jenkins.md)

## <a name="feedback"/> Problems? Questions? Suggestions?

* [Jenkins Plugin FAQ / Troubleshooting Guide](FAQ.md)
* Post any problems, questions or suggestions to the Dynatrace Community's [Application Monitoring & UEM Forum](https://answers.dynatrace.com/spaces/146/index.html).

## <a name="resources"/>Additional Resources

### <a name="doc"/> Dynatrace AppMon Documentation

- [Continuous Delivery & Test Automation](https://community.dynatrace.com/community/pages/viewpage.action?pageId=215161284)
- [Capture Performance Data from Tests](https://community.dynatrace.com/community/display/DOCDT63/Capture+Performance+Data+from+Tests)
- [Integrate Dynatrace in Continuous Integration Builds](https://community.dynatrace.com/community/display/DOCDT63/Integrate+Dynatrace+in+Continuous+Integration+Builds)

### <a name="webinar"/> Recorded Webinar

- [Online Perf Clinic - Eclipse and Jenkins Integration](https://youtu.be/p4Vh6BWlPjg)
- [Online Perf Clinic - Metrics-Driven Continuous Delivery with Dynatrace Test Automation](https://youtu.be/TXPSDpy7unw)

### <a name="blogs"/> Blogs

- [Continuous Performance Validation in Continuous Integration Environments](http://apmblog.dynatrace.com/2013/11/27/continuous-performance-validation-in-continuous-integration-environments/)
- [Software Quality Metrics for your Continuous Delivery Pipeline – Part I](http://apmblog.dynatrace.com/2014/03/13/software-quality-metrics-for-your-continuous-delivery-pipeline-part-i/)
- [Software Quality Metrics for your Continuous Delivery Pipeline – Part II: Database](http://apmblog.dynatrace.com/2014/04/23/database-access-quality-metrics-for-your-continuous-delivery-pipeline/)
- [Software Quality Metrics for your Continuous Delivery Pipeline – Part III – Logging](http://apmblog.dynatrace.com/2014/06/17/software-quality-metrics-for-your-continuous-delivery-pipeline-part-iii-logging/)
- [Automated Performance Analysis for Web API Tests](http://apmblog.dynatrace.com/2014/12/23/automated-performance-analysis-web-api-tests/)
