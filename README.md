# Dynatrace Jenkins Plugin

This plugin for Jenkins pulls Test Automation data from Dynatrace AppMon and displays it through charts and tables on the project and build level.

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
<jvmarg value="-agentpath:${dt_agent_path}=name=${dt_agent_name},
server=${dt_server},loglevel=warning,optionTestRunIdJava=${dtTestrunID}" />
```

At the end of the build, add the Dynatrace AppMon **post-build action** to retrieve the test results. You can also decide if the test results will change the build status.

![post build action](https://github.com/Dynatrace/Dynatrace-Jenkins-Plugin/blob/master/img/conf/post_build_action.png)

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

## Known issues

## License
