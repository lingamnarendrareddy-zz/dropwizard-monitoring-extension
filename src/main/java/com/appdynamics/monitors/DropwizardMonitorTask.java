package com.appdynamics.monitors;

import com.appdynamics.extensions.conf.MonitorConfiguration;
import com.appdynamics.extensions.http.HttpClientUtils;
import com.appdynamics.monitors.responses.Metrics;
import com.google.common.base.Strings;
import com.google.gson.Gson;
import org.apache.commons.lang.StringUtils;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.log4j.Logger;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * This task will be executed in a threadpool.
 * <p>
 * This is a simple impl where we invoke a url and get the content length.
 * Created by narendra.reddy on 04/20/17.
 */
public class DropwizardMonitorTask implements Runnable {

    private Map server;
    private MonitorConfiguration configuration;
    public static final Logger logger = Logger.getLogger(DropwizardMonitorTask.class);
    private String metricPrefix;
    private static final String METERS = "meters";
    private static final String TIMERS = "timers";
    private static final String GAUGES = "gauges";
    private static final String DISPLAY_NAME = "displayName";
    private static final String PATH = "path";
    private static final String TYPE = "type";
    private static final String MULTIPLIER = "multiplier";
    private static final String PATTERN = "pattern";

    public DropwizardMonitorTask(Map server, MonitorConfiguration configuration) {
        this.server = server;
        this.configuration = configuration;
        this.metricPrefix = configuration.getMetricPrefix() + "|";
    }

    public void run() {
        CloseableHttpClient httpClient = configuration.getHttpClient();
        String url = (String) server.get("uri");
        String response = HttpClientUtils.getResponseAsStr(httpClient, url);

        logger.debug("response receive:: " + response);
        Metrics metricsObj = new Gson().fromJson(response,Metrics.class);

        List<Map<String, List<Map<String, String>>>> metrics = (List) configuration.getConfigYml().get("metrics");

        for (Map<String, List<Map<String, String>>> metricMap : metrics) {
            reportMetersMetrics(metricMap.get(METERS), metricsObj);
            reportTimersMetrics(metricMap.get(TIMERS), metricsObj);
            reportGuagesMetrics(metricMap.get(GAUGES), metricsObj);
        }
    }

    /***
     * Reports meter metrics
     *  Using metricPath and pattern
     * @param metricMap
     * @param metrics
     */
    private void reportMetersMetrics(List<Map<String, String>> metricMap, Metrics metrics ) {
        if (metricMap == null) return;

        for (Map<String, String> metricPaths : metricMap) {

            String displayName = metricPaths.get(DISPLAY_NAME);
            String path = metricPaths.get(PATH);
            String type = metricPaths.get(TYPE);
            String multiplier = metricPaths.get(MULTIPLIER);
            String isPattern = metricPaths.get(PATTERN);

            if (StringUtils.isNotBlank(isPattern)) {
                Pattern pattern = Pattern.compile(path);
                for (Map.Entry<String, Metrics.Meter> entryobj : metrics.meters.entrySet()) {
                    String dropwizardMetricName = entryobj.getKey();
                    Matcher matcher;
                    if ((matcher = pattern.matcher(dropwizardMetricName)).matches()) {
                        String pool = matcher.group(1);
                        BigDecimal bigValue = BigDecimal.valueOf(metrics.getMeter(dropwizardMetricName).m1_rate);

                        if (bigValue instanceof Number) {
                            BigDecimal metricValueToReport = applyMultiplier(bigValue, multiplier);
                            printMetric(metricPrefix + displayName + "|" + pool, metricValueToReport, type);
                        } else {
                            logMetricNotANumber(displayName);
                        }
                    }
                }
            } else {
                if(null != metrics.getMeter(path)) {
                    BigDecimal bigValue = BigDecimal.valueOf(metrics.getMeter(path).m1_rate);

                    if (bigValue instanceof Number) {
                        BigDecimal metricValueToReport = applyMultiplier(bigValue, multiplier);
                        printMetric(metricPrefix + displayName, metricValueToReport, type);
                    } else {
                        logMetricNotANumber(displayName);
                    }
                }
            }
        }
    }

