package org.deniskusakin.aem.groovyconsoleplugin.run.configuration

import com.intellij.execution.ExecutionException
import com.intellij.execution.Executor
import com.intellij.execution.configurations.*
import com.intellij.execution.process.OSProcessHandler
import com.intellij.execution.process.ProcessHandler
import com.intellij.execution.runners.ExecutionEnvironment
import com.intellij.openapi.components.ServiceManager
import com.intellij.openapi.options.SettingsEditor
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.JDOMExternalizer
import org.deniskusakin.aem.groovyconsoleplugin.services.PersistentStateService
import org.jdom.Element

/**
 * @author Denis_Kusakin. 6/19/2018.
 */
@Deprecated("Run configuration is not used by plugin")
class AemGroovyRunConfiguration(project: Project, factory: ConfigurationFactory, name: String?) : LocatableConfigurationBase<GrState>(project, factory, name) {
    var scriptPath: String? = null
    var serverName: String? = null
        set(value) {
            field = value
            val service = ServiceManager.getService(project, PersistentStateService::class.java)
            val serverInfo = service.getAEMServers().find { it.name == value }
            serverUrl = serverInfo?.url
            login = serverInfo?.login
            password = serverInfo?.password
        }

    private var serverUrl: String? = null
    private var login: String? = null
    private var password: String? = null

    override fun getConfigurationEditor(): SettingsEditor<out RunConfiguration> {
        return AemGroovySettingsEditor(project)
    }

    override fun getState(executor: Executor, environment: ExecutionEnvironment): RunProfileState? {
        if (serverUrl == null) {
            throw ExecutionException("Server Host is not defined")
        }
        if (login == null) {
            throw ExecutionException("Login is not defined")
        }
        if (password == null) {
            throw ExecutionException("Password is not defined")
        }
        if (scriptPath == null) {
            throw ExecutionException("Script is not defined")
        }
        return GrState(environment, serverUrl!!, login!!, password!!, scriptPath!!)
    }

    override fun readExternal(element: Element) {
        super.readExternal(element)
        scriptPath = JDOMExternalizer.readString(element, "scriptPath")
        serverName = JDOMExternalizer.readString(element, "serverName")
    }

    override fun writeExternal(element: Element) {
        super.writeExternal(element)
        JDOMExternalizer.write(element, "serverName", serverName)
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