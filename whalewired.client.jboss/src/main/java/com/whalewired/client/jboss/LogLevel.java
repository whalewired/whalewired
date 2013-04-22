package com.whalewired.client.jboss;

import java.util.logging.Level;



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

		if (Level.FINEST.equals(level)) {
			return TRACE;
		}
		
		if (Level.FINER.equals(level)) {
			return TRACE;
		}

		if (Level.FINE.equals(level)) {
			return DEBUG;
		}
		
		if (Level.CONFIG.equals(level)) {
			return INFO;
		}

		if (Level.INFO.equals(level)) {
			return INFO;
		}
		
		if (Level.WARNING.equals(level)) {
			return WARN;
		}
		if (Level.SEVERE.equals(level)) {
			return ERROR;
		}
		
		return UNKNOWN; 
	}
}
