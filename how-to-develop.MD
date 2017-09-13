Dynatrace Jenkins Plugin deployment
-----------------------------------
1. Start an ssh-agent and import your private key - so the release plugin can access the GitHub repository.
2. Configure the username and password for jenkins.ci-org in ~/.m2/settings.xml as described below.
```xml
<settings xmlns="http://maven.apache.org/SETTINGS/1.0.0"
  xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
  xsi:schemaLocation="http://maven.apache.org/SETTINGS/1.0.0
                      http://maven.apache.org/xsd/settings-1.0.0.xsd">
    <servers>
        <server>
          <id>maven.jenkins-ci.org</id>
          <username>...</username>
          <password>...</password>
        </server>
      </servers>
</settings>
```
3. Run following maven command:
```
    mvn release:prepare release:perform
```

It will create release on Github and jenkins.ci-org. Release should be available within 4 hours.

More info: <a href="https://wiki.jenkins.io/display/JENKINS/Hosting+Plugins">Hosting plugins</a>