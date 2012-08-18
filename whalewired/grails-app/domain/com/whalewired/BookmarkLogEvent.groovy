package com.whalewired

class BookmarkLogEvent {

    static constraints = {
		indexName(nullable:true)
		throwableLocation(nullable:true)
		logLocation(nullable:true)
		exceptionType(nullable:true)
		description(nullable:true)
		dateCreated(nullable:true)
    }
	
	String indexName;
	String throwableLocation;
	String logLocation;
	String exceptionType;		
	String description;
	Date dateCreated
	
}
