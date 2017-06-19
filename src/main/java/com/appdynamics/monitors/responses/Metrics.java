package com.appdynamics.monitors.responses;

import java.util.Map;

/***
 * Copied from https://github.com/dbaggott/newrelic-dropwizard/blob/master/src/main/java/io/dnbg/newrelic/dropwizard/responses/Metrics.java
 *
 */
public class Metrics {
    public String version;
    public Map<String, Gauge> gauges;
    public Map<String, Counter> counters;
    public Map<String, Histogram> histograms;
    public Map<String, Meter> meters;
    public Map<String, Timer> timers;

    public Counter getCounter(String name) {
        return counters.get(name);
    }

    public Gauge getGauge(String name) {
        return gauges.get(name);
    }

    public Histogram getHistogram(String name) {
        return histograms.get(name);
    }

    public Meter getMeter(String name) {
        return meters.get(name);
    }

    public Timer getTimer(String name) {
        return timers.get(name);
    }

    public static class Gauge {
        public Object value;
    }

    public static class Histogram {
        public long count;
        public double min;
        public double max;
        public double mean;
        public double stddev;
        public double p50;
        public double p75;
        public double p95;
        public double p98;
        public double p99;
        public double p999;
    }

    public static class Meter {
        public long count;
        public double m1_rate;
        public double m5_rate;
        public double m15_rate;
        public double mean_rate;
        public String units;
    }

    public static class Timer {
        public long count;
        public double min;
        public double max;
        public double mean;
        public double stddev;
        public double p50;
        public double p75;
        public double p95;
        public double p98;
        public double p99;
        public double p999;
        public String duration_units;
        public double m1_rate;
        public double m5_rate;
        public double m15_rate;
        public double mean_rate;
        public String rate_units;
    }

    public static class Counter {
        public long count;
    }
}
