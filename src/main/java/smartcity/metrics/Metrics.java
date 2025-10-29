package smartcity.metrics;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Comprehensive metrics collection and performance monitoring system.
 * Tracks operation counts, execution times, and algorithm performance metrics.
 *
 * @author Your Name
 * @version 1.0
 * @since 2024
 */
public class Metrics {
    private final Map<String, AtomicLong> counters;
    private final Map<String, Long> timers;
    private final Map<String, List<Long>> timeSeries;
    private final Map<String, Object> customMetrics;
    private final long startTime;

    // Common metric names
    public static final String DFS_VISITS = "dfsVisits";
    public static final String EDGES_TRAVERSED = "edgesTraversed";
    public static final String RELAXATIONS = "relaxations";
    public static final String TOPOLOGICAL_OPERATIONS = "topologicalOperations";
    public static final String KAHN_POPS = "kahnPops";
    public static final String KAHN_PUSHES = "kahnPushes";
    public static final String SCC_COMPONENTS = "sccComponents";
    public static final String CYCLE_DETECTIONS = "cycleDetections";
    public static final String PATH_RECONSTRUCTIONS = "pathReconstructions";

    /**
     * Creates a new Metrics instance.
     */
    public Metrics() {
        this.counters = new ConcurrentHashMap<>();
        this.timers = new ConcurrentHashMap<>();
        this.timeSeries = new ConcurrentHashMap<>();
        this.customMetrics = new ConcurrentHashMap<>();
        this.startTime = System.nanoTime();

        // Initialize common counters
        initializeCommonCounters();
    }

    private void initializeCommonCounters() {
        setCounter(DFS_VISITS, 0);
        setCounter(EDGES_TRAVERSED, 0);
        setCounter(RELAXATIONS, 0);
        setCounter(TOPOLOGICAL_OPERATIONS, 0);
        setCounter(KAHN_POPS, 0);
        setCounter(KAHN_PUSHES, 0);
        setCounter(SCC_COMPONENTS, 0);
        setCounter(CYCLE_DETECTIONS, 0);
        setCounter(PATH_RECONSTRUCTIONS, 0);
    }

    // ==================== COUNTER OPERATIONS ====================

    /**
     * Increments a counter by 1.
     *
     * @param counterName the name of the counter
     */
    public void incrementCounter(String counterName) {
        incrementCounter(counterName, 1);
    }

    /**
     * Increments a counter by the specified amount.
     *
     * @param counterName the name of the counter
     * @param amount the amount to increment
     */
    public void incrementCounter(String counterName, long amount) {
        counters.computeIfAbsent(counterName, k -> new AtomicLong(0))
                .addAndGet(amount);
    }

    /**
     * Sets a counter to a specific value.
     *
     * @param counterName the name of the counter
     * @param value the value to set
     */
    public void setCounter(String counterName, long value) {
        counters.put(counterName, new AtomicLong(value));
    }

    /**
     * Gets the current value of a counter.
     *
     * @param counterName the name of the counter
     * @return the counter value
     */
    public long getCounter(String counterName) {
        AtomicLong counter = counters.get(counterName);
        return counter != null ? counter.get() : 0;
    }

    /**
     * Resets a counter to 0.
     *
     * @param counterName the name of the counter
     */
    public void resetCounter(String counterName) {
        counters.put(counterName, new AtomicLong(0));
    }

    // ==================== TIMER OPERATIONS ====================

    /**
     * Starts a timer with the specified name.
     *
     * @param timerName the name of the timer
     */
    public void startTimer(String timerName) {
        timers.put(timerName + "_start", System.nanoTime());
    }

    /**
     * Stops a timer and returns the elapsed time in nanoseconds.
     *
     * @param timerName the name of the timer
     * @return elapsed time in nanoseconds
     */
    public long stopTimer(String timerName) {
        long endTime = System.nanoTime();
        Long startTime = timers.get(timerName + "_start");

        if (startTime != null) {
            long elapsed = endTime - startTime;
            timers.put(timerName, elapsed);
            addTimeSeriesData(timerName, elapsed);
            return elapsed;
        }

        return 0;
    }

    /**
     * Gets the elapsed time for a timer in nanoseconds.
     *
     * @param timerName the name of the timer
     * @return elapsed time in nanoseconds, or 0 if timer not found
     */
    public long getElapsedTime(String timerName) {
        Long elapsed = timers.get(timerName);
        return elapsed != null ? elapsed : 0;
    }

    /**
     * Gets the elapsed time for a timer in milliseconds.
     *
     * @param timerName the name of the timer
     * @return elapsed time in milliseconds
     */
    public double getElapsedTimeMillis(String timerName) {
        return getElapsedTime(timerName) / 1_000_000.0;
    }

    /**
     * Measures the execution time of a Runnable.
     *
     * @param timerName the name for the timer
     * @param operation the operation to measure
     * @return elapsed time in nanoseconds
     */
    public long measureExecution(String timerName, Runnable operation) {
        startTimer(timerName);
        operation.run();
        return stopTimer(timerName);
    }

    // ==================== TIME SERIES DATA ====================

