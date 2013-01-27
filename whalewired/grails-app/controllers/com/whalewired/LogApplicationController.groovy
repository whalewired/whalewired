package com.whalewired

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
			result.result.add(name: index.key, ttl: index.value.getSettings().get("index.ttl"));
		}
		result.total = result.result.size();
		
		//println("LogApplicationController.listJSON: "+(result as JSON));
		render result as JSON;
	}


    @Secured(['ROLE_ADMIN'])
	def create = {
		println "create:" + params
		try {
			elasticSearchAdminService.createIndex(params.name, params.ttl);
		} catch (Exception e) {
			println(e);
			render(status: 422, text: 'BALAHHA')
		}
		render(view:"list", params: params);
    }

    
	@Secured(['ROLE_ADMIN'])
	def update = {
		println "update:" + params
		elasticSearchAdminService.updateIndex(params.name, params.ttl.toInteger());
		render(view:"list", params: params);
    }

    @Secured(['ROLE_ADMIN'])
	def delete = {
		println "DELETE:" + params
		elasticSearchAdminService.deleteIndex(params.name);
		render(view:"list", params: params);
    }
}
