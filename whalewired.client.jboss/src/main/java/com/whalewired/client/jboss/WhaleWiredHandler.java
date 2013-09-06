package com.whalewired.client.jboss;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.InterruptedIOException;
import java.io.LineNumberReader;
import java.io.PrintWriter;
import java.io.StringReader;
import java.io.StringWriter;
import java.lang.reflect.Method;
import java.net.HttpURLConnection;
import java.net.InetAddress;
import java.net.URL;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Map.Entry;
import java.util.ResourceBundle;
import java.util.Set;

import org.jboss.logmanager.ExtHandler;
import org.jboss.logmanager.ExtLogRecord;

import com.whalewired.org.json.JSONException;
import com.whalewired.org.json.JSONObject;

public class WhaleWiredHandler extends ExtHandler  {
	
	private String elasticSearchHost;
	private String elasticSearchPort;
	private String logSystem;
	private String logHost;
	private String logContext;

	private static String clientVersion = ResourceBundle.getBundle("client").getString("client.version");
	private static long exceptionReportTime = 0;
	
	public WhaleWiredHandler() {
		super();
	}
	
	@Override
	protected synchronized void doPublish(ExtLogRecord record) {
		
		cleanRecordIfGroovyEnvironment(record);
		
		try {
			WhaleWiredHttpTransmitter transmitter = new WhaleWiredHttpTransmitter(getRecordAsJson(record),  getElasticSearchHost(), getElasticSearchPort(), getLogSystem());
			transmitter.transmit();
		} catch (Exception e) {
			e.printStackTrace();
			reportError(e);
		}
	}
	
	@SuppressWarnings({"unchecked", "rawtypes" })
	private void cleanRecordIfGroovyEnvironment(ExtLogRecord rec) {
		
		try {
			Class stackTraceUtils = Class.forName("org.codehaus.groovy.runtime.StackTraceUtils");
			Method sanitizeMethod = stackTraceUtils.getDeclaredMethod("deepSanitize", Throwable.class);
			// Removes groovy specific addensums from the trace in order to calculate correct location
			Throwable t = (Throwable)sanitizeMethod.invoke(null, new Throwable());
			
			if (!clientVersion.endsWith("g")) {
				clientVersion = clientVersion + "g";
			}
	
	        final StackTraceElement[] stack = t.getStackTrace();
	        boolean found = false;
	        for (StackTraceElement element : stack) {
	            final String className = element.getClassName();
	            if (found) {
	                if (! rec.getLoggerClassName().equals(className)) {
	                    rec.setSourceClassName(className);
	                    rec.setSourceMethodName(element.getMethodName());
	                    rec.setSourceLineNumber(element.getLineNumber());
	                    rec.setSourceFileName(element.getFileName());
	                    return;
	                }
	            } else {
	                found = rec.getLoggerClassName().equals(className);
	            }
	        }
			
	        rec.setSourceClassName("<unknown>");
	        rec.setSourceMethodName("<unknown>");
	        rec.setSourceLineNumber(-1);
	        rec.setSourceFileName("<unknown>");
	        
		} catch (Exception e) {
			reportError(e);
		}
	}
	
	
	
	private String getRecordAsJson(ExtLogRecord event) throws JSONException {
		
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
		jsonBuilder.put("logTime", event.getMillis());
		jsonBuilder.put("loggerName", event.getLoggerName());
		jsonBuilder.put("contextName", logContext);
		
		for (Entry<String, String> entry : (Set<Entry<String, String>>)event.getMdcCopy().entrySet()) {
			if (entry.getValue() != null) {
				jsonBuilder.put(entry.getKey(), entry.getValue());
			}
		}
		
		jsonBuilder.put("logFileName", event.getSourceFileName());
		jsonBuilder.put("logLineNumber", event.getSourceLineNumber());
		jsonBuilder.put("logMethodName", event.getSourceMethodName());
		jsonBuilder.put("logQualifiedClassName", event.getSourceClassName());

		jsonBuilder.put("logMessage", event.getMessage());
		jsonBuilder.put("logLevel", LogLevel.fromLogLevel(event.getLevel()).name());
		jsonBuilder.put("logThread", event.getThreadName());
		if (event.getThrown() != null ) {
			jsonBuilder.put("logThrowableType", event.getThrown().getClass().getName());
			String[] trace = render(event.getThrown());
			if (trace != null && trace.length > 1) {
				String logThrowableLocation = trace[1];
				if (logThrowableLocation != null && logThrowableLocation.contains("Possible solutions")) {
					logThrowableLocation = "";
					if (trace.length > 2) {
						logThrowableLocation = trace[2];
					}
				}
				jsonBuilder.put("logThrowableLocation", logThrowableLocation);				
				StringBuilder traceBuilder = new StringBuilder();
				for (String s : trace) {
					traceBuilder.append(s + "\n");
				}
				jsonBuilder.put("logThrowableTrace", traceBuilder.toString());
			}
		}
		jsonBuilder.put("clientVersion", clientVersion);
		
		return jsonBuilder.toString();
		
	}

	private static class WhaleWiredHttpTransmitter {

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

		public void transmit() {

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
	
	public static void reportError(Exception e) {
		
		if (exceptionReportTime + 3600000 < System.currentTimeMillis()) { // 10 minutes
			System.err.println(e + "\nWhaleWiredHandler will not report errors again for 10 minutes  ");
			exceptionReportTime = System.currentTimeMillis();
		}
	}

	/**
     * Render throwable using Throwable.printStackTrace.
     * @param throwable throwable, may not be null.
     * @return string representation.
     */
    public static String[] render(final Throwable throwable) {
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        try {
            throwable.printStackTrace(pw);
        } catch(RuntimeException ex) {
        }
        pw.flush();
        LineNumberReader reader = new LineNumberReader(
                new StringReader(sw.toString()));
        ArrayList<String> lines = new ArrayList<String>();
        try {
          String line = reader.readLine();
          while(line != null) {
            lines.add(line);
            line = reader.readLine();
          }
        } catch(IOException ex) {
            if (ex instanceof InterruptedIOException) {
                Thread.currentThread().interrupt();
            }
            lines.add(ex.toString());
        }
        String[] tempRep = new String[lines.size()];
        lines.toArray(tempRep);
        return tempRep;
    }

	public String getElasticSearchHost() {
		return elasticSearchHost;
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
    

}