    /**
     * Adds time series data point.
     *
     * @param seriesName the name of the time series
     * @param value the data point value
     */
    public void addTimeSeriesData(String seriesName, long value) {
        timeSeries.computeIfAbsent(seriesName, k -> new ArrayList<>())
                .add(value);
    }

    /**
     * Gets time series data.
     *
     * @param seriesName the name of the time series
     * @return list of data points, or empty list if not found
     */
    public List<Long> getTimeSeriesData(String seriesName) {
        return timeSeries.getOrDefault(seriesName, new ArrayList<>());
    }

    /**
     * Gets statistics for a time series.
     *
     * @param seriesName the name of the time series
     * @return statistics map, or empty map if no data
     */
    public Map<String, Object> getTimeSeriesStats(String seriesName) {
        List<Long> data = getTimeSeriesData(seriesName);
        if (data.isEmpty()) {
            return new HashMap<>();
        }

        Map<String, Object> stats = new HashMap<>();
        stats.put("count", data.size());
        stats.put("min", Collections.min(data));
        stats.put("max", Collections.max(data));
        stats.put("average", data.stream().mapToLong(Long::longValue).average().orElse(0));
        stats.put("total", data.stream().mapToLong(Long::longValue).sum());

        return stats;
    }

    // ==================== CUSTOM METRICS ====================

    /**
     * Sets a custom metric value.
     *
     * @param metricName the name of the metric
     * @param value the metric value
     */
    public void setCustomMetric(String metricName, Object value) {
        customMetrics.put(metricName, value);
    }

    /**
     * Gets a custom metric value.
     *
     * @param metricName the name of the metric
     * @return the metric value, or null if not found
     */
    public Object getCustomMetric(String metricName) {
        return customMetrics.get(metricName);
    }

    /**
     * Gets a custom metric with type safety.
     *
     * @param metricName the name of the metric
     * @param type the expected class type
     * @return the metric value, or null if not found or wrong type
     */
    @SuppressWarnings("unchecked")
    public <T> T getCustomMetric(String metricName, Class<T> type) {
        Object value = customMetrics.get(metricName);
        return type.isInstance(value) ? (T) value : null;
    }

    // ==================== ALGORITHM-SPECIFIC METRICS ====================

    /**
     * Records SCC algorithm metrics.
     *
     * @param dfsVisits number of DFS visits
     * @param edgesTraversed number of edges traversed
     * @param componentCount number of SCC components found
     * @param executionTime execution time in nanoseconds
     */
    public void recordSCCMetrics(long dfsVisits, long edgesTraversed,
                                 long componentCount, long executionTime) {
        setCounter(DFS_VISITS, dfsVisits);
        setCounter(EDGES_TRAVERSED, edgesTraversed);
        setCounter(SCC_COMPONENTS, componentCount);
        timers.put("sccExecution", executionTime);
    }

    /**
     * Records topological sort metrics.
     *
     * @param operations number of topological operations
     * @param pops number of queue/stack pops
     * @param pushes number of queue/stack pushes
     * @param executionTime execution time in nanoseconds
     */
    public void recordTopoMetrics(long operations, long pops, long pushes,
                                  long executionTime) {
        setCounter(TOPOLOGICAL_OPERATIONS, operations);
        setCounter(KAHN_POPS, pops);
        setCounter(KAHN_PUSHES, pushes);
        timers.put("topoExecution", executionTime);
    }

    /**
     * Records DAG shortest path metrics.
     *
     * @param relaxations number of edge relaxations
     * @param topologicalOps number of topological operations
     * @param pathReconstructions number of path reconstructions
     * @param executionTime execution time in nanoseconds
     */
    public void recordDAGSPMetrics(long relaxations, long topologicalOps,
                                   long pathReconstructions, long executionTime) {
        setCounter(RELAXATIONS, relaxations);
        setCounter(TOPOLOGICAL_OPERATIONS, topologicalOps);
        setCounter(PATH_RECONSTRUCTIONS, pathReconstructions);
        timers.put("dagspExecution", executionTime);
    }

    // ==================== METRICS ANALYSIS ====================

    /**
     * Gets all counters as a map.
     *
     * @return map of counter names to values
     */
    public Map<String, Long> getAllCounters() {
        Map<String, Long> result = new HashMap<>();
        counters.forEach((name, atomic) -> result.put(name, atomic.get()));
        return result;
    }

    /**
     * Gets all timers as a map (in milliseconds).
     *
     * @return map of timer names to values in milliseconds
     */
    public Map<String, Double> getAllTimersMillis() {
        Map<String, Double> result = new HashMap<>();
        timers.forEach((name, nanos) -> {
            if (!name.endsWith("_start")) {
                result.put(name, nanos / 1_000_000.0);
            }
        });
        return result;
    }

    /**
     * Gets comprehensive metrics summary.
     *
     * @return map containing all metrics
     */
    public Map<String, Object> getMetricsSummary() {
        Map<String, Object> summary = new HashMap<>();

        // Add counters
        summary.put("counters", getAllCounters());

        // Add timers (in milliseconds)
        summary.put("timers", getAllTimersMillis());

        // Add custom metrics
        summary.put("customMetrics", new HashMap<>(customMetrics));

        // Add overall statistics
        summary.put("totalUptimeMs", (System.nanoTime() - startTime) / 1_000_000.0);
        summary.put("metricCollectionStart", new Date(startTime / 1_000_000));

        return summary;
    }

