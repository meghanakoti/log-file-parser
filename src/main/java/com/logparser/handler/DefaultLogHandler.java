// --- DefaultLogHandler.java ---
package com.logparser.handler;

public class DefaultLogHandler implements LogHandler {
    private LogHandler next;

    @Override
    public void setNext(LogHandler next) {
        this.next = null; // End of chain
    }

    @Override
    public void handle(String logLine) {
        // Do nothing
    }
}
