package com.whalewired

class Issue {

    static constraints = {
		mutedDate(nullable:true)
		throwableLocation(nullable:true)
		exceptionType(nullable:true)
    }
	
	String externalIssueId;
	String throwableLocation;
	String logLocation;
	String exceptionType;
	Date mutedDate;
	
	boolean isMuted() {
		return mutedDate != null;
	}
	
}
