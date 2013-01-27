package com.whalewired.services

import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.common.unit.TimeValue;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.search.SearchHit;

import com.whalewired.BookmarkLogEvent;

class BookmarkLogEventService {
	def elasticSearchService
	def elasticSearchAdminService
		
	def deleteAllBookmarksAndUpdateLogs(indexName) {
		def dbBookmarks = findDbBookmarksByIndexName(indexName)
		/*
		 * delete bookmarks from db
		 */
		deleteDbBookmarks(dbBookmarks)
		
		/*
		 * run it in a thread as there might be a 
		 * lot of log events that need to be updated 
		 * and we don't want to block
		 */
		def th = Thread.start {				
			ListResultSet bookmarkedLogEvents = elasticSearchService.findBookmarkedLogEvents(indexName)
			/*
			 * reset bookmarks and update logs
			 */
			def updateCounter = 0
			bookmarkedLogEvents.result.each {Map mapSource ->
				mapSource.remove('bookmarkId')
				elasticSearchService.updateLog(indexName, mapSource._id, mapSource)
				updateCounter++
				if (updateCounter % 100 == 0) {
					log.debug("So far reseted " + updateCounter + " log events")
				}
			}
			log.debug("Reseted " + updateCounter + " log events")
		}	
		log.info("All bookmarks will be deleted after the thread completes")	
	}
	
	def deleteBookmarkAndUpdateLogs(bookmarkId) {
		def dbBookmark = BookmarkLogEvent.get(bookmarkId)
		if (dbBookmark) {
			dbBookmark.delete()
			if (dbBookmark.hasErrors()) {
				log.warn("could not delete bookmark: " + dbBookmark.getErrors())
			} else {
				log.info("bookmark '" + dbBookmark.getDescription() + "' deleted")
			}			
		}		
		
		/*
		 * run it in a thread as there might be a
		 * lot of log events that need to be updated
		 * and we don't want to block
		 */
		def th = Thread.start {
			ListResultSet bookmarkedLogEvents = elasticSearchService.findBookmarkedLogEvents(dbBookmark.indexName, bookmarkId)
			/*
			 * reset bookmarks and update logs
			 */
			def updateCounter = 0
			bookmarkedLogEvents.result.each {Map mapSource ->
				mapSource.remove('bookmarkId')
				elasticSearchService.updateLog(dbBookmark.indexName, mapSource._id, mapSource)
				updateCounter++
				/*
				if (updateCounter % 100 == 0) {
					log.debug("So far reseted " + updateCounter + " log events")
				}
				*/
			}
			log.debug("Reseted " + updateCounter + " log events")
		}
		log.info("Bookmarks will be deleted after the thread completes")
	}
	
	def deleteDbBookmarks(bookmarks) {
		bookmarks.each {BookmarkLogEvent bookmark ->
			bookmark.delete()
			if (bookmark.hasErrors()) {
				log.debug("bookmarks '" + bookmark.description
					+ "' could not be deleted: " + bookmark.errors)
			}
		}
	}

	def createBookmarkAndUpdateLogs(indexName, logEventId, description) {
		def logEventInstance = elasticSearchService.getLogEvent(indexName, logEventId)
		if (!logEventInstance.bookmarkId) {
			log.info("The log event '" + logEventId + "' is not bookmarked in el")
			
			/*
			 * There may already be a bookmark of this log event, 
			 * but this log event was not existing yet. Thus we need to update it in
			 * el with the bookmarkId.
			 */
			def bookmark = findDBBookmark(indexName,
				logEventInstance.logThrowableType,
					logEventInstance.logLocation,
					logEventInstance.logThrowableLocation);

			if (bookmark) {
				log.info("The log event'" + logEventId + "' with similar pattern is already bookmarked. " + 
					"The log event will be updated in el with bookmarkId '" + bookmark.id + "'")
				
				logEventInstance.bookmarkId = bookmark.id
				elasticSearchService.updateLog(indexName, logEventId, logEventInstance);
				
			} else {
				log.info("No similar pattern found for this log event. One will be created.");
				bookmark = createBookmark(indexName,
					logEventInstance.logThrowableType,
					logEventInstance.logLocation,
					logEventInstance.logThrowableLocation,
					description)
				
				if (bookmark.hasErrors()) {
					log.warn("could not create a bookmark for '" + description + "': " + bookmark.errors) 
					
				} else {
					/*
					 * run it in a thread as there might be a 
					 * lot of log events that need to be updated
					 * and we don't want to block
					 */
					Thread.start {
						log.info("starting thread...")
						elasticSearchService.updateLogs(indexName,
							logEventInstance.logThrowableLocation, 
							logEventInstance.logLocation, 
							logEventInstance.logThrowableType,
							[bookmarkId: bookmark.id])
					}										
					log.info("Bookmark created. So far created number of bookmarks: " 
						+ BookmarkLogEvent.count())
				}
			}	
		} else {
			log.info("Bookmark log event with this pattern is already created.")
		}
	}

	def findDBBookmark(indexName, logThrowableType, logLocation, logThrowableLocation) {
		def c = BookmarkLogEvent.createCriteria()
		def results = c {
			if (indexName != null) {
				eq("indexName", indexName)
			}
			if (logThrowableType != null) {
				eq("exceptionType", logThrowableType)
			}
			if (logLocation != null) {
				eq("logLocation", logLocation)
			}
			if (logThrowableLocation != null) {
				eq("throwableLocation", logThrowableLocation)
			}
		}
		def bookmark = null
		if (results.size() == 1) {
			bookmark = results.getAt(0);
		} else if (results.size() > 1) {
			log.debug("Found more than one bookmark. The first one in the list will be used.")
			bookmark = results.getAt(0);
		}
		return bookmark
	}
	
	def findDbBookmarksByIndexName(indexName) {
		def c = BookmarkLogEvent.createCriteria()
		def results = c {
			eq("indexName", indexName)
			order("dateCreated", "desc")
		}
		log.info("Found " + results.size() + " bookmarks for indexName '" + indexName + "'")
		return results
	}
	
	def findAllDbBookmarks() {
		def c = BookmarkLogEvent.createCriteria()
		def results = c {
			order("dateCreated", "desc")
		}
		log.info("Found " + results.size() + " bookmarks")
		return results
	}		

	def createBookmark(indexName, logThrowableType, logLocation, logThrowableLocation, description) {
		def bookmarkLogEvent = new BookmarkLogEvent(
				indexName: indexName,
				exceptionType: logThrowableType,
				logLocation: logLocation,
				throwableLocation: logThrowableLocation,
				description: description,
				dateCreated: new Date(System.currentTimeMillis()))
		bookmarkLogEvent.save(flush: true)
		return bookmarkLogEvent
	}
}
