# Register Test Run using Maven, Ant or Gradle

<img src="/img/conf/process_test_run_registration_plugin.png" />

* do not add the `Register Test Run` step in Jenkins
* install & configure the Ant/Maven/Gradle/... plug-in in your build script
* register the Test Run using the Ant/Maven/Gradle/... plug-in and make sure to pass the Jenkins build id `${BUILD_ID}` in the meta data of the Test Run. The Jenkins Plug-in will use this build id to retrieve the results.
