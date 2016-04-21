# Dynatrace Jenkins Plugin

<table>
<tr>
<td><img src="https://github.com/Dynatrace/Dynatrace-Jenkins-Plugin/blob/master/img/logo/jenkins.png" width="75"></td>
<td>This plugin for Jenkins pulls Test Automation data from Dynatrace AppMon and displays it through charts and tables on the project and build level. No need for additional Dynatrace AppMon library in your build script.</td>
</tr>
</table>

## Installation

### Manual Installation
* in Jenkins, click on "Manage Jenkins" / "Manage Plugins"
* click on the "Advanced" tab
* upload the plugin (dynatrace-dashboard.hpi) in the section "Upload Plugin" 
* restart Jenkins when the installation is complete

### Using Jenkins Update Centre 

not available yet for this version

## Configuration

### Global settings

The global settings for the plugin are located under Manage Jenkins / Configure System / Dynatrace Application Monitoring. The connection to the Dynatrace AppMon Server is configured in this section. The configured user needs to have the right credentials to be able to access the Test Automation REST API.

![global settings](https://github.com/Dynatrace/Dynatrace-Jenkins-Plugin/blob/master/img/conf/global_settings.png)

The advanced section enables you to set a delay before retrieving the test results from the server. Change this settings if you are not getting all the test results in Jenkins.

### Build configuration

In the build configuration (build name / configure), first enable *Dynatrace Application Monitoring* in the **Build Environment** and fill the required fields.

![build environment](https://github.com/Dynatrace/Dynatrace-Jenkins-Plugin/blob/master/img/conf/build_environment.png)

Then, for each test category (Unit Test, Performance Test, Browser Test or Web API Test), you need to add a **build step** to register a test run to the Dynatrace AppMon server before running your tests.

![build step register testrun](https://github.com/Dynatrace/Dynatrace-Jenkins-Plugin/blob/master/img/conf/build_step_register_test_run.png)

The testrun id is available as environment variable which can be passed to the Dynatrace AppMon agent in the build script.

**Example with Ant:**
```xml
<jvmarg value="-agentpath:$/var/lib/dynatrace/agent/lib64/libdtagent.so=name=JavaAgent,
server=localhost:9998,loglevel=warning,optionTestRunIdJava=${dtTestrunID}" />
```

At the end of the build, add the Dynatrace AppMon **post-build action** to retrieve the test results. You can also decide if the test results will change the build status.

![post build action](https://github.com/Dynatrace/Dynatrace-Jenkins-Plugin/blob/master/img/conf/post_build_action.png)

### FAQ

## Do I need to use an additional plug-in for Ant, Maven, MS Build to register my tests?

Not anymore. The registration of the test run is done through the Jenkins Plugin. The test run id ${dtTestrunID} is passed as an environment variable and can be used in your build scripts.

## Can I use variables to set the version information?

Build number and jenkins jobs are passed automatically to Dynatrace. Version (major, minor, revision, milestone) currently needs to be set manually (fields are not mandatory). We are looking into allowing the use of variables to set those fields.

## Why am I getting a SSLHandshakeException when trying to connect through HTTPS?

Dynatrace AppMon Server uses a self-signed certificate per default. Since this certificate doesn't match the URL you are using to access the Server, Jenkins returns an error when trying to connect.

To solve it:
- either deploy a valid SSL certificate on Dynatrace AppMon server 
- or use the Jenkins plug-in  https://wiki.jenkins-ci.org/display/JENKINS/Skip+Certificate+Check+plugin to skip the certificate check
- or (not recommended) connect through HTTP. In that case, you need to uncheck the setting "Accept authentication data only with HTTPS" under "Dynatrace Server / Services / Management"

## Additional Resources

### Dynatrace AppMon Documentation

- [Continuous Delivery & Test Automation](https://community.dynatrace.com/community/pages/viewpage.action?pageId=215161284)
- [Capture Performance Data from Tests](https://community.dynatrace.com/community/display/DOCDT63/Capture+Performance+Data+from+Tests)
- [Integrate Dynatrace in Continous Integration Builds](https://community.dynatrace.com/community/display/DOCDT63/Integrate+Dynatrace+in+Continuous+Integration+Builds)

### Blogs

- [Continuous Performance Validation in Continuous Integration Environments](http://apmblog.dynatrace.com/2013/11/27/continuous-performance-validation-in-continuous-integration-environments/)
- [Software Quality Metrics for your Continuous Delivery Pipeline – Part I](http://apmblog.dynatrace.com/2014/03/13/software-quality-metrics-for-your-continuous-delivery-pipeline-part-i/)
- [Software Quality Metrics for your Continuous Delivery Pipeline – Part II: Database](http://apmblog.dynatrace.com/2014/04/23/database-access-quality-metrics-for-your-continuous-delivery-pipeline/)
- [Software Quality Metrics for your Continuous Delivery Pipeline – Part III – Logging](http://apmblog.dynatrace.com/2014/06/17/software-quality-metrics-for-your-continuous-delivery-pipeline-part-iii-logging/)
- [Automated Performance Analysis for Web API Tests](http://apmblog.dynatrace.com/2014/12/23/automated-performance-analysis-web-api-tests/)

## Problems? Questions? Suggestions?

Please post any problems, questions or suggestions to the Dynatrace Community's [Application Monitoring & UEM Forum](https://answers.dynatrace.com/spaces/146/index.html).

