package com.whalewired

import static org.elasticsearch.index.query.FilterBuilders.*
import static org.elasticsearch.index.query.QueryBuilders.*

import javax.servlet.http.Cookie;

import grails.converters.JSON
import groovy.json.JsonOutput

import com.whalewired.gui.ExternalIssue
import com.whalewired.services.ElasticSearchAdminService;
import com.whalewired.services.ElasticSearchService;
import com.whalewired.services.IssueService;
import com.whalewired.services.ListResultSet;
import com.whalewired.services.PatternInterval;
import com.whalewired.utils.Utils;
import com.whalewired.services.JiraService

class LogEventController {

	static scope = "session"
	
	ElasticSearchService elasticSearchService
	ElasticSearchAdminService elasticSearchAdminService
	IssueService issueService
	JiraService jiraService
	
	String currentApplication
	String currentLogLevel
	String currentLogSearch
	
	def index = {
		redirect(action: "list", params: params)
	}

	def list = {			
		
//		if (currentApplication == null) {
//			def cookie = g.cookie(name: "ww_preferred_app");
//			currentApplication = cookie;
//		} else {
//			def cookie = new Cookie("ww_preferred_app", currentApplication);
//			cookie.setMaxAge(Integer.MAX_VALUE);
//			response.addCookie(cookie);
//		}		
//		if (currentLogLevel == null) {
//			def cookie = g.cookie(name: "ww_preferred_level");
//			currentLogLevel = cookie;
//		} else {
//			def cookie = new Cookie("ww_preferred_level", currentLogLevel);
//			cookie.setMaxAge(Integer.MAX_VALUE);
//			response.addCookie(cookie);
//		}

	}


	def listJSON = {
		currentApplication = params.logApplication ? params.logApplication : currentApplication;
		if (!currentApplication) {
			currentApplication = elasticSearchAdminService.getDefaultIndex();
		}
		currentLogLevel = params.logLevel;
		currentLogSearch = params.logSearch;
		
		def pageSize = params.pageSize?.toInteger();
		def skip = params.skip?.toInteger();

		def resultSet = elasticSearchService.executeQuery(currentApplication, currentLogLevel, currentLogSearch, 
			pageSize, skip, params.sortField, params.sortOrder);
				
		render resultSet as JSON
	}

	def show = {
		def logEventInstance = chainModel?.logEventInstance // Might be chained from createIssue, check for that before harrassing elasticSearch.
		if (!logEventInstance) {
			logEventInstance = elasticSearchService.getLogEvent(currentApplication, params.id);
		}					
		
		def externalIssue
		if (!logEventInstance) {
			redirect(action: "list")
		} else {
			if (logEventInstance.issueId) {
				Issue issue = Issue.get(logEventInstance.issueId)
				if (issue?.externalIssueId) {
					externalIssue = jiraService.getIssue(issue.externalIssueId)
					println 'Issue found with key ' + externalIssue.issueKey
				} else {
					println 'Issue id ' + issue?.externalIssueId + ' found on logevent, but not found in jira ' 
				}
			}
			[logEventInstance: logEventInstance, 
				currentApplication: currentApplication, 
				types : ExternalIssue.Type, 
				priority : ExternalIssue.Priority, 
				environment : ExternalIssue.Environment, 
				externalIssue : externalIssue]
		}
	}	

	def showJSON = {
		def logEventInstance = elasticSearchService.getLogEvent(currentApplication, params.id);
		render logEventInstance as JSON;
	}

	def tail = {
		
		def tailString = "";
		def resultSet = elasticSearchService.executeQuery(currentApplication, currentLogLevel, currentLogSearch,
			10, 0,  "logTime", "desc");
		resultSet.result.each {
			tailString += (it["logTime"] + " ");
			tailString += (it["logLevel"] + ": ");
			tailString += (it["logQualifiedClassName"]+".");
			tailString += (it["logMethodName"]+"(");
			tailString += (it["logFileName"]+":");
			tailString += (it["logLineNumber"]+")");
			tailString += (it["logMessage"] ? " - "+it["logMessage"]+"\n" : "\n");
			tailString += (it["logThrowableTrace"] ? it["logThrowableTrace"] : "");
		}
		[tailString: tailString];
	}
	
	def createIssue() {
		
		ExternalIssue newExternalIssue = new ExternalIssue(description: params.description, summary: params.summary, type: params.type, priority: params.priority, environment: params.environment)
		
		def logEventInstance = elasticSearchService.getLogEvent(currentApplication, params.logEventId);
		
		logEventInstance.logTime = new Date(logEventInstance.logTime);
		newExternalIssue.description += "\n\n ---- Log entry ----\n" + JsonOutput.prettyPrint(JsonOutput.toJson(logEventInstance));

		def issueKey = issueService.createIssueAndUpdateLogs(currentApplication,
									newExternalIssue,
									logEventInstance.logThrowableLocation, 
									logEventInstance.logLocation, 
									logEventInstance.logThrowableType);
								
		flash.message = 'Created new issue with key ' + issueKey
		
		/*
		 *  reload the show page, this time showing information for the new issue!
		 */
		redirect(action: "show", params : [ id: logEventInstance._id])
	}
		
	//
	// TODO consider creating a chart controller and move this action there
	//
	def occurrencesAsJSON = {		
		def logEventId = params.id							
		def logEventInstance = elasticSearchService.getLogEvent(currentApplication, logEventId)		
		ListResultSet resultSet = elasticSearchService.findLogEventOccurrenceEntries(
			new PatternInterval(
				value: PatternInterval.Interval.valueOf(params.interval), 
				indexName: currentApplication,
				numberOfSamples: 10, 
				logThrowableLocation: logEventInstance.logThrowableLocation,
				logLocation: logEventInstance.logLocation,
				logThrowableType: logEventInstance.logThrowableType))
		
		render resultSet.result as JSON
	}
}
