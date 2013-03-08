package com.whalewired.client.log4j;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ResourceBundle;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.apache.log4j.AppenderSkeleton;
import org.apache.log4j.spi.LoggingEvent;

import com.whalewired.org.json.JSONException;
import com.whalewired.org.json.JSONObject;

public class WhaleWiredAppender extends AppenderSkeleton {

	private String whalewired_es;
	private String whalewired_es_port;
	private String log_account;
	private String log_application;
	private String log_host;
	private String max_threads;

	private static ExecutorService executor;
	private static String clientVersion = ResourceBundle.getBundle("client").getString("client.version");
	private static long exceptionReportTime = 0;

	private ExecutorService getExecutor() {
		if (executor == null) {
			executor = Executors.newFixedThreadPool(Integer.parseInt(getMax_threads())); // JDK5++
		}
		return executor;
	}
	
	@Override
	public void activateOptions() {
		super.activateOptions();
		this.getExecutor();
	}

	@Override
	protected void append(final LoggingEvent event) {

		try {
			getExecutor().execute(new WhaleWiredHttpTransmitter(getEventAsJson(event),  getWhalewired_es(), getWhalewired_es_port(), getLog_application()));
		} catch (Exception e) {
			reportError(e);
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

	private String getEventAsJson(LoggingEvent event) throws JSONException {

		JSONObject jsonBuilder = new JSONObject();
		jsonBuilder.put("accountName", log_account);
		jsonBuilder.put("applicationName", log_application);
		jsonBuilder.put("hostName", log_host);
		jsonBuilder.put("logTime", event.getTimeStamp());
		jsonBuilder.put("loggerName", event.getLogger().getName());
		if (event.getLocationInformation() != null) {
			jsonBuilder.put("logFileName", event.getLocationInformation().getFileName());
			jsonBuilder.put("logLineNumber", event.getLocationInformation().getLineNumber());
			jsonBuilder.put("logMethodName", event.getLocationInformation().getMethodName());
			jsonBuilder.put("logQualifiedClassName", event.getLocationInformation().getClassName());
			StringBuilder locationBuilder = new StringBuilder();
			locationBuilder.append(event.getLocationInformation().getClassName()+".");
			locationBuilder.append(event.getLocationInformation().getMethodName()+"(");
			locationBuilder.append(event.getLocationInformation().getFileName()+":");
			locationBuilder.append(event.getLocationInformation().getLineNumber()+")");
			jsonBuilder.put("logLocation", locationBuilder.toString());
			StringBuilder locationShortBuilder = new StringBuilder();
			locationShortBuilder.append(event.getLocationInformation().getFileName()+":");
			locationShortBuilder.append(event.getLocationInformation().getLineNumber());
			jsonBuilder.put("logLocationShort", locationShortBuilder.toString());
		}
		jsonBuilder.put("logMessage", event.getMessage());
		jsonBuilder.put("logLevel", LogLevel.fromLogLevel(event.getLevel()).name());
		jsonBuilder.put("logThread", event.getThreadName());
		if (event.getThrowableInformation() != null && event.getThrowableInformation().getThrowable() != null) {
			jsonBuilder.put("logThrowableType", event.getThrowableInformation().getThrowable().getClass().getName());
			if (event.getThrowableInformation().getThrowableStrRep() != null && event.getThrowableInformation().getThrowableStrRep().length > 1) {
				jsonBuilder.put("logThrowableLocation", event.getThrowableInformation().getThrowableStrRep()[1].substring(3)); // removes 'at '
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

	private static class WhaleWiredHttpTransmitter implements Runnable {

		private final String eventAsJsonData;
		private final String whalewired_es;
		private final String whalewired_es_port;
		private final String log_application;

		public WhaleWiredHttpTransmitter(String eventAsJsonData,
				String whalewired_es, String whalewired_es_port,
				String log_application) {
			super();
			this.eventAsJsonData = eventAsJsonData;
			this.whalewired_es = whalewired_es;
			this.whalewired_es_port = whalewired_es_port;
			this.log_application = log_application;
		}

		public void run() {

			try {
				URL url;
				HttpURLConnection connection = null;
				try {
					// INDEX skal være account eller lignenede
					url = new URL("http", whalewired_es, Integer.parseInt(whalewired_es_port), "/"+log_application+"/logevent/");
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
	
	private static void reportError(Exception e) {
		
		if (exceptionReportTime + 3600000 < System.currentTimeMillis()) { // 10 minutes
			System.err.println(e + "\nWhaleWiredAppender will not report errors again for 10 minutes  ");
			exceptionReportTime = System.currentTimeMillis();
		}
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

	public String getMax_threads() {
		return max_threads;
	}

	public void setMax_threads(String max_threads) {
		this.max_threads = max_threads;
	}
}
