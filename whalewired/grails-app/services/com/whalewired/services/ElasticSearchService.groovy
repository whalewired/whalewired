package com.whalewired.services
import static org.elasticsearch.index.query.FilterBuilders.*;
import static org.elasticsearch.index.query.QueryBuilders.*;
import static org.elasticsearch.node.NodeBuilder.*;
import static com.whalewired.services.ElasticSearchAdminService.*;

import org.elasticsearch.search.*;
import org.elasticsearch.search.facet.datehistogram.DateHistogramFacetProcessor.DateFieldParser.DayOfMonth;
import org.elasticsearch.search.sort.SortOrder
import org.elasticsearch.action.get.GetResponse;
import org.elasticsearch.action.search.*;
import org.elasticsearch.client.Client;
import org.elasticsearch.common.unit.TimeValue;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.TextQueryBuilder

import com.sun.org.apache.xalan.internal.xsltc.compiler.CurrentCall;

import java.util.Calendar;
import static org.elasticsearch.groovy.node.GNodeBuilder.*


class ElasticSearchService {

	def elasticSearchAdminService;
	def weekDays = ["Sun", "Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun"]
	
	def findLogEventOccurrencesForLastNumberOfDays(indexName, lastNumberOfDays, throwableLocation, logLocation, logThrowableType) {
		def resultSet = new ListResultSet()		
		for (int i=lastNumberOfDays; i>=0; i--) {
			LogEventOccurrenceEntry entry = findLogEventOccurrenceEntryDaysAgo(indexName, i, 
				throwableLocation, logLocation, logThrowableType);
			
			resultSet.result.add(entry);
		}		
		return resultSet;
	}
		
	def findLogEventOccurrenceEntries(PatternInterval patternInterval) {		
		def resultSet = new ListResultSet()		
		patternInterval.getFromToCalendarList().each { fromToPair ->
			def from = fromToPair[0]
			def to = fromToPair[1]
			LogEventOccurrenceEntry entry = findLogEventOccurrenceEntry(from, to, "HH:mm",
				patternInterval.indexName,
				patternInterval.logThrowableLocation,
				patternInterval.logLocation,
				patternInterval.logThrowableType,
				patternInterval.logLevel)
				
			resultSet.result << entry
		}
		resultSet
	}
	
	def findLogEventOccurrenceEntry(fromCalendar, toCalendar,
		datePattern, indexName,
		throwableLocation, logLocation, logThrowableType, logLevel) {
		/*
		 * query with filter
		 */
		QueryBuilder boolQuery = createLogEventPatternQueryBuilder(throwableLocation, logLocation, logThrowableType, logLevel)

		QueryBuilder filteredQuery = filteredQuery(
				boolQuery,
				rangeFilter("logTime")
				.from(fromCalendar.getTime().getTime())
				.to(toCalendar.getTime().getTime())
				);
			
			
		log.info("from: " + fromCalendar.getTime())
		log.info("to: " + toCalendar.getTime())
		log.info("query: " + filteredQuery.toString())
		

		Client client = getEsClient()
		SearchResponse resp = client.prepareSearch()
				.setIndices(indexName)
				.setTypes(ES_TYPE)
				.setQuery(filteredQuery)
				.execute().actionGet()
		def totalHits = resp.getHits().getTotalHits()

		// log.info("number of hits:  " + totalHits)
		return new LogEventOccurrenceEntry(
			date: weekDays[fromCalendar.get(Calendar.DAY_OF_WEEK)-1]+" ["+fromCalendar.format(datePattern) + "-" + toCalendar.format(datePattern) + "]",
			dayOfWeek: fromCalendar.get(Calendar.DAY_OF_WEEK),
			occurs: totalHits);
	}
		
	def findLogEventOccurrenceEntryDaysAgo(indexName, daysAgo, throwableLocation, logLocation, logThrowableType) {
		/*
		* from
		*/
	   def from = Calendar.getInstance()
	   from.add(Calendar.DAY_OF_YEAR, -daysAgo)
	   from.set(Calendar.HOUR_OF_DAY, 0)
	   from.set(Calendar.MINUTE, 0)
	   from.set(Calendar.SECOND, 0)
	   from.set(Calendar.MILLISECOND, 0)
	   /*
		* to
		*/
	   def to = Calendar.getInstance()
	   to.add(Calendar.DAY_OF_YEAR, -daysAgo)
	   to.set(Calendar.HOUR_OF_DAY, 23)
	   to.set(Calendar.MINUTE, 59)
	   to.set(Calendar.SECOND, 59)
	   to.set(Calendar.MILLISECOND, 999)
	   
	   def datePattern = "MM/dd/yyyy"
	   
	   findLogEventOccurrenceEntry(from, to, datePattern, 
		   indexName, daysAgo, 
		   throwableLocation, logLocation, logThrowableType)
	}
	
	
	
	def findBookmarkedLogEvents(indexName) {		
		QueryBuilder builder = filteredQuery(matchAllQuery(),
			/*
			 * assuming that all bookmarks have an id starting from 1
			 * that one will work 
			 */
			rangeFilter("bookmarkId").from(1))				
		return buildLogEventSourceListFromBuildQuery(indexName, builder) 
	}
	
	public ListResultSet findBookmarkedLogEvents(indexName, bookmarkId) {
		BoolQueryBuilder builder = boolQuery();
		builder.must(new TextQueryBuilder('bookmarkId', bookmarkId).type(TextQueryBuilder.Type.PHRASE_PREFIX));		
		return buildLogEventSourceListFromBuildQuery(indexName, builder);
	}
		
