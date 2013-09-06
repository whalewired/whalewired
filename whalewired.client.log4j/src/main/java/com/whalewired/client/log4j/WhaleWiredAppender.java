package com.whalewired.client.log4j;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Method;
import java.net.HttpURLConnection;
import java.net.InetAddress;
import java.net.URL;
import java.net.UnknownHostException;
import java.util.Map.Entry;
import java.util.ResourceBundle;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.apache.log4j.AppenderSkeleton;
import org.apache.log4j.spi.LocationInfo;
import org.apache.log4j.spi.LoggingEvent;

import com.whalewired.org.json.JSONException;
import com.whalewired.org.json.JSONObject;

public class WhaleWiredAppender extends AppenderSkeleton {

	private String elasticSearchHost;
	private String elasticSearchPort;
	private String logSystem;
	private String logHost;
	private String logContext;
	private String maxThreads = "1";

	private static ExecutorService executor;
	private static String clientVersion = ResourceBundle.getBundle("client").getString("client.version");
	private static long exceptionReportTime = 0;

	private ExecutorService getExecutor() {
		if (executor == null) {
			executor = Executors.newFixedThreadPool(Integer.parseInt(getMaxThreads())); // JDK5++
		}
		return executor;
	}
	
	@Override
	public void activateOptions() {
		super.activateOptions();
		this.getExecutor();
	}

	@Override
	protected void append(final LoggingEvent ev) {
		
		LoggingEvent event = cleanEventIfGroovyEnvironment(ev);

		try {
			getExecutor().execute(new WhaleWiredHttpTransmitter(getEventAsJson(event),  getElasticSearchHost(), getElasticSearchPort(), getLogSystem()));
		} catch (Exception e) {
			reportError(e);
		}
		
	}
	
	@SuppressWarnings({"unchecked", "rawtypes" })
	private LoggingEvent cleanEventIfGroovyEnvironment(LoggingEvent event) {
		
		try {
			Class stackTraceUtils = Class.forName("org.codehaus.groovy.runtime.StackTraceUtils");
			Method sanitizeMethod = stackTraceUtils.getDeclaredMethod("deepSanitize", Throwable.class);
			// Removes groovy specific addensums from the trace in order to calculate correct location
			Throwable t = (Throwable)sanitizeMethod.invoke(null, new Throwable()); 
	
			if (!clientVersion.endsWith("g")) {
				clientVersion = clientVersion + "g";
			}
			
			// Generate correct location info that takes closures into account
			// Based on org.apache.log4j.spi.LocationInfo
			final String NA = "?";
			String fileName = NA;
			String className = NA;
			String methodName = NA;
	        String lineNumber = NA;
		
			String fqnOfCallingClass = event.getFQNOfLoggerClass();

			StackTraceElement[] elements =  (StackTraceElement[]) t.getStackTrace();
			String prevClass = NA;
			
			for(int i = elements.length - 1; i >= 0; i--) {
				
				String thisClass = elements[i].getClassName();
				
				if(fqnOfCallingClass.equals(thisClass) || thisClass.startsWith(fqnOfCallingClass)) {
					int caller = i + 1;
					if (caller < elements.length) {
						className = prevClass;
						methodName = elements[caller].getMethodName();
						fileName = elements[caller].getFileName();
						if (fileName == null) {
							fileName = NA;
						}
						int line = elements[caller].getLineNumber();
						if (line < 0) {
							lineNumber = NA;
						} else {
							lineNumber = String.valueOf(line);
						}
					}
					break;
				}
				prevClass = thisClass;
			}
			
			LocationInfo newInfo = new LocationInfo(fileName, className, methodName, lineNumber);
			
			LoggingEvent cleanedEvent = new LoggingEvent(event.fqnOfCategoryClass, event.getLogger(), event.timeStamp,
					event.getLevel(), event.getMessage(), event.getThreadName(), event.getThrowableInformation(),
					event.getNDC(), newInfo, event.getProperties());
			
			return cleanedEvent;
			
		} catch (Exception e) {
			reportError(e);
			return event;
		}
	}

	@Override
	public void close() {

		try {
			executor.shutdown();
			executor = null;
		} catch (Exception e) {
			reportError(e);
		}
	}

	@Override
	public boolean requiresLayout() {
		return false;
	}

