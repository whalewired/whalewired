package com.whalewired

import com.whalewired.services.ElasticSearchService;
import com.whalewired.services.ListResultSet
import com.whalewired.services.PatternInterval;
import grails.converters.JSON;
import javax.servlet.http.Cookie;

class DashboardController {
	def bookmarkLogEventService
	def elasticSearchAdminService
	def elasticSearchService
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
	
	
}
