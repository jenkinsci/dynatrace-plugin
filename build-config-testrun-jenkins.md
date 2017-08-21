# Register Test Run using Dynatrace Jenkins plugin

<img src="/img/conf/process_test_run_registration_jenkins.png" />

Then, for each test category (Unit Test, Performance Test, Browser Test or Web API Test), you need to add a **build step** to register a Test Run to the Dynatrace AppMon server before running your tests.

![build step register testrun](/img/conf/build_step_register_test_run.png)

Test Run's id is available as environment variable which can be passed to the Dynatrace AppMon agent in the build script.

**Example:**
```xml
<jvmarg value="-agentpath:$/var/lib/dynatrace/agent/lib64/libdtagent.so=name=JavaAgent,
server=localhost:9998,loglevel=warning,optionTestRunIdJava=${dtTestrunID}" />
```

**Maven tip:**

You can pass java arguments into surefire and failsafe plugins directly from commandline, without any changes in your Maven build script needed.
Just adapt and add following arguments in `Build -> Invoke top-level Maven targets -> Goals`:
```batch
-DargLine="-agentpath:<path to dtagent.dll/dtagent.so>=name=<agent name>,server=<host[:port]>,optionTestRunIdJava=%dtTestrunID%"
```