	@SuppressWarnings("unchecked")
	private String getEventAsJson(LoggingEvent event) throws JSONException {
		
		if (this.logHost == null || "".equals(this.logHost.trim())) {
			try {
				this.logHost = InetAddress.getLocalHost().getHostName();
			} catch (UnknownHostException e) {
				this.logHost = "Unknown";
			}
		}

		JSONObject jsonBuilder = new JSONObject();
		jsonBuilder.put("systemName", logSystem);
		jsonBuilder.put("hostName", logHost);
		jsonBuilder.put("logTime", event.getTimeStamp());
		jsonBuilder.put("loggerName", event.getLogger().getName());
		jsonBuilder.put("contextName", logContext);
		
		for (Entry<String, Object> entry : (Set<Entry<String, Object>>)event.getProperties().entrySet()) {
			if (entry.getValue() != null) {
				jsonBuilder.put(entry.getKey(), entry.getValue().toString());
			}
		}
		
		if (event.getLocationInformation() != null) {
			jsonBuilder.put("logFileName", event.getLocationInformation().getFileName());
			jsonBuilder.put("logLineNumber", event.getLocationInformation().getLineNumber());
			jsonBuilder.put("logMethodName", event.getLocationInformation().getMethodName());
			jsonBuilder.put("logQualifiedClassName", event.getLocationInformation().getClassName());
		}
		jsonBuilder.put("logMessage", event.getMessage());
		jsonBuilder.put("logLevel", LogLevel.fromLogLevel(event.getLevel()).name());
		jsonBuilder.put("logThread", event.getThreadName());
		if (event.getThrowableInformation() != null && event.getThrowableInformation().getThrowable() != null) {
			jsonBuilder.put("logThrowableType", event.getThrowableInformation().getThrowable().getClass().getName());
			if (event.getThrowableInformation().getThrowableStrRep() != null && event.getThrowableInformation().getThrowableStrRep().length > 1) {
				String logThrowableLocation = event.getThrowableInformation().getThrowableStrRep()[1];
				if (logThrowableLocation != null && logThrowableLocation.contains("Possible solutions")) {
					logThrowableLocation = "";
					if (event.getThrowableInformation().getThrowableStrRep().length > 2) {
						logThrowableLocation = event.getThrowableInformation().getThrowableStrRep()[2];
					}
				}
				jsonBuilder.put("logThrowableLocation", logThrowableLocation);
			}
		}
		if (event.getThrowableStrRep() != null) {
			StringBuilder traceBuilder = new StringBuilder();
			for (String s : event.getThrowableStrRep()) {
				traceBuilder.append(s + "\n");
			}
			jsonBuilder.put("logThrowableTrace", traceBuilder.toString());
		}
		jsonBuilder.put("clientVersion", clientVersion);
		
		return jsonBuilder.toString();
	}
	
	public static void reportError(Exception e) {
		
		if (exceptionReportTime + 3600000 < System.currentTimeMillis()) { // 10 minutes
			System.err.println(e + "\nWhaleWiredAppender will not report errors again for 10 minutes  ");
			exceptionReportTime = System.currentTimeMillis();
		}
	}

	public String getElasticSearchHost() {
		
		return this.elasticSearchHost;
	}

	public void setElasticSearchHost(String elasticSearchHost) {
		this.elasticSearchHost = elasticSearchHost;
	}

	public String getElasticSearchPort() {
		return elasticSearchPort;
	}

	public void setElasticSearchPort(String elasticSearchPort) {
		this.elasticSearchPort = elasticSearchPort;
	}

	public String getLogSystem() {
		return logSystem;
	}

	public void setLogSystem(String logSystem) {
		this.logSystem = logSystem;
	}

	public String getLogHost() {
		return logHost;
	}

	public void setLogHost(String logHost) {
		this.logHost = logHost;
	}

	public String getLogContext() {
		return logContext;
	}

	public void setLogContext(String logContext) {
		this.logContext = logContext;
	}

	public String getMaxThreads() {
		return maxThreads;
	}

	public void setMaxThreads(String maxThreads) {
		this.maxThreads = maxThreads;
	}
	
	private static class WhaleWiredHttpTransmitter implements Runnable {

		private final String eventAsJsonData;
		private final String elasticSearchIp;
		private final String elasticSearchPort;
		private final String logSystem;

		public WhaleWiredHttpTransmitter(String eventAsJsonData,
				String elasticSearchIp, String elasticSearchPort,
				String logSystem) {
			super();
			this.eventAsJsonData = eventAsJsonData;
			this.elasticSearchIp = elasticSearchIp;
			this.elasticSearchPort = elasticSearchPort;
			this.logSystem = logSystem;
		}

		public void run() {

			try {
				URL url;
				HttpURLConnection connection = null;
				try {
					url = new URL("http", elasticSearchIp, Integer.parseInt(elasticSearchPort), "/"+logSystem+"/logevent/");
					connection = (HttpURLConnection) url.openConnection();
					connection.setConnectTimeout(5000);
					connection.setReadTimeout(5000);
					connection.setRequestMethod("POST");
					connection.setRequestProperty("Content-Type", "application/json");
					connection.setRequestProperty("Content-Length", ""+ Integer.toString(eventAsJsonData.getBytes().length));
					connection.setRequestProperty("Content-Language", "da-DK");
					connection.setUseCaches(false);
					connection.setDoInput(true);
					connection.setDoOutput(true);

					// Send request
					DataOutputStream wr = null;
					try {
						wr = new DataOutputStream(connection.getOutputStream());
					wr.write(eventAsJsonData.getBytes("UTF-8"));
					} finally {
						if (wr != null) {
							wr.flush();
							wr.close();
							wr = null;
						}
					}
					
					// Get Response
					InputStream is = null;
					BufferedReader rd = null;
					try {
						is = connection.getInputStream();
						rd = new BufferedReader(new InputStreamReader(is));
						String line;
						StringBuffer response = new StringBuffer();
						while ((line = rd.readLine()) != null) {
							response.append(line);
							response.append('\r');
						}
					} finally {
						if (is != null) {
							is.close();
							is = null;
						}
						if (rd != null) {
							rd.close();
							rd = null;
						}
					}
				} finally {
					if (connection != null) {
						connection.disconnect();
						connection = null;
					}
				}
			} catch (Exception e) {
				reportError(e);
			}
		}
	}
	
	

}
