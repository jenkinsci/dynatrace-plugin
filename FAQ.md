# FAQ / Troubleshooting Guide


## Problems? Questions? Suggestions?

Post any problems, questions or suggestions to the Dynatrace Community's [Application Monitoring & UEM Forum](https://answers.dynatrace.com/spaces/146/index.html).
 

## FAQ

##### Dynatrace AppMon version compatibility - which version works with the plugin?
> 6.1 and newer

##### What do I need to change in my Ant Script?
> [Option 1](README.md#option1): Test Run Registration from Jenkins

> in this case, the Test Run Id will be passed from Jenkins to your Ant script as an environment variable. You just need to make sure that the **Java agent is injected and that the test run id ${dtTestrunID} is passed to the agent**.
> Example:

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


> [Option 2](README.md#option2): Test Run Registration from Ant

##### What do I need to change in my Maven Script?

> [Option 1](README.md#option1): Test Run Registration from Jenkins

> in this case, the Test Run Id will be passed from Jenkins to your Ant script as an environment variable. You just need to make sure that the **Java agent is injected and that the test run id ${dtTestrunID} is passed to the agent**.
> Example with Surefire:

```xml
<plugin>
	<groupId>org.apache.maven.plugins</groupId>
	<artifactId>maven-surefire-plugin</artifactId>
	<version>2.19.1</version>
	<configuration>
	  <includes>
		<include>**/Unit*.java</include>
	  </includes>
	  <argLine>-agentpath:"${dt_agent_path}"=name=${dt_agent_name},server=${dt_server},optionTestRunIdJava=${dtTestrunID}</argLine>
	</configuration>
</plugin>
```

> Example with Failsafe:

```xml
<plugin>
<groupId>org.apache.maven.plugins</groupId>
<artifactId>maven-failsafe-plugin</artifactId>
<version>2.19.1</version>
<configuration>
  <includes>
	<include>**/Integration*.java</include>
  </includes>
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

> [Option 2](README.md#option2): Test Run Registration from Maven


##### Do I need to use an additional plug-in for Ant, Maven, MS Build to register my tests?

> Not anymore. The registration of the test run is done through the Jenkins Plugin. The test run id ${dtTestrunID} is passed as an environment variable and can be used in your build scripts.

##### Can I use variables to set the version information?

> Build number and jenkins jobs are passed automatically to Dynatrace. Version (major, minor, revision, milestone) currently needs to be set manually (fields are not mandatory). We are looking into allowing the use of variables to set those fields.

## Known Issues

##### SSLHandshakeException when trying to connect through HTTPS

> Dynatrace AppMon Server uses a self-signed certificate per default. Since this certificate doesn't match the URL you are using to access the Server, Jenkins returns an error when trying to connect.

> To solve it:
* either deploy a valid SSL certificate on Dynatrace AppMon server 
* or use the Jenkins plug-in  https://wiki.jenkins-ci.org/display/JENKINS/Skip+Certificate+Check+plugin to skip the certificate check
* or (not recommended) connect through HTTP. In that case, you need to uncheck the setting "Accept authentication data only with HTTPS" under "Dynatrace Server / Services / Management"
