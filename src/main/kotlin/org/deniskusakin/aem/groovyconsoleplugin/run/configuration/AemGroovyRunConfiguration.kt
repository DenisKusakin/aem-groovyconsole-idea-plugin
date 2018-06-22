package org.deniskusakin.aem.groovyconsoleplugin.run.configuration

import com.intellij.execution.Executor
import com.intellij.execution.configurations.*
import com.intellij.execution.process.OSProcessHandler
import com.intellij.execution.process.ProcessHandler
import com.intellij.execution.runners.ExecutionEnvironment
import com.intellij.openapi.options.SettingsEditor
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.JDOMExternalizer
import org.jdom.Element

/**
 * @author Denis_Kusakin. 6/19/2018.
 */
class AemGroovyRunConfiguration(project: Project, factory: ConfigurationFactory, name: String?) : LocatableConfigurationBase(project, factory, name) {
    var serverHost: String? = null
    var login: String? = null
    var password: String? = null
    var scriptPath: String? = null

    override fun getConfigurationEditor(): SettingsEditor<out RunConfiguration> {
        return AemGroovySettingsEditor(project)
    }

    override fun getState(executor: Executor, environment: ExecutionEnvironment): RunProfileState? {
        return GrState(environment, serverHost ?: "http://localhost:4502", login ?: "admin2", password ?: "admin2", scriptPath ?: "")
    }

    override fun readExternal(element: Element) {
        super.readExternal(element)
        scriptPath = JDOMExternalizer.readString(element, "scriptPath")
        serverHost = JDOMExternalizer.readString(element, "server")
        login = JDOMExternalizer.readString(element, "login")
        password = JDOMExternalizer.readString(element, "password")
    }

    override fun writeExternal(element: Element) {
        super.writeExternal(element)
        JDOMExternalizer.write(element, "server", serverHost)
        JDOMExternalizer.write(element, "login", login)
        JDOMExternalizer.write(element, "password", password)
        JDOMExternalizer.write(element, "scriptPath", scriptPath)
    }
}

class GrState(env: ExecutionEnvironment, val serverHost: String, val login: String, val password: String, val scriptPath: String) : CommandLineState(env) {
    override fun startProcess(): ProcessHandler {
        val commandLine = GeneralCommandLine("curl", "-u", "$login:$password", "--data-urlencode", "script@$scriptPath", "-X", "POST", "-H", "Content-Type: application/x-www-form-urlencoded; charset=UTF-8", "$serverHost/bin/groovyconsole/post.json")
                .withParameters("-s")
        val processHandler = OSProcessHandler(commandLine)
        processHandler.setShouldDestroyProcessRecursively(false)
        return processHandler
    }

}