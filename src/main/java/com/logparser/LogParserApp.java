package com.logparser;

import com.logparser.handler.*;
import com.logparser.aggregator.*;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.Map;

public class LogParserApp {

    public static void main(String[] args) {
        if (args.length != 2 || !"--file".equals(args[0])) {
        	System.out.println("[Error] Missing or invalid arguments.");
        	System.out.println("Usage: java -jar log-parser-cli.jar --file <filename.txt>");
            return;
        }

        String fileName = args[1];
        System.out.println("[App] Starting log parsing for file: " + fileName);

        // Chain: APM → Application → Request → Default
        LogHandler apmHandler = new APMLogHandler();
        LogHandler appHandler = new ApplicationLogHandler();
        LogHandler requestHandler = new RequestLogHandler();
        LogHandler defaultHandler = new LogHandler() {
            public void setNext(LogHandler next) {}
            public void handle(String logLine) {}
        };

        apmHandler.setNext(appHandler);
        appHandler.setNext(requestHandler);
        requestHandler.setNext(defaultHandler);

        int lineCount = 0;
        try (BufferedReader reader = new BufferedReader(new FileReader(fileName))) {
            String line;
            System.out.println("[App] Processing contents in file ");
            while ((line = reader.readLine()) != null) {
                
            	lineCount++;
                try {
                    apmHandler.handle(line);
                } catch (Exception e) {
                    System.err.println("[App] Error processing line " + lineCount + ": " + e.getMessage());
                }
            }
        } catch (FileNotFoundException e) {
            System.err.println("[App] Input file not found: " + fileName);
        } catch (IOException e) {
            System.err.println("[App] Failed to read the input file: " + e.getMessage());
        }

        // --- APM Summary ---
        if (APMAggregator.getInstance().getParsedMetrics().isEmpty()) {
            System.out.println("[App] No APM logs found. Writing empty apm.json.");
        } else {
            System.out.println("[App] Parsed APM metrics: " + APMAggregator.getInstance().getParsedMetrics());
        }
        APMAggregator.getInstance().writeToJson("apm.json");
        System.out.println("[App] APM metrics written to apm.json");

        // --- Application Summary ---
        Map<String, Integer> appLogCounts = ApplicationLogAggregator.getInstance().getLevelCounts();
        if (appLogCounts.isEmpty()) {
            System.out.println("[App] No Application logs found. Writing empty application.json.");
        } else {
            System.out.println("[App] Parsed Application log levels: " + appLogCounts);
        }
        ApplicationLogAggregator.getInstance().writeToJson("application.json");
        System.out.println("[App] Application log counts written to application.json");

        // --- Request Summary ---
        if (RequestLogAggregator.getInstance().isEmpty()) {
            System.out.println("[App] No Request logs found. Writing empty request.json.");
        }
        RequestLogAggregator.getInstance().writeToJson("request.json");
        System.out.println("[App] Request log metrics written to request.json");

        System.out.println("[App] Finished log parsing.");
    }
}
