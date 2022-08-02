package org.deniskusakin.aem.groovyconsoleplugin.services.http.model

interface GroovyConsoleResponseHandler {
    fun onSuccess(output: GroovyConsoleOutput)

    fun onFail(th: Throwable)
}
