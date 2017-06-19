# Extension Starter
 This is a template to get started with extensions.
 
## Usage
 Please take a look at the class `com.appdynamics.monitors.DropwizardMonitor` as the starting point.
 The `execute` method is the entry method.  This class does the initialization and triggers the execution
 of the task `com.appdynamics.monitors.DropwizardMonitorTask`.
 
 The task in this case is executed in a thread pool. The business logic is contained in the task class. 
 The task class fetches the data through an http call and then reports the metric.  
  
## Build
 Its maven, you know how to build!
 `mvn clean install`

## Workbench 
 1. After the build, copy the `StarterMonitor-1.0.0.zip` to `<Machine-Agent>/monitors`
 2. Run `java -jar extensions-starter-http.jar` to start the workbench server.
 3. Open http://localhost:9090 from a browser to see the workbench preview.
 
## Files
  1. `StarterMonitor.java`
  2. `StarterMonitorTask.java`
  3. `monitor.xml`
  4. `config.yml`
  5. `pom.xml`
  
