package com.whalewired.services

import static org.elasticsearch.node.NodeBuilder.*
import static com.whalewired.services.ElasticSearchAdminService.*;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.action.search.SearchType
import org.elasticsearch.common.unit.TimeValue
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.search.SearchHit

import com.whalewired.Issue;



class IssueService {

	def elasticSearchAdminService
	def elasticSearchService
	JiraService jiraService

	def getEsClient() {
		return elasticSearchAdminService.getEsClient();
	}


	String createIssueAndUpdateLogs(indexName, newExternalIssue, throwableLocation, logLocation, logThrowableType) {
		
		def issueKey = jiraService.createIssue(newExternalIssue)
		
		/*
		 * create issue
		 */
		Issue issue = createIssue(issueKey, throwableLocation, logLocation, logThrowableType);
		
		/*
		 * update logs with the issue id
		 */
		if (issue.hasErrors()) {
			println("errors: " + issue.errors);
		} else {
			println("so far created number of issues: " + Issue.count());

			elasticSearchService.updateLogs(indexName, 
				throwableLocation, logLocation, logThrowableType, 
				['issueId': issue.id])			
		}
		return issueKey
	}

	def createIssue(externalIssueId, throwableLocation, logLocation, logThrowableType) {

		def issue = new Issue('externalIssueId':externalIssueId,
				'exceptionType':logThrowableType,
				'mutedDate':null,
				'throwableLocation':throwableLocation,
				'logLocation':logLocation);
		issue.save();
		return issue;
	}

	def muteIssue(Issue issue) {

		issue.setMutedDate(new Date());
		
		// TODO skal vi scanne alle med issue id og markere muted p� hvert event ogs� - for at forsimple pr�sentationen
	}


}
