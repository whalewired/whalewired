
quartz {
    autoStartup = false
    jdbcStore = false
    waitForJobsToCompleteOnShutdown = true
}

environments {
    test {
        quartz {
            autoStartup = false
        }
    }
	
	development {
		quartz {
			autoStartup = true
		}
	}
}
