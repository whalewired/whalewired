package com.whalewired.jobs

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
			elasticSearchAdminService.deleteLogs(index.key, index.value.getSettings().get("index.ttl").toInteger());
		}
	}
			
}
