# Registering Test Run using Dynatrace Maven plugin

In this case, the Test Run registration is done directly from Maven - enabling you to re-use version information and meta-data defined in the `pom.xml` file.

Download and install the Dynatrace Maven Plug-in as described [here](https://github.com/Dynatrace/Dynatrace-Maven-Plugin).

Add the Dynatrace Automation Plug-in in your dependency list:
```xml
<dependencies>
	<dependency>
		<groupId>dynaTrace</groupId>
		<artifactId>dtAutomation</artifactId>
		<version>${dynaTrace.version}</version>
	</dependency>
</dependencies>
```

**Surefire**:

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
	</executions>
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

**Failsafe**:

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
