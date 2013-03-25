package com.whalewired.services

import java.security.KeyStore
import org.apache.http.conn.scheme.Scheme
import org.apache.http.conn.ssl.SSLSocketFactory

import com.whalewired.gui.ExternalIssue
import com.whalewired.utils.RestUtil

import grails.converters.JSON
import groovyx.net.http.ContentType
import groovyx.net.http.RESTClient

class JiraService {

    String createIssue(ExternalIssue issue) {
		
		log.debug 'Creating issue in JIRA: ' + (issue as JSON)
		
		def rest = RestUtil.getRestClient()
		
		String auth = 'bruger:pass'.bytes.encodeBase64().toString()
		
		def response;
		if (ExternalIssue.Type.BUG.equals(issue.type)) {
			response = rest.post(
				uri: 'https://traenportal.jira.com/rest/api/2/issue',
				headers: ['Authorization': 'Basic ' + auth],
				contentType: ContentType.JSON,
				requestContentType: ContentType.JSON,
				body: [
					fields: [
				        project: [
				            key: 'OPTAG'
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
				uri: 'https://traenportal.jira.com/rest/api/2/issue',
				headers: ['Authorization': 'Basic ' + auth],
				contentType: ContentType.JSON,
				requestContentType: ContentType.JSON,
				body: [
					fields: [
						project: [
							key: 'OPTAG'
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
	
	ExternalIssue getIssue(String issueKey) {
		
		log.debug 'Retrieving issue with key ' + issueKey 
		
		def rest = RestUtil.getRestClient()
		
		String auth = 'whalewired:Ballerup2012'.bytes.encodeBase64().toString()
		
		def response = rest.get(
			uri: 'https://traenportal.jira.com/rest/api/2/issue/' + issueKey,
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
