package com.whalewired.client.log4j;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.InterruptedIOException;
import java.lang.reflect.InvocationTargetException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ResourceBundle;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.apache.log4j.AppenderSkeleton;
import org.apache.log4j.Layout;
import org.apache.log4j.helpers.LogLog;
import org.apache.log4j.spi.LocationInfo;
import org.apache.log4j.spi.LoggingEvent;
import org.codehaus.groovy.runtime.StackTraceUtils;

import com.whalewired.org.json.JSONException;
import com.whalewired.org.json.JSONObject;

public class GWhaleWiredAppender extends WhaleWiredAppender {

	@Override
	protected void append(final LoggingEvent event) {

		try {
			// Removes groovy specific addensums from the trace in order to calculate correct location
			Throwable t = StackTraceUtils.deepSanitize(new Throwable())
	
			// Generate correct location info that takes closures into account
			// Based on org.apache.log4j.spi.LocationInfo
			final String NA = "?"
			String fileName
			String className
			String methodName
	        String lineNumber
		
			String fqnOfCallingClass = event.getFQNOfLoggerClass()

			StackTraceElement[] elements =  (StackTraceElement[]) t.getStackTrace()
			String prevClass = NA;
			for(int i = elements.length - 1; i >= 0; i--) {
				
				String thisClass = elements[i].getClassName()
				
				if(fqnOfCallingClass.equals(thisClass) || thisClass.startsWith(fqnOfCallingClass)) {
					int caller = i + 1;
					if (caller < elements.length) {
						className = prevClass;
						methodName = elements[caller].getMethodName()
						fileName = elements[caller].getFileName()
						if (fileName == null) {
							fileName = NA;
						}
						int line = elements[caller].getLineNumber().intValue()
						if (line < 0) {
							lineNumber = NA;
						} else {
							lineNumber = String.valueOf(line)
						}
					}
					break
				}
				prevClass = thisClass
			}
			
			LocationInfo newInfo = new LocationInfo(fileName, className, methodName, lineNumber)
			
			LoggingEvent cleanedEvent = new LoggingEvent(event.fqnOfCategoryClass, event.getLogger(), event.timeStamp,
					event.getLevel(), event.getMessage(), event.getThreadName(), event.getThrowableInformation(),
					event.getNDC(), newInfo, event.getProperties())
			
			super.append(cleanedEvent);
			
		} catch (Exception e) {
			super.reportError(e)
			super.append(event)
		}
	}
}
