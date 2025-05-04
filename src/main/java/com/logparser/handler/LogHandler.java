package com.logparser.handler;

public interface LogHandler {
	
	void setNext(LogHandler next);
    void handle(String logLine);

}
