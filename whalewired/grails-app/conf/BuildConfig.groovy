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
        //mavenCentral()
        //mavenLocal()
        //mavenRepo "http://snapshots.repository.codehaus.org"
        //mavenRepo "http://repository.codehaus.org"
        //mavenRepo "http://download.java.net/maven/2/"
        //mavenRepo "http://repository.jboss.com/maven2/"
		mavenRepo "http://repo1.maven.org/maven2/"
        mavenRepo "http://oss.sonatype.org/content/repositories/releases/"
    }
    dependencies {
        // specify dependencies here under either 'build', 'compile', 'runtime', 'test' or 'provided' scopes eg.
        // runtime 'mysql:mysql-connector-java:5.1.5'
		compile 'org.elasticsearch:elasticsearch-lang-groovy:0.18.7'
		test 'org.elasticsearch:elasticsearch-lang-groovy:0.18.7'
		build 'org.elasticsearch:elasticsearch-lang-groovy:0.18.7'
		runtime 'org.elasticsearch:elasticsearch-lang-groovy:0.18.7'
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
