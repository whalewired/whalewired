package com.whalewired.jobs

import org.quartz.CronTrigger;

import com.whalewired.services.*

class DeleteOldLogsJob {
	def concurrent = false
	static triggers = {
		cron name: 'deleteOldLogs', cronExpression: '0 0 12 * * ?'   
		//cron name: 'deleteOldLogs', cronExpression: '0 * * * * ?'
	}
		
	def backwardDays = 30		
	def elasticSearchAdminService;

    def execute() {		
		
		def indices = elasticSearchAdminService.getIndices();
		for (index in indices) {
			
			//println("[${index.key}] index.ttl: " + index.value.getSettings().get("index.ttl"))
			
			
			
			def ttl = index.value.getSettings().get("index.ttl")
			if (ttl) {
				elasticSearchAdminService.deleteLogs(index.key, ttl);
			} else {
			   //println  "[${index.key}] has no TTL definition"
			}
		}
	}
			
}
