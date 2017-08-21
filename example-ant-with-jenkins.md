# Using Test Run registered in Jenkins

Test Run Id will be passed from Jenkins to your Ant script as an environment variable. 
You just need to make sure that the Java agent is injected and that the Test Run id `${dtTestrunID}` is passed to the agent.

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
