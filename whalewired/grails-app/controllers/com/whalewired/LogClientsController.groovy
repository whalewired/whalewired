package com.whalewired

import org.codehaus.groovy.grails.commons.ApplicationHolder

class LogClientsController {

	def index = {
        redirect(action: "list", params: params)
    }

	def list = {	
		
		def fileResourceInstanceList = []
		
		new File(grailsApplication.parentContext.servletContext.getRealPath("/client-files")).eachFile() { file->
			fileResourceInstanceList.add(file);
		}
		
		[ fileResourceInstanceList: fileResourceInstanceList ]
		
	}
}
