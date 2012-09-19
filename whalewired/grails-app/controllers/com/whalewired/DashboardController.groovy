package com.whalewired

import com.whalewired.services.PatternInterval;
import grails.converters.JSON;
import javax.servlet.http.Cookie;

class DashboardController {
	def bookmarkLogEventService
	def elasticSearchService
	def elasticSearchAdminService
	static WW_BOOKMARK_PREFERRED_APP = "ww_bookmark_preferred_app"

	def index = {
		redirect(action: "show", params: params)
	}
	
	
	def show = {
		[currentApplication: getApplication(params)]
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
	
	
	
	/**
	 * Create JSON with with error occurences
	 * @return
	 */
	def errorOnApplicationAsJSON() {
		def applicationName = params.appName
		def timeInterval = PatternInterval.Interval.LAST_1_DAY
		
		def resultSet = elasticSearchService.findLogEventOccurrenceEntries(
			new PatternInterval(
				value: timeInterval,
				indexName: applicationName,
				logThrowableLocation: null,
				numberOfSamples: 4,
				logLocation: null,
				logThrowableType: null,
				logLevel: "ERROR"
			)
		)		
		render resultSet as JSON
	}
	
	
}
