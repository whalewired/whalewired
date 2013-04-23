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
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.ResourceBundle;

import org.jboss.logmanager.ExtHandler;
import org.jboss.logmanager.ExtLogRecord;

import com.whalewired.org.json.JSONException;
import com.whalewired.org.json.JSONObject;

public class WhaleWiredHandler extends ExtHandler  {
	
	private String whalewired_es;
	private String whalewired_es_port;
	private String log_account;
	private String log_application;
	private String log_host;

	private static String clientVersion = ResourceBundle.getBundle("client").getString("client.version");
	private static long exceptionReportTime = 0;
	
	public WhaleWiredHandler() {
		super();
	}
	
	@Override
	protected synchronized void doPublish(ExtLogRecord record) {
		
		try {
			WhaleWiredHttpTransmitter transmitter = new WhaleWiredHttpTransmitter(getRecordAsJson(record),  
					whalewired_es, Integer.parseInt(whalewired_es_port), log_application);
			transmitter.transmit();
		} catch (Exception e) {
			e.printStackTrace();
			reportError(e);
		}
	}
	
	private String getRecordAsJson(ExtLogRecord event) throws JSONException {

		JSONObject jsonBuilder = new JSONObject();
		jsonBuilder.put("accountName", log_account);
		jsonBuilder.put("applicationName", log_application);
		jsonBuilder.put("hostName", log_host);
		jsonBuilder.put("logTime", event.getMillis());
		jsonBuilder.put("loggerName", event.getLoggerName());
		jsonBuilder.put("logFileName", event.getSourceFileName());
		jsonBuilder.put("logLineNumber", event.getSourceLineNumber());
		jsonBuilder.put("logMethodName", event.getSourceMethodName());
		jsonBuilder.put("logQualifiedClassName", event.getSourceClassName());
		StringBuilder locationBuilder = new StringBuilder();
		locationBuilder.append(event.getSourceClassName()+".");
		locationBuilder.append(event.getSourceMethodName()+"(");
		locationBuilder.append(event.getSourceFileName()+":");
		locationBuilder.append(event.getSourceLineNumber()+")");
		jsonBuilder.put("logLocation", locationBuilder.toString());
		StringBuilder locationShortBuilder = new StringBuilder();
		locationShortBuilder.append(event.getSourceFileName()+":");
		locationShortBuilder.append( event.getSourceLineNumber());
		jsonBuilder.put("logLocationShort", locationShortBuilder.toString());
		jsonBuilder.put("logMessage", event.getMessage());
		jsonBuilder.put("logLevel", LogLevel.fromLogLevel(event.getLevel()).name());
		jsonBuilder.put("logThread", event.getThreadName());
		if (event.getThrown() != null ) {
			jsonBuilder.put("logThrowableType", event.getThrown().getClass().getName());
			String[] trace = render(event.getThrown());
			if (trace != null && trace.length > 1) {
				jsonBuilder.put("logThrowableLocation", trace[1].substring(3)); // removes 'at '
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
		private final String whalewired_es;
		private final int whalewired_es_port;
		private final String log_application;

		public WhaleWiredHttpTransmitter(String eventAsJsonData,
				String whalewired_es, int whalewired_es_port,
				String log_application) {
			super();
			this.eventAsJsonData = eventAsJsonData;
			this.whalewired_es = whalewired_es;
			this.whalewired_es_port = whalewired_es_port;
			this.log_application = log_application;
		}

		public void transmit() {

			try {
				URL url;
				HttpURLConnection connection = null;
				try {
					// INDEX skal være account eller lignenede
					url = new URL("http", whalewired_es, whalewired_es_port, "/"+log_application+"/logevent/");
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
			System.err.println(e + "\nWhaleWiredAppender will not report errors again for 10 minutes  ");
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

	public String getWhalewired_es() {
		return whalewired_es;
	}

	public void setWhalewired_es(String whalewired_es) {
		this.whalewired_es = whalewired_es;
	}

	public String getWhalewired_es_port() {
		return whalewired_es_port;
	}

	public void setWhalewired_es_port(String whalewired_es_port) {
		this.whalewired_es_port = whalewired_es_port;
	}

	public String getLog_account() {
		return log_account;
	}

	public void setLog_account(String log_account) {
		this.log_account = log_account;
	}

	public String getLog_application() {
		return log_application;
	}

	public void setLog_application(String log_application) {
		this.log_application = log_application;
	}

	public String getLog_host() {
		return log_host;
	}

	public void setLog_host(String log_host) {
		this.log_host = log_host;
	}	
}
