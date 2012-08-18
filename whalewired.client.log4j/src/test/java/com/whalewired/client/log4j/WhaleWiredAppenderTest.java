package com.whalewired.client.log4j;

import java.util.Random;

import org.apache.log4j.Logger;
import org.junit.Test;

public class WhaleWiredAppenderTest {

	static Logger logger = Logger.getLogger("com.whalewired");
	static long maxmem = 0;

	@Test
	public void test() {
		Integer amount = 1000;
		System.out.println("Generating " + amount + " log events");

		for (int i = 0; i < amount; i++) {

			if (i % 6 == 0) {
				logTrace();
			} else if (i % 5 == 0) {
				logDebug();
			} else if (i % 4 == 0) {
				logInfo();
			} else if (i % 3 == 0) {
				logWarn();
			} else if (i % 2 == 0) {
				logError();
			} else {
				logFatal();
			}
			
			int mb = 1024*1024;
			System.gc();
	        Runtime runtime = Runtime.getRuntime();
	        long newmem = (runtime.totalMemory() - runtime.freeMemory()) / mb;
	        if (newmem != maxmem) {
	        	maxmem = newmem;
	        	System.out.println("Used Memory:" + maxmem);
	        }
			
			
		}
		try {
			Thread.sleep(1000);
			
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		{
		int mb = 1024*1024;
		System.gc();
        Runtime runtime = Runtime.getRuntime();
       	System.out.println("END Used Memory:" + (runtime.totalMemory() - runtime.freeMemory()) / mb);
		}
		try {
			Thread.sleep(10000);
			
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		{
			int mb = 1024*1024;
			System.gc();
	        Runtime runtime = Runtime.getRuntime();
	       	System.out.println("END 2 Used Memory:" + (runtime.totalMemory() - runtime.freeMemory()) / mb);
			
		}
		
	}
	
	private static void logTrace() {
		logger.trace(generateRandomText());
	}

	private static void logDebug() {
		logger.debug(generateRandomText());
	}

	private static void logInfo() {
		logger.info(generateRandomText());
	}

	private static void logWarn() {
		logger.warn(generateRandomText());
	}

	private static void logError() {
		try {
			throw new RuntimeException("ERROR - ERROR");
		} catch (Exception e) {
			try {
				throw new RuntimeException(generateRandomText(), e);
			} catch (Exception e2) {
				logger.error(generateRandomText() + "זרו", e2);
			}
		}

	}

	private static void logFatal() {
		try {
			throw new RuntimeException("FATAL - FATAL");
		} catch (Exception e) {
			try {
				throw new RuntimeException(generateRandomText(), e);
			} catch (Exception e2) {
				logger.error(generateRandomText(), e2);
			}
		}
	}

	private static String generateRandomText() {

		StringBuilder text = new StringBuilder();
		Random r = new Random();
		int words = (r.nextInt() % 20) + 1;
		int wordLength = (r.nextInt() % 20) + 1;
		for (int w = 0; w < words; w++) {

			for (int i = 0; i < wordLength; i++) {
				char tmp = (char) ('a' + r.nextInt('z' - 'a'));
				text.append(tmp);
			}
			text.append(" ");
		}
		return text.toString();
	}
}
