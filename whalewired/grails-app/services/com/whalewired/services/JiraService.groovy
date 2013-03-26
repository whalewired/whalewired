package com.whalewired.services

import java.security.KeyStore
import org.apache.http.conn.scheme.Scheme
import org.apache.http.conn.ssl.SSLSocketFactory

import com.whalewired.LogApp;
import com.whalewired.gui.ExternalIssue
import com.whalewired.utils.RestUtil

import grails.converters.JSON
import groovyx.net.http.ContentType
import groovyx.net.http.RESTClient

class JiraService {

    String createIssue(String appName, ExternalIssue issue) {
		
		log.debug 'Creating issue in JIRA: ' + (issue as JSON)
		
		def rest = RestUtil.getRestClient()
		
		LogApp logApp = LogApp.findByAppName(appName)
		
		println "logApp: " + logApp
		
		String auth = "${logApp.jiraUserName}:${logApp.jiraPassword}".toString().bytes.encodeBase64().toString()
		
		def response;
		if (ExternalIssue.Type.BUG.equals(issue.type)) {
			response = rest.post(
				uri: "${logApp.jiraUrl}",
				headers: ['Authorization': 'Basic ' + auth],
				contentType: ContentType.JSON,
				requestContentType: ContentType.JSON,
				body: [
					fields: [
				        project: [
				            key: "${logApp.jiraProject}"
				       	],
						issuetype: [
							name: issue.type.name
						],
						priority: [
							name: issue.priority.name
						],
						customfield_10030: [ value: issue.environment.name ],
						summary: issue.summary,
						description: issue.description
					]
				]
			)
		} else {
			response = rest.post(
				uri: "${logApp.jiraUrl}",
				headers: ['Authorization': 'Basic ' + auth],
				contentType: ContentType.JSON,
				requestContentType: ContentType.JSON,
				body: [
					fields: [
						project: [
							key: "${logApp.jiraProject}"
						   ],
						issuetype: [
							name: issue.type.name
						],
						priority: [
							name: issue.priority.name
						],
						summary: issue.summary,
						description: issue.description
					]
				]
			)
		}
		
		println 'Received response: ' + response
		println 'Created issue with key ' + response.data.key
		
		return response.data.key
    }
	
	ExternalIssue getIssue(String appName, String issueKey) {
		
		log.debug 'Retrieving issue with key ' + issueKey 
		
		LogApp logApp = LogApp.findByAppName(appName)
		
		def rest = RestUtil.getRestClient()
		
		String auth = "${logApp.jiraUserName}:${logApp.jiraPassword}".toString().bytes.encodeBase64().toString()
		
		def response = rest.get(
			uri: "${logApp.jiraUrl}/" + issueKey,
			headers: ['Authorization': 'Basic ' + auth],
			contentType: ContentType.JSON
		)
		
		log.debug 'Received response ' + response
		
		ExternalIssue retrievedIssue = new ExternalIssue()
		retrievedIssue.summary = response.data.fields.summary
		retrievedIssue.description = response.data.fields.description
		retrievedIssue.issueKey = response.data.key
		retrievedIssue.type = ExternalIssue.Type.valueOf(response.data.fields.issuetype.name.toUpperCase())
		retrievedIssue.priority = ExternalIssue.Priority.valueOf(response.data.fields.priority.name.toUpperCase())
		if (response.data.fields?.customfield_10030?.value) {
			retrievedIssue.environment = ExternalIssue.Environment.valueOf(response.data.fields?.customfield_10030?.value.toUpperCase())
		}
		
		log.debug 'Retrieved issue ' + (retrievedIssue as JSON)
		
		return retrievedIssue
	}
}
