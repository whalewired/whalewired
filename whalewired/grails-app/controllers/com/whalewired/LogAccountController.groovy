package com.whalewired

import com.whalewired.LogAccount;

import grails.converters.JSON

class LogAccountController {

	static scaffold = true;

    static allowedMethods = [save: "POST", update: "POST", delete: "POST"]

    def index = {
        redirect(action: "list", params: params)
    }

	def list = {
		params.max = Math.min(params.max ? params.int('max') : 10, 100)
		[logAccountInstanceList: LogAccount.list(params), logAccountInstanceTotal: LogAccount.count()]
	}

    def create = {
        def logAccountInstance = new LogAccount()
        logAccountInstance.properties = params
        return [logAccountInstance: logAccountInstance]
    }

    def save = {
        def logAccountInstance = new LogAccount(params)
        if (logAccountInstance.save(flush: true)) {
            flash.message = message(code: 'default.created.message', args: [message(code: 'logAccount.label', default: 'LogAccount'), logAccountInstance.id])
            redirect(action: "show", id: logAccountInstance.id)
        }
        else {
            render(view: "create", model: [logAccountInstance: logAccountInstance])
        }
    }

    def show = {
        def logAccountInstance = LogAccount.get(params.id)
        if (!logAccountInstance) {
            flash.message = message(code: 'default.not.found.message', args: [message(code: 'logAccount.label', default: 'LogAccount'), params.id])
            redirect(action: "list")
        }
        else {
            [logAccountInstance: logAccountInstance]
        }
    }

    def edit = {
        def logAccountInstance = LogAccount.get(params.id)
        if (!logAccountInstance) {
            flash.message = message(code: 'default.not.found.message', args: [message(code: 'logAccount.label', default: 'LogAccount'), params.id])
            redirect(action: "list")
        }
        else {
            return [logAccountInstance: logAccountInstance]
        }
    }

    def update = {
        def logAccountInstance = LogAccount.get(params.id)
        if (logAccountInstance) {
            if (params.version) {
                def version = params.version.toLong()
                if (logAccountInstance.version > version) {
                    
                    logAccountInstance.errors.rejectValue("version", "default.optimistic.locking.failure", [message(code: 'logAccount.label', default: 'LogAccount')] as Object[], "Another user has updated this LogAccount while you were editing")
                    render(view: "edit", model: [logAccountInstance: logAccountInstance])
                    return
                }
            }
            logAccountInstance.properties = params
            if (!logAccountInstance.hasErrors() && logAccountInstance.save(flush: true)) {
                flash.message = message(code: 'default.updated.message', args: [message(code: 'logAccount.label', default: 'LogAccount'), logAccountInstance.id])
                redirect(action: "show", id: logAccountInstance.id)
            }
            else {
                render(view: "edit", model: [logAccountInstance: logAccountInstance])
            }
        }
        else {
            flash.message = message(code: 'default.not.found.message', args: [message(code: 'logAccount.label', default: 'LogAccount'), params.id])
            redirect(action: "list")
        }
    }

    def delete = {
        def logAccountInstance = LogAccount.get(params.id)
        if (logAccountInstance) {
            try {
                logAccountInstance.delete(flush: true)
                flash.message = message(code: 'default.deleted.message', args: [message(code: 'logAccount.label', default: 'LogAccount'), params.id])
                redirect(action: "list")
            }
            catch (org.springframework.dao.DataIntegrityViolationException e) {
                flash.message = message(code: 'default.not.deleted.message', args: [message(code: 'logAccount.label', default: 'LogAccount'), params.id])
                redirect(action: "show", id: params.id)
            }
        }
        else {
            flash.message = message(code: 'default.not.found.message', args: [message(code: 'logAccount.label', default: 'LogAccount'), params.id])
            redirect(action: "list")
        }
    }
}
