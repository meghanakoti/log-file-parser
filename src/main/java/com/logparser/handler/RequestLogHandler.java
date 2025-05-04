package com.logparser.handler;

import com.logparser.aggregator.RequestLogAggregator;

public class RequestLogHandler implements LogHandler {
    
	private LogHandler next;
    private final RequestLogAggregator aggregator = RequestLogAggregator.getInstance();

    @Override
    public void setNext(LogHandler next) {
        this.next = next;
    }

    @Override
    public void handle(String logLine) {
        if (logLine.contains("request_url=") &&
            logLine.contains("response_status=") &&
            logLine.contains("response_time_ms=")) {
            try {
                String[] parts = logLine.split(" ");
                String url = null;
                int status = -1;
                int responseTime = -1;

                for (String part : parts) {
                    if (part.startsWith("request_url=")) {
                        url = part.substring("request_url=".length()).replaceAll("\"", "");
                    } else if (part.startsWith("response_status=")) {
                        status = Integer.parseInt(part.substring("response_status=".length()));
                    } else if (part.startsWith("response_time_ms=")) {
                        responseTime = Integer.parseInt(part.substring("response_time_ms=".length()));
                    }
                }

                if (url != null && status >= 0 && responseTime >= 0) {
                    aggregator.add(url, responseTime, status);
                }
            } catch (Exception e) {
                System.err.println("[Request Handler] Error parsing line: " + e.getMessage());
            }
        } else if (next != null) {
            next.handle(logLine);
        }
    }
}

