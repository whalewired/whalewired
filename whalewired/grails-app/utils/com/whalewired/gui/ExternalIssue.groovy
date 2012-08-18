package com.whalewired.gui

class ExternalIssue {
	public enum Priority {
		TRIVIAL('Trivial'), MINOR('Minor'), MAJOR('Major'), CRITICAL('Critical'), BLOCKER('Blocker')
		
		String name
		
		private Priority(String name) {
			this.name = name
		}
	}
	
	public enum Type {
		BUG('Bug'), IMPROVEMENT('Improvement'), TASK('Task')
		
		String name
		
		private Type(String name) {
			this.name = name
		}
	}
	
	public enum Environment {
		DEVELOPMENT('Development'), INTERNAL_HOTFIX('Internal Hotfix'), 
		INTERNAL_STAGING_OR_INTEGRATION_TEST('Internal Staging/Integration Test'), 
		EXTERNAL_TEST('External Test'), EXTERNAL_STAGING_OR_PREPROD('External Staging/Preproduction'),
		PRODUCTION('Production')
		
		String name
		
		private Environment(String name) {
			this.name = name
		}
	}
	
	String issueKey;
	Priority priority
	Type type
	Environment environment
	String summary
	String description
}