	public ListResultSet executeQuery(logApplication, logLevel, logSearch, pageSize, skip, sortField, sortOrder) {

		def resultSet = new ListResultSet();
		
		Client esClient = getEsClient();
		
		def sortFields = [];
		if (sortField) {
			if ("logLocationShort".equals(sortField)) {
				sortFields.add("logFileName");
				sortFields.add("logLineNumber");
			} else if ("logMessage".equals(sortField)) {
				sortFields.add("logMessage.untouched");
			} else {
				sortFields.add(sortField);
			}
		}

		def searchRequestBuilder = esClient.prepareSearch(logApplication);
		searchRequestBuilder.setTypes(ES_TYPE);
		searchRequestBuilder.setFrom(skip);
		searchRequestBuilder.setSize(pageSize);
		if (sortFields) {
			sortFields.each {
				searchRequestBuilder.addSort(it,  SortOrder.valueOf(sortOrder.toUpperCase()));
			}
		}
		
		if (logLevel) {
			searchRequestBuilder.setFilter(termFilter('logLevel',logLevel));
		}
		
		if (logSearch) {
			TextQueryBuilder tqb = new TextQueryBuilder("_all", logSearch).type(TextQueryBuilder.Type.PHRASE_PREFIX);
			searchRequestBuilder.setQuery(tqb);
		}

		def searchResponse = searchRequestBuilder.execute().actionGet();

		searchResponse.hits.each {SearchHit hit ->
			def sourceMap = hit.getSource();
			sourceMap["_id"] = hit.getId();
			resultSet.result.add(sourceMap);
		}

		resultSet.total = searchResponse.hits.getTotalHits();

		return resultSet;
	}

	def getLogEvent(indexName, id) {
		Client client = getEsClient(); 
		GetResponse result = client.prepareGet(indexName, ES_TYPE, id).execute().actionGet();
		if (result.source) { 
			result.source._id = id;
		}
		return result.source;
	
	}

	def getEsClient() {
		return elasticSearchAdminService.getEsClient();
	}
	
	def updateLogs(indexName, logThrowableLocation, logLocation, logThrowableType, fieldsToUpdate) {		
		BoolQueryBuilder buildQuery = createLogEventPatternQueryBuilder(logThrowableLocation, logLocation, logThrowableType, null)
		def resultSet = buildLogEventSourceListFromBuildQuery(indexName, buildQuery)
		def updateCounter = 0
		log.info("resultSet.size: " + resultSet.result.size())
		resultSet.result.each {Map sourceMap ->
			sourceMap.putAll(fieldsToUpdate);
			updateLog(indexName, sourceMap._id, sourceMap);
			updateCounter++
			if (updateCounter % 100 == 0) {
				// log.info("So far updated " + updateCounter + " log events")
			} 
		}
		log.info("Updated " + updateCounter + " log events")				
	}	
	
	def updateLog(indexName, logId, fields) {
		Client client = getEsClient();
		client.prepareIndex(indexName, ES_TYPE, logId).setSource(fields).execute().actionGet();
		
		/*
		 * Used to verify if the log got updated with the fields			
		 */
		// println("updated fields: " + client.prepareGet(indexName, ES_TYPE, logId).execute().actionGet().getSource());
	}
	
	def buildLogEventSourceListFromBuildQuery(indexName, buildQuery) {
		log.info("query: " + buildQuery.toString())
		
		ListResultSet resultSet = new ListResultSet()
		Client client = getEsClient()
		// Scroll until no hits are returned
		SearchResponse scrollResp = client.prepareSearch()
			.setIndices(indexName)
			.setTypes(ES_TYPE)
			.setSearchType(SearchType.SCAN)
			.setQuery(buildQuery)
			.setScroll(new TimeValue(1000))
			.setSize(100).execute().actionGet() // 100 hits per shard will be returned for each scroll												
		resultSet.total = scrollResp.getHits().getTotalHits()
		
		log.info("hits: " + resultSet.total)
				
		def hitCounter = 0;
		while (true) {			
			scrollResp = client.prepareSearchScroll(scrollResp.getScrollId()).setScroll(
					new TimeValue(1000)).execute().actionGet()
			
			boolean hitsRead = false
			// for (SearchHit hit : scrollResp.getHits()) {
			scrollResp.getHits().each {SearchHit hit ->
				hitsRead = true;
				def sourceMap = hit.source
				sourceMap._id = hit.id
				resultSet.result << sourceMap
				hitCounter++
				/*				
				if (hitCounter % 100 == 0) {
					log.info("So far created " + hitCounter + " entries in resultSet")
				}	
				*/			
			}							
			// Break condition: No hits are returned
			if (!hitsRead) {
				break;
			}
		}
		log.info("Created " + hitCounter + " entries in resultSet")
		return resultSet
	}
	
	def createLogEventPatternQueryBuilder(logThrowableLocation, logLocation, logThrowableType, logLevel) {
		BoolQueryBuilder bool = boolQuery();
		if (logThrowableLocation != null) {
			bool.must(new TextQueryBuilder('logThrowableLocation', 
				logThrowableLocation).type(TextQueryBuilder.Type.PHRASE_PREFIX));
		}
		if (logLocation != null) {
			bool.must(new TextQueryBuilder('logLocation',
					logLocation).type(TextQueryBuilder.Type.PHRASE_PREFIX));
		}
		if (logThrowableType != null) {
			bool.must(new TextQueryBuilder('logThrowableType',
					logThrowableType).type(TextQueryBuilder.Type.PHRASE_PREFIX));
		}
		if (logLevel != null) {
			bool.must(new TextQueryBuilder('logLevel',
					logLevel).type(TextQueryBuilder.Type.PHRASE_PREFIX));
		}
		return bool
	}
	
}
