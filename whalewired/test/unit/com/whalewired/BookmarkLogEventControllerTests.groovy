package com.whalewired



import org.junit.*
import grails.test.mixin.*

@TestFor(BookmarkLogEventController)
@Mock(BookmarkLogEvent)
class BookmarkLogEventControllerTests {


    def populateValidParams(params) {
      assert params != null
      // TODO: Populate valid properties like...
      //params["name"] = 'someValidName'
    }

    void testIndex() {
        controller.index()
        assert "/bookmarkLogEvent/list" == response.redirectedUrl
    }

    void testList() {

        def model = controller.list()

        assert model.bookmarkLogEventInstanceList.size() == 0
        assert model.bookmarkLogEventInstanceTotal == 0
    }

    void testCreate() {
       def model = controller.create()

       assert model.bookmarkLogEventInstance != null
    }

    void testSave() {
        controller.save()

        assert model.bookmarkLogEventInstance != null
        assert view == '/bookmarkLogEvent/create'

        response.reset()

        populateValidParams(params)
        controller.save()

        assert response.redirectedUrl == '/bookmarkLogEvent/show/1'
        assert controller.flash.message != null
        assert BookmarkLogEvent.count() == 1
    }

    void testShow() {
        controller.show()

        assert flash.message != null
        assert response.redirectedUrl == '/bookmarkLogEvent/list'


        populateValidParams(params)
        def bookmarkLogEvent = new BookmarkLogEvent(params)

        assert bookmarkLogEvent.save() != null

        params.id = bookmarkLogEvent.id

        def model = controller.show()

        assert model.bookmarkLogEventInstance == bookmarkLogEvent
    }

    void testEdit() {
        controller.edit()

        assert flash.message != null
        assert response.redirectedUrl == '/bookmarkLogEvent/list'


        populateValidParams(params)
        def bookmarkLogEvent = new BookmarkLogEvent(params)

        assert bookmarkLogEvent.save() != null

        params.id = bookmarkLogEvent.id

        def model = controller.edit()

        assert model.bookmarkLogEventInstance == bookmarkLogEvent
    }

    void testUpdate() {
        controller.update()

        assert flash.message != null
        assert response.redirectedUrl == '/bookmarkLogEvent/list'

        response.reset()


        populateValidParams(params)
        def bookmarkLogEvent = new BookmarkLogEvent(params)

        assert bookmarkLogEvent.save() != null

        // test invalid parameters in update
        params.id = bookmarkLogEvent.id
        //TODO: add invalid values to params object

        controller.update()

        assert view == "/bookmarkLogEvent/edit"
        assert model.bookmarkLogEventInstance != null

        bookmarkLogEvent.clearErrors()

        populateValidParams(params)
        controller.update()

        assert response.redirectedUrl == "/bookmarkLogEvent/show/$bookmarkLogEvent.id"
        assert flash.message != null

        //test outdated version number
        response.reset()
        bookmarkLogEvent.clearErrors()

        populateValidParams(params)
        params.id = bookmarkLogEvent.id
        params.version = -1
        controller.update()

        assert view == "/bookmarkLogEvent/edit"
        assert model.bookmarkLogEventInstance != null
        assert model.bookmarkLogEventInstance.errors.getFieldError('version')
        assert flash.message != null
    }

    void testDelete() {
        controller.delete()
        assert flash.message != null
        assert response.redirectedUrl == '/bookmarkLogEvent/list'

        response.reset()

        populateValidParams(params)
        def bookmarkLogEvent = new BookmarkLogEvent(params)

        assert bookmarkLogEvent.save() != null
        assert BookmarkLogEvent.count() == 1

        params.id = bookmarkLogEvent.id

        controller.delete()

        assert BookmarkLogEvent.count() == 0
        assert BookmarkLogEvent.get(bookmarkLogEvent.id) == null
        assert response.redirectedUrl == '/bookmarkLogEvent/list'
    }
}
