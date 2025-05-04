package com.logparser.aggregator;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.FileWriter;
import java.io.IOException;
import java.util.*;

public class RequestLogAggregator {

    private static final RequestLogAggregator instance = new RequestLogAggregator();

    // Holds all response times and status code counts per API route
    private static class LogStats {
        List<Integer> responseTimes = new ArrayList<>();
        Map<String, Integer> statusCounts = new HashMap<>();
    }

    // Map from API route to its collected stats
    private final Map<String, LogStats> logsByUrl = new LinkedHashMap<>();

    private RequestLogAggregator() {}

    public static RequestLogAggregator getInstance() {
        return instance;
    }

    /**
     * Adds a single request log entry to the aggregator.
     * Groups response times and status codes by API route.
     */
    public void add(String url, int responseTime, int statusCode) {
        logsByUrl.computeIfAbsent(url, k -> new LogStats());
        LogStats stats = logsByUrl.get(url);
        stats.responseTimes.add(responseTime);

        //grouping logic for status codes like 2xx, 4x, 5xx
        String category = (statusCode / 100) + "XX";
        int count = stats.statusCounts.getOrDefault(category, 0);
        stats.statusCounts.put(category, count + 1);
    }

    /**
     * Calculates a percentile using linear interpolation.
     * Returns a double value even when percentile lies between two indexes.
     */
    private double getInterpolatedPercentile(List<Integer> sortedValues, double percentile) {
        
    	if (sortedValues.isEmpty()) return 0;
        int n = sortedValues.size();
        double rank = (percentile / 100.0) * (n - 1);
        int lowerIndex = (int) Math.floor(rank);
        int upperIndex = (int) Math.ceil(rank);

        if (lowerIndex == upperIndex) {
            return sortedValues.get(lowerIndex);
        }

        double lowerValue = sortedValues.get(lowerIndex);
        double upperValue = sortedValues.get(upperIndex);
        double weight = rank - lowerIndex;

        return lowerValue + weight * (upperValue - lowerValue);
    }
    
    /**
     * Writes aggregated request data to a JSON file.
     * For each URL, calculates min/max/percentiles and status code counts.
     */

    public void writeToJson(String filePath) {
        Map<String, Object> output = new LinkedHashMap<>();

        for (Map.Entry<String, LogStats> entry : logsByUrl.entrySet()) {
            String url = entry.getKey();
            LogStats stats = entry.getValue();
            List<Integer> times = stats.responseTimes;
            Collections.sort(times);

            Map<String, Object> result = new LinkedHashMap<>();

            Map<String, Double> responseTimeStats = new LinkedHashMap<>();
            responseTimeStats.put("min", (double) times.get(0));
            responseTimeStats.put("50_percentile", getInterpolatedPercentile(times, 50));
            responseTimeStats.put("90_percentile", getInterpolatedPercentile(times, 90));
            responseTimeStats.put("95_percentile", getInterpolatedPercentile(times, 95));
            responseTimeStats.put("99_percentile", getInterpolatedPercentile(times, 99));
            responseTimeStats.put("max", (double) times.get(times.size() - 1));

            result.put("response_times", responseTimeStats);
            result.put("status_codes", stats.statusCounts);

            output.put(url, result);
        }

        try (FileWriter writer = new FileWriter(filePath)) {
            new ObjectMapper().writerWithDefaultPrettyPrinter().writeValue(writer, output);
            System.out.println("[Aggregator] Request log metrics written to " + filePath);
        } catch (IOException e) {
            System.err.println("[Aggregator] Error writing request JSON: " + e.getMessage());
        }
    }

	public boolean isEmpty() {
		return logsByUrl.isEmpty();
	}
}
