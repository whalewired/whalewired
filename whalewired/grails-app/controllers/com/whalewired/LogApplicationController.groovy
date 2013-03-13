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
			result.result.add(id: index.key, name: index.key, ttl: index.value.getSettings().get("index.ttl"));
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
		render model2result(model) as JSON;
    }

    @Secured(['ROLE_ADMIN'])
	def delete = {
		println "DELETE:" + params
		def model = JSON.parse(params.model)[0]
		elasticSearchAdminService.deleteIndex(model.name);
		render model2result(model) as JSON;
    }
	
	private ListResultSet model2result(JSONElement model) {
	
		def result = new ListResultSet();
		result.result.add(id: model.id, name: model.name, ttl: model.ttl);
		return result;
	} 
}
