dataSource {
	pooled = true
	driverClassName = "org.h2.Driver"
	username = "sa"
	password = ""
}

hibernate {
	cache.use_second_level_cache = "true"
	cache.use_query_cache = "true"
	cache.provider_class = 'org.hibernate.cache.EhCacheProvider'
}


// environment specific settings
environments {
	development {
		dataSource {
			dbCreate = "update" // one of 'create', 'create-drop','update'
			// url = "jdbc:h2:mem:devDb"
			// dbCreate = "create-drop"
			url = "jdbc:h2:data/h2/wwdb"
		}
	}
	test {
		dataSource {
			dbCreate = "update"
			url = "jdbc:h2:mem:testDb"
		}
	}
	production {
		dataSource {
			dbCreate = "update"
			url = "jdbc:h2:data/h2/wwdb"
		}
	}
}