    /***
     * Reports timer metrics
     *  Using metricPath and pattern
     * @param metricMap
     * @param metrics
     */
    private void reportTimersMetrics(List<Map<String, String>> metricMap, Metrics metrics ) {
        if (metricMap == null) return;

        for (Map<String, String> metricPaths : metricMap) {

            String displayName = metricPaths.get(DISPLAY_NAME);
            String path = metricPaths.get(PATH);
            String type = metricPaths.get(TYPE);
            String multiplier = metricPaths.get(MULTIPLIER);
            String isPattern = metricPaths.get(PATTERN);

            if (StringUtils.isNotBlank(isPattern)) {
                Pattern pattern = Pattern.compile(path);
                for (Map.Entry<String, Metrics.Timer> entryobj : metrics.timers.entrySet()) {
                    String dropwizardMetricName = entryobj.getKey();
                    Matcher matcher;
                    if ((matcher = pattern.matcher(dropwizardMetricName)).matches()) {
                        String pool = matcher.group(1);
                        BigDecimal bigValue = BigDecimal.valueOf(metrics.getTimer(dropwizardMetricName).m1_rate);

                        if (bigValue instanceof Number) {
                            BigDecimal metricValueToReport = applyMultiplier(bigValue, multiplier);
                            printMetric(metricPrefix + displayName + "|" + pool, metricValueToReport, type);
                        } else {
                            logMetricNotANumber(displayName);
                        }
                    }
                }
            } else {
                if (null != metrics.getTimer(path)) {
                    BigDecimal bigValue = BigDecimal.valueOf(metrics.getTimer(path).m1_rate);

                    if (bigValue instanceof Number) {
                        BigDecimal metricValueToReport = applyMultiplier(bigValue, multiplier);
                        printMetric(metricPrefix + displayName, metricValueToReport, type);
                    } else {
                        logMetricNotANumber(displayName);
                    }
                }
            }
        }
    }

    /***
     * Reports gauges metrics
     *  Using metricPath and pattern
     * @param metricMap
     * @param metrics
     */
    private void reportGuagesMetrics(List<Map<String, String>> metricMap, Metrics metrics ) {
        if (metricMap == null) return;

        for (Map<String, String> metricPaths : metricMap) {

            String displayName = metricPaths.get(DISPLAY_NAME);
            String path = metricPaths.get(PATH);
            String type = metricPaths.get(TYPE);
            String multiplier = metricPaths.get(MULTIPLIER);
            String isPattern = metricPaths.get(PATTERN);


            if (StringUtils.isNotBlank(isPattern)) {
                Pattern pattern = Pattern.compile(path);
                for (Map.Entry<String, Metrics.Gauge> entryobj : metrics.gauges.entrySet()) {
                    String dropwizardMetricName = entryobj.getKey();
                    Matcher matcher;
                    if ((matcher = pattern.matcher(dropwizardMetricName)).matches()) {
                        String pool = matcher.group(1);
                        BigDecimal bigValue = new BigDecimal(metrics.getGauge(dropwizardMetricName).value.toString());

                        if (bigValue instanceof Number) {
                            BigDecimal metricValueToReport = applyMultiplier(bigValue, multiplier);
                            printMetric(metricPrefix + displayName+"|"+pool, metricValueToReport, type);
                        } else {
                            logMetricNotANumber(displayName);
                        }
                    }
                }
            } else {
                if (null != metrics.getGauge(path)) {

                    if (metrics.getGauge(path).value instanceof Number) {
                        BigDecimal bigValue = new BigDecimal(metrics.getGauge(path).value.toString());
                        BigDecimal metricValueToReport = applyMultiplier(bigValue, multiplier);
                        printMetric(metricPrefix + displayName, metricValueToReport, type);
                    } else {
                        logMetricNotANumber(displayName);
                    }
                }
            }
        }
    }

    /***
     * Reports the metrics to AppDynamics controller
     * @param metricPath
     * @param metricValue
     * @param metricType
     */
    private void printMetric(String metricPath, BigDecimal metricValue, String metricType) {

        if (metricValue != null) {
            configuration.getMetricWriter().printMetric(metricPath, metricValue, metricType);
        }
        if (logger.isDebugEnabled()) {
            logger.debug("Metric [" + metricType + "] metric = " + metricPath + " = " + metricValue);
        }
    }

    /***
     * Applies multiplier
     * @param metricValue
     * @param multiplier
     * @return BigInteger
     */
    private BigDecimal applyMultiplier(BigDecimal metricValue, String multiplier) {

        if (Strings.isNullOrEmpty(multiplier)) {
            return metricValue;
        }

        try {
            metricValue = metricValue.multiply(new BigDecimal(multiplier));
            return metricValue;
        } catch (NumberFormatException nfe) {
            logger.error(String.format("Cannot apply multiplier {} to value {}.", multiplier, metricValue), nfe);
        }
        throw new IllegalArgumentException("Cannot convert into BigInteger " + metricValue);
    }

    /***
     * Logs warning
     * @param name
     */
    private void logMetricNotANumber(String name) {
        logger.warn("Not reporting "+ name + " dropwizard value is not a number");
    }
}