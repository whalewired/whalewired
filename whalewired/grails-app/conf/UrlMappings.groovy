class UrlMappings {

	static mappings = {
		"/$controller/$action?/$id?"{
			constraints {
				// apply constraints here
			}
		}

		"/"(controller: "logEvent", view:"list")
		"500"(view:'/error')
	}
}
