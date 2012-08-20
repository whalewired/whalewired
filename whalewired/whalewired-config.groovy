// set per-environment serverURL stem for creating absolute links
environments {
    production {
        grails.serverURL = "http://172.17.33.54/whalewired"
		esCluster = "whalewired_cluster"
    }
}

log4j = {
    appenders {
        console name:'stdout', layout:pattern(conversionPattern: '%c{2} %m%n')
    }

    error  'org.codehaus.groovy.grails.web.servlet',  //  controllers
           'org.codehaus.groovy.grails.web.pages', //  GSP
           'org.codehaus.groovy.grails.web.sitemesh', //  layouts
           'org.codehaus.groovy.grails.web.mapping.filter', // URL mapping
           'org.codehaus.groovy.grails.web.mapping', // URL mapping
           'org.codehaus.groovy.grails.commons', // core / classloading
           'org.codehaus.groovy.grails.plugins', // plugins
           'org.codehaus.groovy.grails.orm.hibernate', // hibernate integration
           'org.springframework',
           'org.hibernate',
           'net.sf.ehcache.hibernate'

    warn   'org.mortbay.log'
	
	info	'com.whalewired'
//			'groovyx.net.http.HttpURLClient'
	
	debug	'grails.app.controllers',
			'grails.app.services',
            'grails.app.conf'
}