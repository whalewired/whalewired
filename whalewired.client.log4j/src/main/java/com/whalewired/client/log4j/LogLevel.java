package com.whalewired.client.log4j;

import org.apache.log4j.Level;

public enum LogLevel {

/*
trace (the least serious)
debug
info
warn
error
fatal (the most serious)	
 */
	
	TRACE,
	DEBUG,
	INFO,
	WARN,
	ERROR,
	FATAL,
	UNKNOWN;
	
	public static LogLevel fromLogLevel(final Level level) {
		
		if (Level.ALL.equals(level)) {
			return TRACE;
		}

		if (Level.TRACE.equals(level)) {
			return TRACE;
		}

		if (Level.DEBUG.equals(level)) {
			return DEBUG;
		}

		if (Level.INFO.equals(level)) {
			return INFO;
		}
		
		if (Level.WARN.equals(level)) {
			return WARN;
		}
		if (Level.ERROR.equals(level)) {
			return ERROR;
		}
		if (Level.FATAL.equals(level)) {
			return FATAL;
		}
		
		return UNKNOWN; 
	}
}
