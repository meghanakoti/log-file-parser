package com.logparser.aggregator;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.FileWriter;
import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;

public class ApplicationLogAggregator {
	
    private static final ApplicationLogAggregator instance = new ApplicationLogAggregator();
    private final Map<String, Integer> levelCounts = new LinkedHashMap<>();

    private ApplicationLogAggregator() {}

    public static ApplicationLogAggregator getInstance() {
        return instance;
    }

    public void add(String level) {
        levelCounts.put(level, levelCounts.getOrDefault(level, 0) + 1);
    }

    public void writeToJson(String filePath) {
        
    	try (FileWriter writer = new FileWriter(filePath)) {
            new ObjectMapper().writerWithDefaultPrettyPrinter().writeValue(writer, levelCounts);
            System.out.println("[Aggregator] Application log levels written to " + filePath);
        } catch (IOException e) {
            System.err.println("[Aggregator] Error writing application JSON: " + e.getMessage());
        }
    }

	public Map<String, Integer> getLevelCounts() {
		return levelCounts;
	}
}
