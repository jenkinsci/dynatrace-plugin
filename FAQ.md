# FAQ / Troubleshooting Guide


## Problems? Questions? Suggestions?

Post any problems, questions or suggestions to the Dynatrace Community's [Application Monitoring & UEM Forum](https://answers.dynatrace.com/spaces/146/index.html).
 

## FAQ

##### Dynatrace AppMon version compatibility - which version works with the plugin?
> 6.1 and newer

##### Jenkins compatibility - which version works with the plugin?
> works with 1.x and 2.x
> However since a security feature was introduced in 2.5, an additional configuration step is required

## Known Issues

<a name="jenkins2.5" />
##### Jenkins 2.5+ build step execution failed with java.lang.NullPointerException

Due to a new security feature introduced in Jenkins 2.5 (SECURITY-170, see https://wiki.jenkins-ci.org/display/SECURITY/Jenkins+Security+Advisory+2016-05-11) custom build parameters cannot be passed to build scripts as environment variables. 

Solution 1 (not recommended by Jenkins) - enable the use of custom build parameters:

```
java -Dhudson.model.ParametersAction.keepUndefinedParameters=true -jar jenkins.war
```

Solution 2 - enable only the use of the Dynatrace parameters:

```
java -Dhudson.model.ParametersAction.safeParameters=dtProfile,dtVersionMajor,dtVersionMinor,dtVersionRevision,dtVersionBuild,dtVersionMilestone,dtMarker,dtTestrunID,dtStoredSessionName,dtServerUrl,dtUsername,dtPassword -jar jenkins.war
```

##### SSLHandshakeException when trying to connect through HTTPS

> Dynatrace AppMon Server uses a self-signed certificate per default. Since this certificate doesn't match the URL you are using to access the Server, Jenkins returns an error when trying to connect.

> To solve it:
* either deploy a valid SSL certificate on Dynatrace AppMon server 
* or use the Jenkins plug-in  https://wiki.jenkins-ci.org/display/JENKINS/Skip+Certificate+Check+plugin to skip the certificate check
* or (not recommended) connect through HTTP. In that case, you need to uncheck the setting "Accept authentication data only with HTTPS" under "Dynatrace Server / Services / Management"
