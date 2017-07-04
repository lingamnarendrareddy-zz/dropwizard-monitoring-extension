package com.appdynamics.monitors;

import com.appdynamics.extensions.conf.MonitorConfiguration;
import com.appdynamics.extensions.util.MetricWriteHelper;
import com.appdynamics.extensions.util.MetricWriteHelperFactory;
import com.google.common.collect.Maps;
import com.singularity.ee.agent.systemagent.api.AManagedMonitor;
import com.singularity.ee.agent.systemagent.api.TaskExecutionContext;
import com.singularity.ee.agent.systemagent.api.TaskOutput;
import com.singularity.ee.agent.systemagent.api.exception.TaskExecutionException;
import org.apache.log4j.ConsoleAppender;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.log4j.PatternLayout;

import java.io.OutputStreamWriter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 *
 * Created by narendra.reddy on 04/20/17.
 */
public class DropwizardMonitor extends AManagedMonitor {
    public static final Logger logger = Logger.getLogger(DropwizardMonitor.class);

    private static final String METRIC_PREFIX = "Custom Metrics|Dropwizard|";
    private MonitorConfiguration configuration;

    /**
     * Prints the version of the Monitor
     */
    public DropwizardMonitor() {
        String version = getClass().getPackage().getImplementationTitle();
        String msg = String.format("Using Monitor Version [%s]", version);
        logger.info(msg);
        System.out.println(msg);
    }

    /**
     * Entry point to the Class.
     * 1) If the configuration is not initialized , then we initialize it first
     * 2) Executes the data fetch task.
     *
     * @param argsMap
     * @param taskExecutionContext
     * @return
     * @throws TaskExecutionException
     */
    public TaskOutput execute(Map<String, String> argsMap, TaskExecutionContext taskExecutionContext)
            throws TaskExecutionException {
        logger.debug("The task arguments are {} " + argsMap);
        if (configuration == null) {
            initialize(argsMap);
        }

        configuration.executeTask();
        return new TaskOutput("Whatever");
    }


    private void initialize(Map<String, String> argsMap) {
        // Get the path of config.yml from the arguments.
        final String configFilePath = argsMap.get("config-file");
        // Metric writer is needed for the workbench.
        MetricWriteHelper metricWriteHelper = MetricWriteHelperFactory.create(this);
        MonitorConfiguration conf = new MonitorConfiguration(METRIC_PREFIX, new TaskRunnable(), metricWriteHelper);
        // set the path. The framework will read/watch/load the config changes.
        conf.setConfigYml(configFilePath);
        this.configuration = conf;
    }

    private class TaskRunnable implements Runnable {

        public void run() {
            Map<String, ?> configYml = configuration.getConfigYml();
            if (configYml != null) {
                List<Map> servers = (List) configYml.get("servers");
                if (servers != null && !servers.isEmpty()) {
                    for (Map server : servers) {
                        // For each server entry create a task and execute it in a  thread pool.
                        DropwizardMonitorTask task = new DropwizardMonitorTask(server, configuration);
                        configuration.getExecutorService().execute(task);
                    }
                } else {
                    logger.error("There are no servers configured");
                }
            }

        }
    }

    /***
     *
     * @param args
     * @throws TaskExecutionException
     */
    public static void main(String[] args) throws TaskExecutionException {

        ConsoleAppender ca = new ConsoleAppender();
        ca.setWriter(new OutputStreamWriter(System.out));
        ca.setLayout(new PatternLayout("%-5p [%t]: %m%n"));
        ca.setThreshold(Level.TRACE);

        logger.getRootLogger().addAppender(ca);


        final DropwizardMonitor monitor = new DropwizardMonitor();

        final HashMap<String, String> taskArgs = Maps.newHashMap();
        taskArgs.put("config-file", "/Users/narendrareddy/GitHub/dropwizard-monitoring-extension/src/main/resources/conf/config.yml");

        monitor.execute(taskArgs, null);

    }
}
