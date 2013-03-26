package com.whalewired

class LogApp {

    static constraints = {
		appName(nullable:false, unique: true)
		jiraUserName(nullable:true)
		jiraPassword(nullable:true)
		jiraUrl(nullable:true)
		jiraProject(nullable:true)
    }
	
	String appName;
	String jiraUserName;
	String jiraPassword;
	String jiraUrl;		
	String jiraProject;		
}
