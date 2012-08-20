import static org.elasticsearch.client.Requests.*

import grails.util.Environment


import org.elasticsearch.action.admin.cluster.state.ClusterStateResponse
import org.elasticsearch.action.admin.indices.mapping.put.PutMappingRequest
import org.elasticsearch.client.Client
import org.elasticsearch.client.Requests
import org.elasticsearch.node.Node;

import com.whalewired.Role
import com.whalewired.User
import com.whalewired.UserRole
import com.whalewired.services.ElasticSearchAdminService;
import com.whalewired.services.ElasticSearchService;


class BootStrap {

	ElasticSearchAdminService elasticSearchAdminService;
	
    def init = { servletContext ->
		
		log.info("Whalewired init starting... ");
		createRolesAndUsersInMemory();
		elasticSearchAdminService.getIndices(); // To get it up and running
		log.info("Whalewired init finished... ");
    }
	
    def destroy = {
		elasticSearchAdminService.shutdown();
    }
	
	def createRolesAndUsersInMemory = {
		log.info("Establishing roles and users");
		def adminRole = new Role(authority: 'ROLE_ADMIN').save(flush: true)
		def userRole = new Role(authority: 'ROLE_USER').save(flush: true)
		def testUser = new User(username: 'me', enabled: true, password: 'password')
		testUser.save(flush: true)
		UserRole.create testUser, adminRole, true
		assert User.count() == 1
		assert Role.count() == 2
		assert UserRole.count() == 1
		log.info("Established roles and users");
	}
	
}
