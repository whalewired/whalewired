package com.whalewired



import org.junit.*

import com.whalewired.LogAccount;
import com.whalewired.LogAccountController;

import grails.test.mixin.*


@TestFor(LogAccountController)
@Mock(LogAccount)
class LogAccountControllerTests {


    @Test
    void testIndex() {
        controller.index()
        assert "/logAccount/list" == response.redirectedUrl
    }

    @Test
    void testList() {

        def model = controller.list()

        assert model.logAccountInstanceList.size() == 0
        assert model.logAccountInstanceTotal == 0

    }

    @Test
    void testCreate() {
       def model = controller.create()

       assert model.logAccountInstance != null


    }

    @Test
    void testSave() {
        controller.save()

        assert model.logAccountInstance != null
        assert view == '/logAccount/create'

        // TODO: Populate valid properties

        controller.save()

        assert response.redirectedUrl == '/logAccount/show/1'
        assert controller.flash.message != null
        assert LogAccount.count() == 1
    }


    @Test
    void testShow() {
        controller.show()

        assert flash.message != null
        assert response.redirectedUrl == '/logAccount/list'


        def logAccount = new LogAccount()

        // TODO: populate domain properties

        assert logAccount.save() != null

        params.id = logAccount.id

        def model = controller.show()

        assert model.logAccountInstance == logAccount
    }

    @Test
    void testEdit() {
        controller.edit()

        assert flash.message != null
        assert response.redirectedUrl == '/logAccount/list'


        def logAccount = new LogAccount()

        // TODO: populate valid domain properties

        assert logAccount.save() != null

        params.id = logAccount.id

        def model = controller.edit()

        assert model.logAccountInstance == logAccount
    }

    @Test
    void testUpdate() {

        controller.update()

        assert flash.message != null
        assert response.redirectedUrl == '/logAccount/list'

        response.reset()


        def logAccount = new LogAccount()

        // TODO: populate valid domain properties

        assert logAccount.save() != null

        // test invalid parameters in update
        params.id = logAccount.id

        controller.update()

        assert view == "/logAccount/edit"
        assert model.logAccountInstance != null

        logAccount.clearErrors()

        // TODO: populate valid domain form parameter
        controller.update()

        assert response.redirectedUrl == "/logAccount/show/$logAccount.id"
        assert flash.message != null
    }

    @Test
    void testDelete() {
        controller.delete()

        assert flash.message != null
        assert response.redirectedUrl == '/logAccount/list'

        response.reset()

        def logAccount = new LogAccount()

        // TODO: populate valid domain properties
        assert logAccount.save() != null
        assert LogAccount.count() == 1

        params.id = logAccount.id

        controller.delete()

        assert LogAccount.count() == 0
        assert LogAccount.get(logAccount.id) == null
        assert response.redirectedUrl == '/logAccount/list'


    }


}