# Dynatrace Plugin for Jenkins #

This plugin for Jenkins pulls Test Automation data from a dynaTrace Server and displays it through charts and tables on the project and build level.

## Usage ##

The plugin is used a "Post-Build Action" and has to know

- how to connect to the dynaTrace Server, and 
- how to identify which tests where executed in a build.

In order to do this, two prerequisites that have be fulfilled: 

- On the dynaTrace Server, a dashboard that contains the Test Automation dashlet has be created. This dashboard will be queried through the REST interface from the Jenkins server, so the dynaTrace Server REST interface has to be accessible from the machine running Jenkins.
The dashboard report is limited to the first 100 tests in the Test Automation dashlet by default. If you have more than 100 tests, please adjust the "Maximum Number of lines per table" in the Reporting section of the dashlet properties accordingly.
- In the build that is executed, the DtSetTestInformation call to the dynaTrace Server has set the BUILD_ID provided by Jenkins as the build number. This field is used to identify which tests were executed in a build.

## Configuration ##

To set it up, you can add the "dynaTrace Test Automation" action as a post-build action to your build:

![](https://community.compuwareapm.com/community/download/attachments/137726679/configuration.png?version=1&modificationDate=1379044370370&api=v2)

Here you can enter the connection details as well as the name of the dashboard containing the Test Automation dashlet, and you can test the connection.

In the advanced settings, you have the following options:

- You can change if the build result from the dynaTrace Server should also affect the Jenkins build result - if this is enabled, tests that are considered as volatile or failed (from a functional or performance perspective) by dynaTrace will cause the Jenkins build to be unstable or fail. By default, this feature is enabled
- Depending on the dynaTrace Server load and the volume of tests, it might take a while until the test results are available through the REST interface. By default, the Jenkins plugin will try to get data 6 times, 10 seconds apart. If necessary, you can increase the number of tries here. Please keep in mind that this increases the build duration.

## Maintainer ##
Wolfgang Gottesheim, [wolfgang.gottesheim@dynatrace.com](mailto:wolfgang.gottesheim@dynatrace.com)
