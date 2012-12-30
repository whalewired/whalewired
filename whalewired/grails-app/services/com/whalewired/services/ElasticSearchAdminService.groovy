package com.whalewired.services

import static org.elasticsearch.node.NodeBuilder.*
import grails.plugins.springsecurity.Secured
import groovy.json.JsonBuilder

import org.codehaus.groovy.grails.web.context.ServletContextHolder
import org.elasticsearch.action.admin.cluster.state.ClusterStateResponse
import org.elasticsearch.action.admin.indices.close.CloseIndexRequest
import org.elasticsearch.action.admin.indices.create.CreateIndexRequest
import org.elasticsearch.action.admin.indices.delete.DeleteIndexRequest
import org.elasticsearch.action.admin.indices.mapping.put.PutMappingRequest
import org.elasticsearch.action.admin.indices.open.OpenIndexRequest
import org.elasticsearch.action.admin.indices.settings.UpdateSettingsRequest
import org.elasticsearch.client.Client
import org.elasticsearch.client.Requests
import org.elasticsearch.common.settings.ImmutableSettings
import org.elasticsearch.common.settings.Settings
import org.elasticsearch.index.query.RangeQueryBuilder
import org.elasticsearch.node.Node


class ElasticSearchAdminService {
	
	def grailsApplication;
	
	static final ES_TYPE = "logevent";
	static final ES_TYPE_MAPPING;
	
	static {
	
		def builder = new JsonBuilder()
		builder.logevent() {
			properties {
				_id {
						type 'string'
						index 'not_analyzed'
						store 'yes'
				}
				logLevel {
						type 'string'
						index 'not_analyzed'
				}
				logTime {
						type 'date'
				}
				logMessage {
						type 'multi_field'
						fields {
							logMessage {
								type 'string'
							}
							untouched {
								type 'string'
								index 'not_analyzed'
							}
						}
				}
			}
		}
		ES_TYPE_MAPPING = builder.toString();
	}
	
	def getEsClient() {
		Client esClient = ServletContextHolder.getServletContext().getAttribute("whalewired.es.client");
		if (esClient == null) {
			Node node = nodeBuilder().clusterName(grailsApplication.config.esCluster).client(true).node();
			esClient = node.client();
			ServletContextHolder.getServletContext().setAttribute("whalewired.es.node", node);
			ServletContextHolder.getServletContext().setAttribute("whalewired.es.client", esClient);
			log.debug("Established node and client for: "+grailsApplication.config.esCluster);
		}
		return esClient;
	}
	
	def shutdown() {
		ServletContextHolder.getServletContext().getAttribute("whalewired.es.node").close();
	}

	def getIndices() {
		
		getEsClient().admin().cluster().prepareHealth().setWaitForYellowStatus().execute().actionGet();
		ClusterStateResponse stateResponse = getEsClient().admin().cluster().prepareState().execute().actionGet();
		return stateResponse.getState().getMetaData().indices();
	}

	def getDefaultIndex() {
		
		println("getDefaultIndex")
		// TODO make this configurable
		return getIndices()?.keySet().asList().get(0);
	}

			
	@Secured(['ROLE_ADMIN'])
	def createIndex(indexName, ttl) {
		
		indexName = indexName.toLowerCase();
		Settings settings = ImmutableSettings.settingsBuilder().put("index.ttl", ttl).build();
		getEsClient().admin().indices().create(new CreateIndexRequest(indexName, settings)).actionGet();
		PutMappingRequest putMappingRequest = Requests.putMappingRequest(indexName).type(ES_TYPE).source(ES_TYPE_MAPPING);
		getEsClient().admin().indices().putMapping(putMappingRequest).actionGet();
		
		log.info("Created index "+indexName);
			
	}

	@Secured(['ROLE_ADMIN'])
	def updateIndex(indexName, ttl) {
		
		indexName = indexName.toLowerCase();
		getEsClient().admin().indices().close(new CloseIndexRequest(indexName)).actionGet();
		
		Settings settings = ImmutableSettings.settingsBuilder().put("index.ttl", ttl).build();
		getEsClient().admin().indices().updateSettings(new UpdateSettingsRequest(settings)).actionGet();

		getEsClient().admin().indices().open(new OpenIndexRequest(indexName)).actionGet();
		log.info("Updated settings for index "+indexName);
	}

	@Secured(['ROLE_ADMIN'])
	def deleteIndex(indexName) {
		
		indexName = indexName.toLowerCase();
		getEsClient().admin().indices().delete(new DeleteIndexRequest(indexName)).actionGet();
		log.info("Deleted index "+indexName);
	}
	
   def deleteLogs(indexName, daysBackwards) {
	   
	   def cal = Calendar.getInstance();
	   cal.add(Calendar.DAY_OF_YEAR, -daysBackwards);

	   def deleteResponse = getEsClient().prepareDeleteByQuery(indexName)
			   .setTypes(ES_TYPE)
			   .setQuery(new RangeQueryBuilder("logTime")
			   .lt(cal.getTime().getTime()))
			   .execute()
			   .actionGet();

	   log.info "Deleted in " +indexName +" from " + deleteResponse.index(indexName).getSuccessfulShards() + " shards.";
   }

}
