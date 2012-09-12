package com.whalewired

import java.awt.ModalEventFilter.ApplicationModalEventFilter;

import javax.servlet.http.Cookie;

import grails.converters.JSON;

import org.springframework.dao.DataIntegrityViolationException

import com.whalewired.services.ListResultSet;
import com.whalewired.services.PatternInterval;

class BookmarkLogEventController {

    //static allowedMethods = [save: "POST", update: "POST", delete: "POST"]
	
	static scope = "session"
	static WW_BOOKMARK_PREFERRED_APP = "ww_bookmark_preferred_app"
	def elasticSearchService
	def elasticSearchAdminService
	def bookmarkLogEventService		

    def index() {
        redirect(action: "list", params: params)
    }

    def list() {  			
		[currentApplication: getApplication(params)]		
    }		
	
	def delete() {
		def dbBookmark = BookmarkLogEvent.get(params.id)
		def indexName
		if (dbBookmark) {
			indexName = dbBookmark.indexName
		}
		bookmarkLogEventService.deleteBookmarkAndUpdateLogs(params.id)		
		redirect(action: "list",  params: [currentApplication: indexName])
	}

	def create() {
		bookmarkLogEventService.createBookmarkAndUpdateLogs(params.currentApplication,
			params.id,
			params.description)
		redirect(action: "list",  params: [currentApplication: params.currentApplication])
	}
	
	def details() {
		[bookmarkId: params.id]
	}
	
	def bookmarksAsJSON() {
		def application = getApplication(params)		
		def bookmarks = bookmarkLogEventService.findDbBookmarksByIndexName(application)		
		render bookmarksAsResultList(bookmarks) as JSON
	}
	
	
	def allBookmarksAsJSON() {
		def bookmarks = bookmarkLogEventService.findAllDbBookmarks();		
		render bookmarksAsResultList(bookmarks) as JSON
	}
	
	def bookmarkOccurrencesAsJSON() {
		// def lastNumberOfDays = 7
		def bookmarkLogEvent = BookmarkLogEvent.get(Long.valueOf(params.id))		
		def resultSet = elasticSearchService.findLogEventOccurrenceEntries(
			new PatternInterval(
				value: PatternInterval.Interval.LAST_12_HOURS,
				indexName: bookmarkLogEvent.indexName,
				logThrowableLocation: bookmarkLogEvent.throwableLocation,
				sampleSize: 6,
				logLocation: bookmarkLogEvent.logLocation,
				logThrowableType: bookmarkLogEvent.exceptionType)
			)
		
		/*
		def resultSet = elasticSearchService.findLogEventOccurrencesForLastNumberOfDays(
			bookmarkLogEvent.indexName,
			lastNumberOfDays,
			bookmarkLogEvent.throwableLocation,
			bookmarkLogEvent.logLocation,
			bookmarkLogEvent.exceptionType)
		*/
		
		render resultSet as JSON
	}
	
	def getApplication(params) {
		def application
		if (params.currentApplication) {
			application = params.currentApplication
			createApplicationCookie(application)
		} else {
			application = getApplicationCookie()
		}
		application
	}
		
	def getApplicationCookie() {
		def application = g.cookie(name: WW_BOOKMARK_PREFERRED_APP)
		if (!application) {
			application = createApplicationCookie(null)
		}		
		application
	}
	
	def createApplicationCookie(application) {
		def applicationCookie
		if (application) {
			applicationCookie = application
		} else {
			applicationCookie = elasticSearchAdminService.getDefaultIndex()
		} 
		def cookie = new Cookie(WW_BOOKMARK_PREFERRED_APP, applicationCookie);
		cookie.setMaxAge(Integer.MAX_VALUE);
		response.addCookie(cookie);
		applicationCookie
	}	
	
	def bookmarksAsResultList(bookmarks) {
		def resultSet = new ListResultSet()
		resultSet.result = bookmarks
		resultSet.total = bookmarks.size()
		resultSet
	}
	
	/*
	def deleteAll() {		
		bookmarkLogEventService.deleteAllBookmarksAndUpdateLogs(params.currentApplication)						
		redirect(action: "list",  params: [currentApplication: params.currentApplication])
	}
	*/	
}
