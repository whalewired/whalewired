package com.whalewired.utils

import groovyx.net.http.RESTClient

import java.security.KeyStore

import org.apache.http.conn.scheme.Scheme
import org.apache.http.conn.ssl.SSLSocketFactory

class RestUtil {
	
	public static RESTClient getRestClient() {
		
		def keyStore = KeyStore.getInstance(KeyStore.defaultType)
		RestUtil.class.getResource("/whalestore.jks").withInputStream {
			keyStore.load(it, "changeit".toCharArray())
		}
		
		def rest = new RESTClient()
		rest.client.connectionManager.schemeRegistry.register(new Scheme("https", new SSLSocketFactory(keyStore), 443))
		
		return rest
	}
}