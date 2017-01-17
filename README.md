<img src="/img/logo/jenkins.png" width="300" />

# Dynatrace Jenkins Plugin

This plugin for Jenkins pulls Test Automation data from Dynatrace AppMon and displays it through charts and tables on the project and build level.

* Download Latest Release: https://github.com/jenkinsci/dynatrace-plugin/releases
* Install from Jenkins Plugin Center: https://wiki.jenkins-ci.org/display/JENKINS/Dynatrace+Plugin
* Jenkins Plugin on the Dynatrace Community: https://community.dynatrace.com/community/display/DL/Test+Automation+Plugin+for+Jenkins

Special thanks to Wolfgang Gottesheim who contributed to the first version of this plugin.

#### Table of Contents

* [Installation](#installation)  
  * [Using Jenkins Update Centre](#update)
  * [Manual Installation](#manual)
* [Configuration](#configuration)
  * [Global Settings](#global)
  * [Build configuration](#build)
    * [Option 1: Test Run Registration from Jenkins](#option1)
    * [Option 2: Test Run Registration from Maven/Ant/...](#option2)
  * [Post Build Action](#post)
* [Examples](#examples)
 * [Maven](#maven)
    * [Option 1: Test Run Registration from Jenkins](#maven1)
    * [Option 2: Test Run Registration from Maven](#maven2)
 * [Ant](#ant)
    * [Option 1: Test Run Registration from Jenkins](#ant1)
    * [Option 2: Test Run Registration from Ant](#ant2)
 *  [NAnt](#ant)
    * [Option 1: Test Run Registration from Jenkins](#nant1)
* [Problems? Questions? Suggestions?](#feedback)
* [Additional Resources](#resources)
  * [Dynatrace AppMon Documentation](#doc)
  * [Recorded Webinar](#webinar)
  * [Blogs](#blogs)



<a name="installation"/>
## Installation

<a name="update"/>
### Using Jenkins Update Centre 

The recommended way of installing the plugin is by the Update Centre. Simply search for the "Dynatrace" keyword and install the plugin with a single click.

<a name="manual"/>
### Manual Installation

This procedure is meant for developers who want to install locally built plugin version.

* build the plugin from source using `mvn package` command
* in Jenkins, go to "Manage Jenkins" / "Manage Plugins" page
* switch to the "Advanced" tab
* upload the built plugin package from `target/dynatrace-dashboard.hpi` path in the "Upload Plugin" section
* restart Jenkins when the installation is complete

<a name="configuration"/>
## Configuration

If you are using Jenkins 2.5 or higher, you need to enable the use of environment variables - see [Jenkins 2.5+ build step execution failed with java.lang.NullPointerException](FAQ.md#jenkins2.5)

<a name="global"/>
### Global settings

The global settings for the plugin are located under Manage Jenkins / Configure System / Dynatrace Application Monitoring. The connection to the Dynatrace AppMon Server is configured in this section. The configured user needs to have the right credentials to be able to access the Test Automation REST API.

![global settings](https://github.com/Dynatrace/Dynatrace-Jenkins-Plugin/blob/master/img/conf/global_settings.png)

The advanced section enables you to set a delay before retrieving the test results from the server. Change this settings if you are not getting all the test results in Jenkins.

<a name="build"/>
### Build configuration

In the build configuration (build name / configure), first enable *Use Dynatrace AppMon to monitor tests* in the **Build Environment** and fill the required fields.

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

<a name="examples"/>
## Examples

<a name="maven"/>
### Maven

<a name="maven1"/>
#### Option 1

[Option 1: Test Run Registration from Jenkins](#option1)

in this case, the Test Run Id will be passed from Jenkins to your Ant script as an environment variable. You just need to make sure that the **Java agent is injected and that the test run id ${dtTestrunID} is passed to the agent**.

**Example with Surefire**:

```xml
<plugin>
	<groupId>org.apache.maven.plugins</groupId>
	<artifactId>maven-surefire-plugin</artifactId>
	<version>2.19.1</version>
	<configuration>
		<includes>
			<include>**/Unit*.java</include>
		</includes>
		<!-- dtTestrunID is passed from Jenkins as environment variable --> 
		<!-- dt_agent_path, dt_agent_name and dt_server needs to be configured in your script or passed as environment variable -->
		<argLine>-agentpath:"${dt_agent_path}"=name=${dt_agent_name},server=${dt_server},optionTestRunIdJava=${dtTestrunID}</argLine>
	</configuration>
</plugin>
```

**Example with Failsafe:**

```xml
<plugin>
	<groupId>org.apache.maven.plugins</groupId>
	<artifactId>maven-failsafe-plugin</artifactId>
	<version>2.19.1</version>
	<configuration>
		<includes>
			<include>**/Integration*.java</include>
		</includes>
		
		<!-- dtTestrunID is passed from Jenkins as environment variable --> 
		<!-- dt_agent_path, dt_agent_name and dt_server needs to be configured in your script or passed as environment variable -->
		<argLine>-agentpath:"${dt_agent_path}"=name=${dt_agent_name},server=${dt_server},optionTestRunIdJava=${dtTestrunID}</argLine>
	</configuration>
	<executions>
		<execution>
			<goals>
			 	<goal>integration-test</goal>
				<goal>verify</goal>
			</goals>
		</execution>
	</executions>
</plugin>
```

<a name="maven2"/>
#### Option 2

In this case, the test run registration is done directly from Maven - enabling you to re-use Maven version information and meta-data.

Download and install the Dynatrace Maven Plug-in as described here: https://community.dynatrace.com/community/display/DL/Dynatrace+Test+Automation+and+Maven

**Add the Dynatrace Automation Plug-in in your dependency list**:
```xml
<dependencies>
	<dependency>
		<groupId>dynaTrace</groupId>
		<artifactId>dtAutomation</artifactId>
		<version>${dynaTrace.version}</version>
	</dependency>
</dependencies>
```

**Example with Surefire**:

```xml
<plugin>
	<groupId>dynaTrace</groupId>
	<artifactId>dtAutomation</artifactId>
	<version>${dynaTrace.version}</version>
	<executions>
		<execution>
			<id>DT_StartTest_UnitTest</id>
			<configuration>
				<!-- the build id (passed from Jenkins) will be re-used by Dynatrace to retrieve the test results --> 
				<versionBuild>${BUILD_ID}</versionBuild>
				<profileName>junit-example-dt-maven</profileName>
				<category>unit</category>
			</configuration>
			<!-- start this test in the process-test-classes phase which is the one before the tests are executed -->
			<phase>process-test-classes</phase>
				<goals>
					<!-- call the startTest goal of the Dynatrace Maven plugin -->
					<goal>startTest</goal>
				</goals>
		</execution>
</plugin>
<plugin>
	<groupId>org.apache.maven.plugins</groupId>
	<artifactId>maven-surefire-plugin</artifactId>
	<version>2.19.1</version>
	<configuration>
		<includes>
			<include>**/Unit*.java</include>
		</includes>
		<!-- dtTestrunID is passed from the Dynatrace Maven Plug-in --> 
		<!-- dt_agent_path, dt_agent_name and dt_server needs to be configured in your script or passed as environment variable -->
		<argLine>-agentpath:"${dt_agent_path}"=name=${dt_agent_name},server=${dt_server},optionTestRunIdJava=${dtTestrunID}</argLine>
	</configuration>
</plugin>
```

**Example with Failsafe**:

```xml
<plugin>
	<groupId>dynaTrace</groupId>
	<artifactId>dtAutomation</artifactId>
	<version>${dynaTrace.version}</version>
	<executions>
		<execution>
		<id>DT_StartTest_IntegrationTest</id>
		<configuration>
			<!-- the build id (passed from Jenkins) will be re-used by Dynatrace to retrieve the test results --> 
			<versionBuild>${BUILD_ID}</versionBuild>
			<profileName>junit-example-dt-maven</profileName>
			<category>performance</category>
		</configuration>
		<phase>pre-integration-test</phase>
		<goals>
			<goal>startTest</goal>
		</goals>
		</execution>
	</executions>
</plugin>
<plugin>
	<groupId>org.apache.maven.plugins</groupId>
	<artifactId>maven-failsafe-plugin</artifactId>
	<version>2.19.1</version>
	<configuration>
		<includes>
			<include>**/Integration*.java</include>
		</includes>
		<!-- dtTestrunID is passed from the Dynatrace Maven Plug-in --> 
		<!-- dt_agent_path, dt_agent_name and dt_server needs to be configured in your script or passed as environment variable -->
		<argLine>-agentpath:"${dt_agent_path}"=name=${dt_agent_name},server=${dt_server},optionTestRunIdJava=${dtTestrunID}</argLine>
	</configuration>
	<executions>
		<execution>
			<goals>
				<goal>integration-test</goal>
				<goal>verify</goal>
			</goals>
		</execution>
	</executions>
</plugin>
```

<a name="ant"/>
### Ant

<a name="ant1"/>
#### Option 1

[Option 1: Test Run Registration from Jenkins](#option1)

in this case, the Test Run Id will be passed from Jenkins to your Ant script as an environment variable. You just need to make sure that the **Java agent is injected and that the test run id ${dtTestrunID} is passed to the agent**.

**Example:**

```xml
<target name="junit" depends="jar"> 
<junit printsummary="yes"> 
<!-- dtTestrunID is passed from Jenkins as environment variable --> 
<!-- dt_agent_path, dt_agent_name and dt_server needs to be configured in your script or passed as environment variable -->
<jvmarg value="-agentpath:${dt_agent_path}=name=${dt_agent_name},server=${dt_server},loglevel=warning,optionTestRunIdJava=${dtTestrunID}" /> 
<classpath> 
	<path refid="classpath"/> 
	<path refid="application"/> 
</classpath> 
<batchtest fork="yes"> 
	<fileset dir="${src.dir}" includes="*Test.java"/> 
</batchtest> 
</junit> 
</target> 
```


<a name="ant2"/>
#### Option 2

In this case, the test run registration is done directly from Maven - enabling you to re-use Maven version information and meta-data.

Download and install the Dynatrace Ant Library as described here: https://community.dynatrace.com/community/display/DL/Dynatrace+Test+Automation+and+Ant 

**Import the Dynatrace Ant Task definitions**:

```xml
<property name="dtBaseDir" value="${libs}/dynaTrace" />
<import file="${dtBaseDir}/dtTaskDefs.xml"/>
```

**Call DtStartTest before running your Tests**:

```xml
<target name="test" depends="compile,test-compile" description="Run tests">
<mkdir dir="${test.result.dir}"/>
<mkdir dir="${test.report.dir}"/>

<!-- the build id (passed from Jenkins) will be re-used by Dynatrace to retrieve the test results --> 
<DtStartTest
	versionMajor="${version.major}"
	versionMinor="${version.minor}"
	versionRevision="${version.revision}"
	versionMilestone="test-parallel"
	versionBuild="${BUILD_ID}"
	profilename="${dynatrace.profile.name}"
	username="${dynatrace.server.user}"
	password="${dynatrace.server.pass}"
	serverurl="${dynatrace.server.url}"
	category="${dynatrace.test.category}">
</DtStartTest>
```
**Finally run your Unit Tests**:

```xml
<junit printsummary="yes"> 
<!-- dtTestrunID is passed from the Dynatrace Ant task --> 
<!-- dt_agent_path, dt_agent_name and dt_server needs to be configured in your script or passed as environment variable -->
<jvmarg value="-agentpath:${dt_agent_path}=name=${dt_agent_name},server=${dt_server},loglevel=warning,optionTestRunIdJava=${dtTestrunID}" /> 
<classpath> 
	<path refid="classpath"/> 
	<path refid="application"/> 
</classpath> 
<batchtest fork="yes"> 
	<fileset dir="${src.dir}" includes="*Test.java"/> 
</batchtest> 
</junit> 
```

<a name="nant"/>
### NAnt

<a name="nant1"/>
#### Option 1

[Option 1: Test Run Registration from Jenkins](#option1)

For the .net agent, the test run id must be passed through the environment variable DT_TESTRUN_ID (see also <a href="https://community.dynatrace.com/community/display/DOCDT63/.NET+Agent+Configuration" target="_blank">.NET Agent Configuration</a>). Using <a href="https://wiki.jenkins-ci.org/display/JENKINS/EnvInject+Plugin" target="_blank">EnvInject Plugin</a> in Jenkins you can inject the DT_TESTRUN_ID variable between the Register Test Run and the Execute Build steps.

<img src="/img/conf/NAnt.png" />



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

<a name="webinar"/>
### Recorded Webinar

- [Online Perf Clinic - Eclipse and Jenkins Integration](https://youtu.be/p4Vh6BWlPjg)
- [Online Perf Clinic - Metrics-Driven Continuous Delivery with Dynatrace Test Automation](https://youtu.be/TXPSDpy7unw)

<a name="blogs"/>
### Blogs

- [Continuous Performance Validation in Continuous Integration Environments](http://apmblog.dynatrace.com/2013/11/27/continuous-performance-validation-in-continuous-integration-environments/)
- [Software Quality Metrics for your Continuous Delivery Pipeline – Part I](http://apmblog.dynatrace.com/2014/03/13/software-quality-metrics-for-your-continuous-delivery-pipeline-part-i/)
- [Software Quality Metrics for your Continuous Delivery Pipeline – Part II: Database](http://apmblog.dynatrace.com/2014/04/23/database-access-quality-metrics-for-your-continuous-delivery-pipeline/)
- [Software Quality Metrics for your Continuous Delivery Pipeline – Part III – Logging](http://apmblog.dynatrace.com/2014/06/17/software-quality-metrics-for-your-continuous-delivery-pipeline-part-iii-logging/)
- [Automated Performance Analysis for Web API Tests](http://apmblog.dynatrace.com/2014/12/23/automated-performance-analysis-web-api-tests/)


