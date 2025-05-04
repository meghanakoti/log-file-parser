package com.logparser.aggregator;

import java.io.FileWriter;
import java.io.IOException;
import java.util.*;
import com.fasterxml.jackson.databind.ObjectMapper;

public class APMAggregator {
	
    private static final APMAggregator instance = new APMAggregator();
    private final Map<String, List<Double>> metrics = new HashMap<>();

    private APMAggregator() {}

    public static APMAggregator getInstance() {
        return instance;
    }

    public void add(String metric, double value) {
        metrics.computeIfAbsent(metric, k -> new ArrayList<>()).add(value);
    }

    public Set<String> getParsedMetrics() {
        return metrics.keySet();
    }

    public void writeToJson(String filePath) {
        Map<String, Map<String, Double>> output = new LinkedHashMap<>();

        for (Map.Entry<String, List<Double>> entry : metrics.entrySet()) {
            List<Double> values = entry.getValue();
            Collections.sort(values);

            double min = values.get(0);
            double max = values.get(values.size() - 1);
            double avg = values.stream().mapToDouble(Double::doubleValue).average().orElse(0.0);
            double median = values.size() % 2 == 0 ?
                (values.get(values.size()/2 - 1) + values.get(values.size()/2)) / 2.0 :
                values.get(values.size()/2);

            Map<String, Double> stats = new LinkedHashMap<>();
            stats.put("minimum", min);
            stats.put("median", median);
            stats.put("average", avg);
            stats.put("max", max);

            output.put(entry.getKey(), stats);
        }

        try (FileWriter writer = new FileWriter(filePath)) {
            new ObjectMapper().writerWithDefaultPrettyPrinter().writeValue(writer, output);
        } catch (IOException e) {
            System.err.println("[Aggregator] Error writing APM JSON: " + e.getMessage());
        }
    }
}
