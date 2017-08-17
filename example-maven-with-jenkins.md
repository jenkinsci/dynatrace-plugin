# Using Test Run registered in Jenkins

Test Run id will be passed from Jenkins to your Maven build script (`pom.xml`) as an environment variable.
You just need to make sure that the Java agent is injected and that the Test Run id `${dtTestrunID}` is passed to the agent.

**Surefire**:

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

**Failsafe:**

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
