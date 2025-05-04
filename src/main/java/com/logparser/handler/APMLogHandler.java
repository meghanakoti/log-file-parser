package com.logparser.handler;

import com.logparser.aggregator.APMAggregator;

public class APMLogHandler implements LogHandler {
    
	private LogHandler next;
    private final APMAggregator aggregator = APMAggregator.getInstance();

    @Override
    public void setNext(LogHandler next) {
        this.next = next;
    }

    @Override
    public void handle(String logLine) {
        if (logLine.contains("metric=") && logLine.contains("value=")) {
            String[] parts = logLine.split(" ");
            String metric = null;
            Double value = null;

            for (String part : parts) {
                if (part.startsWith("metric=")) {
                    metric = part.substring("metric=".length());
                } else if (part.startsWith("value=")) {
                    try {
                        value = Double.parseDouble(part.substring("value=".length()));
                    } catch (NumberFormatException e) {
                        System.err.println("[APM Handler] Failed to parse value in log: " + part);
                    }
                }
            }

            if (metric != null && value != null) {
                aggregator.add(metric, value);
            } else {
                System.err.println("[APM Handler] Incomplete APM log line: " + logLine);
            }
        } else if (next != null) {
            next.handle(logLine);
        }
    }
}
