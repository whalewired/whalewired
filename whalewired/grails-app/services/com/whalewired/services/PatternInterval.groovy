package com.whalewired.services

class PatternInterval {
	
	public static enum Interval {
		LAST_1_HOUR,
		LAST_3_HOURS,
		LAST_6_HOURS,
		LAST_12_HOURS,
		LAST_1_DAY,
		LAST_2_DAYS,
		LAST_3_DAYS,
		LAST_5_DAYS		
	}
	
	Interval value = Interval.LAST_1_HOUR
	def indexName
	def logThrowableLocation
	def logLocation
	def logThrowableType
	def factor
	def numberOfSamples
	def private defaultSampleSize = 10
			
	def is1HourInterval() {
		value.equals(Interval.LAST_1_HOUR)
	}  
	
	def is3HoursInterval() {
		value.equals(Interval.LAST_3_HOURS)
	}
	
	def is6HoursInterval() {
		value.equals(Interval.LAST_6_HOURS)
	}
	
	def is12HoursInterval() {
		value.equals(Interval.LAST_12_HOURS)
	}
	
	def is1DayInterval() {
		value.equals(Interval.LAST_1_DAY)
	}
	
	def is2DaysInterval() {
		value.equals(Interval.LAST_2_DAYS)
	}
	
	def is3DaysInterval() {
		value.equals(Interval.LAST_3_DAYS)
	}
	
	def is5DaysInterval() {
		value.equals(Interval.LAST_5_DAYS)
	}
	
	def getFactor() {
		def factor
		if (is1HourInterval()) factor = 1
		else if (is3HoursInterval()) factor = 3
		else if (is6HoursInterval()) factor = 6
		else if (is12HoursInterval()) factor = 12
		else if (is1DayInterval()) factor = 24
		else if (is2DaysInterval()) factor = 2*24
		else if (is3DaysInterval()) factor = 3*24
		else if (is5DaysInterval()) factor = 5*24
		factor
	}	
	
	def getFromToCalendarList() {
		def list = []
		
		//If sampleSize is null then set as default to 10.
		if(numberOfSamples == null){	
			numberOfSamples = defaultSampleSize 
		}
		
		
		/*Steps are generated such that the number of samples is numberOfSamples
		
		SampleIntervalSize is the time interval size or each sample
		 Example with sampleSize is 10 and total interval of 1 hour:
		 SampleIntervalSize = 60 min / 10 = 6 min
		
		timeSinceFirstSample is the time since the oldest sample interval ends*/
		
		def factor = getFactor()
		def sampleIntervalSize = (int) Math.floor((60*factor)/numberOfSamples)
		def timeSinceFirstSample = (int) (60*factor-sampleIntervalSize)
		
		(timeSinceFirstSample..0).step(sampleIntervalSize ) {i ->
			// from
			def from = Calendar.instance
			from.add(Calendar.MINUTE, -(i+sampleIntervalSize) )
			from[Calendar.SECOND] = 0
			from[Calendar.MILLISECOND] = 0
			// to
			def to = Calendar.instance
			to.add(Calendar.MINUTE, -i)
			to[Calendar.SECOND] = 0
			to[Calendar.MILLISECOND] = 0
			
			list << [from, to]
		}
		list
	}


}
