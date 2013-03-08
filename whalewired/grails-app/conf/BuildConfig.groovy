grails.project.class.dir = "target/classes"
grails.project.test.class.dir = "target/test-classes"
grails.project.test.reports.dir = "target/test-reports"
//grails.project.war.file = "target/${appName}-${appVersion}.war"
grails.project.war.file = "target/${appName}.war"


grails.project.dependency.resolution = {
	
    // inherit Grails' default dependencies
    inherits("global") {
        // uncomment to disable ehcache
        // excludes 'ehcache'
    }
    log "warn" // log level of Ivy resolver, either 'error', 'warn', 'info', 'debug' or 'verbose'
    repositories {
        grailsPlugins()
        grailsHome()
        grailsCentral()

        // uncomment the below to enable remote dependency resolution
        // from public Maven repositories
        mavenCentral()
        //mavenLocal()
        mavenRepo "http://oss.sonatype.org/content/repositories/releases/"
        mavenRepo "http://snapshots.repository.codehaus.org"
        mavenRepo "http://repository.codehaus.org"
        mavenRepo "http://download.java.net/maven/2/"
        mavenRepo "http://repository.jboss.com/maven2/"
		mavenRepo "http://repo1.maven.org/maven2/"
		
    }
    dependencies {
		compile 'org.elasticsearch:elasticsearch-lang-groovy:1.3.0'
		test 'org.elasticsearch:elasticsearch-lang-groovy:1.3.0'
		build 'org.elasticsearch:elasticsearch-lang-groovy:1.3.0'
		runtime 'org.elasticsearch:elasticsearch-lang-groovy:1.3.0'
		compile 'com.spatial4j:spatial4j:0.3'
		test 'com.spatial4j:spatial4j:0.3'
		build 'com.spatial4j:spatial4j:0.3'
		runtime 'com.spatial4j:spatial4j:0.3'
    }

    plugins {
        compile ":hibernate:$grailsVersion"
        compile ":resources:1.0"
		
		//For installing Bootstrap Twitter Plugin
		/*runtime ':twitter-bootstrap:2.0.0.16'
		runtime ':fields:1.0.1'*/
		
        build ":tomcat:$grailsVersion"
    }
	
}
