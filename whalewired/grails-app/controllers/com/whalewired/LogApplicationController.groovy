package com.whalewired

import org.codehaus.groovy.grails.web.json.JSONElement;

import com.whalewired.services.ElasticSearchAdminService;
import com.whalewired.services.ElasticSearchService;
import com.whalewired.services.ListResultSet;

import grails.converters.JSON;
import grails.plugins.springsecurity.Secured;




class LogApplicationController {

	ElasticSearchService elasticSearchService;
	ElasticSearchAdminService elasticSearchAdminService;
	
    static allowedMethods = [save: "POST", update: "POST", delete: "POST"]

    def index = {
        redirect(action: "list", params: params)
    }

	@Secured(['ROLE_ADMIN'])
	def list = {
		render(view:"list", params: params);
	}
	
	def listJSON = {
		
		def indices = elasticSearchAdminService.getIndices();
		def result = new ListResultSet();
		for (index in indices) {
			LogApp logApp = LogApp.findByAppName(index.key)
			result.result.add(id: index.key, name: index.key, ttl: index.value.getSettings().get("index.ttl"),
				jiraUserName: logApp?.jiraUserName, jiraPassword: logApp?.jiraPassword, jiraUrl: logApp?.jiraUrl, jiraProject: logApp?.jiraProject);
		}
		result.total = result.result.size();
		
		println("LogApplicationController.listJSON: "+(result as JSON));
		render result as JSON;
	}


    @Secured(['ROLE_ADMIN'])
	def create = {
		
		println "create:" + params 
		def model = JSON.parse(params.model)[0]
		try {
			elasticSearchAdminService.createIndex(model.name, model.ttl);
			model.id = model.name
			
			LogApp logApp = new LogApp(appName: model.name, jiraUserName: model.jiraUserName, 
				jiraPassword: model.jiraPassword, jiraUrl: model.jiraUrl, jiraProject: model.jiraProject)
			logApp.save()
			
		} catch (Exception e) {
			println(e);
			render(status: 422, text: 'error')
		}
		
		render model2result(model) as JSON;
    }

    
	@Secured(['ROLE_ADMIN'])
	def update = {
		println "update:" + params
		def model = JSON.parse(params.model)[0]
		elasticSearchAdminService.updateIndex(model.name, model.ttl.toInteger());
		
		LogApp logApp = LogApp.findOrCreateWhere([appName: model.name])
		logApp.jiraUserName = model.jiraUserName
		logApp.jiraPassword = model.jiraPassword
		logApp.jiraUrl = model.jiraUrl
		logApp.jiraProject = model.jiraProject
		logApp.merge()

		render model2result(model) as JSON;
    }

    @Secured(['ROLE_ADMIN'])
	def delete = {
		println "DELETE:" + params
		def model = JSON.parse(params.model)[0]
		elasticSearchAdminService.deleteIndex(model.name);
		
		LogApp logApp = LogApp.findByAppName(model.name)
		logApp?.delete()
		
		render model2result(model) as JSON;
    }
	
	private ListResultSet model2result(JSONElement model) {
	
		def result = new ListResultSet();
		result.result.add(id: model.id, name: model.name, ttl: model.ttl, 
			jiraUserName: model.jiraUserName, jiraPassword: model.jiraPassword, 
			jiraUrl: model.jiraUrl, jiraProject: model.jiraProject);
		return result;
	} 
}
