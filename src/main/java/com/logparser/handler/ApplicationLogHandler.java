package com.logparser.handler;

import com.logparser.aggregator.ApplicationLogAggregator;

public class ApplicationLogHandler implements LogHandler {
    
	private LogHandler next;
    private final ApplicationLogAggregator aggregator = ApplicationLogAggregator.getInstance();

    @Override
    public void setNext(LogHandler next) {
        this.next = next;
    }

    @Override
    public void handle(String logLine) {
        if (logLine.contains("level=")) {
            try {
                String[] parts = logLine.split(" ");
                for (String part : parts) {
                    if (part.startsWith("level=")) {
                        String level = part.substring("level=".length()).toUpperCase();
                        aggregator.add(level);
                        return;
                    }
                }
            } catch (Exception e) {
                System.err.println("[Application Handler] Error parsing line: " + e.getMessage());
            }
        } else if (next != null) {
            next.handle(logLine);
        }
    }
}
