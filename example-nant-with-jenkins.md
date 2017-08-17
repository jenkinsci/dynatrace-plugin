# Using Test Run registered in Jenkins

For the .NET agent, the Test Run id must be passed through the environment variable `DT_TESTRUN_ID` (see also [.NET Agent Configuration](https://community.dynatrace.com/community/display/DOCDT63/.NET+Agent+Configuration)).

Using [EnvInject Plugin](https://wiki.jenkins-ci.org/display/JENKINS/EnvInject+Plugin) in Jenkins you can inject the `DT_TESTRUN_ID` variable between the `Register Test Run` and the `Execute Build` steps.

<img src="/img/conf/NAnt.png" />
