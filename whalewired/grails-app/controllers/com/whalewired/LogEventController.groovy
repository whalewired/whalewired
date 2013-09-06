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

	List<String> applications	
	String currentApplication
	String currentLogLevel
	String currentLogSearch
	
	def index = {
		redirect(action: "list", params: params)
	}

	def list = {
		
		if (params.logApplication) {
			currentApplication = params.logApplication
		}	
	}
	
	def list2 = {

		if (!applications) {
			applications = []
			elasticSearchAdminService.getIndices().each {
				applications << it.key
			}
		}
				
		if (params.logApplication) {
			currentApplication = params.logApplication
		}
		
		[applicationsJson: applications as JSON]
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
	
	def listJSON2 = {
/*		
		iSortCol_0: 0
		bRegex: false
		sSearch_0:
		sSearch:
		iSortingCols: 1
		mDataProp_0: 0
		bSearchable_0: true
		sSortDir_0: asc
		bSortable_0: true
		iDisplayStart: 0
		sColumns:
		iColumns: 1
		_: 1369944586996
		iDisplayLength: 10
		sEcho: 1
		bRegex_0: false
		
		page: options.page,
		pageSize: options.pageSize,
		take: options.take,
		skip: options.skip,
		sortField: options.sort && options.sort[0] ? options.sort[0].field : "",
		 sortOrder: options.sort && options.sort[0] ? options.sort[0].dir : "",
		logApplication: applicationValue,
		 logLevel: levelValue,
		 logSearch: $("#searchFilter").val()
*/
		
		currentApplication = params.logApplication ? params.logApplication : currentApplication;
		
		if (!currentApplication) {
			currentApplication = elasticSearchAdminService.getDefaultIndex();
		}
		currentLogLevel = params.logLevel;
		currentLogSearch = params.logSearch;
		
		def pageSize = params.pageSize?.toInteger();
		def skip = params.skip?.toInteger();

//		def resultSet = elasticSearchService.executeQuery(currentApplication, currentLogLevel, currentLogSearch,
//			pageSize, skip, params.sortField, params.sortOrder);
		def resultSet = elasticSearchService.executeQuery2("cla.nine.dk", currentLogLevel, currentLogSearch,
			params.iDisplayLength.toInteger(), params.iDisplayStart.toInteger(), params.sortField, params.sSortDir_0);
		
		
		
		
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
					externalIssue = jiraService.getIssue(currentApplication, issue.externalIssueId)
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