    /**
     * Resets all metrics to initial state.
     */
    public void resetAll() {
        counters.clear();
        timers.clear();
        timeSeries.clear();
        customMetrics.clear();
        initializeCommonCounters();
    }

    /**
     * Resets specific categories of metrics.
     *
     * @param categories the categories to reset
     */
    public void resetCategories(String... categories) {
        for (String category : categories) {
            switch (category.toLowerCase()) {
                case "counters":
                    counters.clear();
                    initializeCommonCounters();
                    break;
                case "timers":
                    timers.clear();
                    break;
                case "timeseries":
                    timeSeries.clear();
                    break;
                case "custom":
                    customMetrics.clear();
                    break;
            }
        }
    }

    // ==================== PERFORMANCE ANALYSIS ====================

    /**
     * Calculates operations per second for a counter.
     *
     * @param counterName the counter name
     * @param timerName the timer name
     * @return operations per second, or 0 if data not available
     */
    public double calculateOpsPerSecond(String counterName, String timerName) {
        long count = getCounter(counterName);
        double seconds = getElapsedTime(timerName) / 1_000_000_000.0;

        return seconds > 0 ? count / seconds : 0;
    }

    /**
     * Calculates memory usage statistics.
     *
     * @return memory statistics map
     */
    public Map<String, Object> getMemoryStats() {
        Runtime runtime = Runtime.getRuntime();
        Map<String, Object> memoryStats = new HashMap<>();

        memoryStats.put("usedMemoryMB", (runtime.totalMemory() - runtime.freeMemory()) / (1024.0 * 1024.0));
        memoryStats.put("freeMemoryMB", runtime.freeMemory() / (1024.0 * 1024.0));
        memoryStats.put("totalMemoryMB", runtime.totalMemory() / (1024.0 * 1024.0));
        memoryStats.put("maxMemoryMB", runtime.maxMemory() / (1024.0 * 1024.0));

        return memoryStats;
    }

    /**
     * Prints a comprehensive metrics report.
     */
    public void printMetricsReport() {
        System.out.println("=== METRICS REPORT ===");
        System.out.printf("Collection uptime: %.2f seconds%n",
                (System.nanoTime() - startTime) / 1_000_000_000.0);

        System.out.println("\nCOUNTERS:");
        getAllCounters().forEach((name, value) ->
                System.out.printf("  %s: %,d%n", name, value));

        System.out.println("\nTIMERS (ms):");
        getAllTimersMillis().forEach((name, value) ->
                System.out.printf("  %s: %.3f ms%n", name, value));

        System.out.println("\nPERFORMANCE:");
        Map<String, Object> memoryStats = getMemoryStats();
        memoryStats.forEach((name, value) ->
                System.out.printf("  %s: %s%n", name, value));
    }

    // ==================== BUILDER PATTERN ====================

    /**
     * Builder for Metrics with predefined configurations.
     */
    public static class Builder {
        private boolean enableMemoryTracking = false;
        private boolean enableTimeSeries = true;
        private Set<String> predefinedCounters = new HashSet<>();

        public Builder() {
            // Add common predefined counters
            predefinedCounters.addAll(Arrays.asList(
                    DFS_VISITS, EDGES_TRAVERSED, RELAXATIONS,
                    TOPOLOGICAL_OPERATIONS, KAHN_POPS, KAHN_PUSHES
            ));
        }

        public Builder enableMemoryTracking(boolean enable) {
            this.enableMemoryTracking = enable;
            return this;
        }

        public Builder enableTimeSeries(boolean enable) {
            this.enableTimeSeries = enable;
            return this;
        }

        public Builder addPredefinedCounter(String counterName) {
            this.predefinedCounters.add(counterName);
            return this;
        }

        public Metrics build() {
            Metrics metrics = new Metrics();

            if (enableMemoryTracking) {
                // Schedule periodic memory tracking
                Timer memoryTimer = new Timer(true);
                memoryTimer.scheduleAtFixedRate(new TimerTask() {
                    @Override
                    public void run() {
                        metrics.setCustomMetric("memorySnapshot", metrics.getMemoryStats());
                    }
                }, 0, 5000); // Every 5 seconds
            }

            return metrics;
        }
    }

    // ==================== SINGLETON PATTERN (Optional) ====================

    private static volatile Metrics globalInstance;

    /**
     * Gets the global Metrics instance (singleton pattern).
     *
     * @return global Metrics instance
     */
    public static Metrics getGlobalInstance() {
        if (globalInstance == null) {
            synchronized (Metrics.class) {
                if (globalInstance == null) {
                    globalInstance = new Metrics();
                }
            }
        }
        return globalInstance;
    }

    /**
     * Resets the global Metrics instance.
     */
    public static void resetGlobalInstance() {
        synchronized (Metrics.class) {
            globalInstance = null;
        }
    }
}