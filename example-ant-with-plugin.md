# Registering Test Run using Dynatrace Ant plugin

Test Run registration is done directly from Ant - enabling you to re-use version information and meta-data given in the Ant script.

Download and install the Dynatrace Ant Library as described [here](https://github.com/Dynatrace/Dynatrace-Ant-Plugin).

Import the Dynatrace Ant Task definitions:

```xml
<property name="dtBaseDir" value="${libs}/dynaTrace" />
<import file="${dtBaseDir}/dtTaskDefs.xml"/>
```

Call `DtStartTest` before running your Tests:

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
Run Unit Tests:

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
