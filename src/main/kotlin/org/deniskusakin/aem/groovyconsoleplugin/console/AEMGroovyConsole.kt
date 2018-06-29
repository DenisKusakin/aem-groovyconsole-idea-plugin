package org.deniskusakin.aem.groovyconsoleplugin.console

import com.beust.klaxon.Klaxon
import com.github.kittinunf.fuel.Fuel
import com.github.kittinunf.fuel.core.FuelError
import com.github.kittinunf.fuel.core.Request
import com.github.kittinunf.fuel.core.Response
import com.github.kittinunf.result.Result
import com.google.gson.Gson
import com.intellij.execution.ExecutionException
import com.intellij.execution.ExecutionManager
import com.intellij.execution.configurations.GeneralCommandLine
import com.intellij.execution.executors.DefaultRunExecutor
import com.intellij.execution.impl.ConsoleViewImpl
import com.intellij.execution.process.*
import com.intellij.execution.ui.ConsoleView
import com.intellij.execution.ui.ConsoleViewContentType
import com.intellij.openapi.project.Project
import com.intellij.execution.ui.RunContentDescriptor
import com.intellij.execution.ui.actions.CloseAction
import com.intellij.execution.util.ExecUtil
import com.intellij.openapi.actionSystem.ActionManager
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.DefaultActionGroup
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.components.ServiceManager
import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.module.Module
import com.intellij.openapi.util.Key
import com.intellij.openapi.util.text.StringUtil
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.util.Consumer
import org.deniskusakin.aem.groovyconsoleplugin.services.PersistentStateService
import java.awt.BorderLayout
import java.io.IOException
import java.nio.charset.Charset
import javax.swing.JPanel

/**
 * @author Denis_Kusakin. 6/26/2018.
 */
class AEMGroovyConsole(private val project: Project, private val descriptor: RunContentDescriptor, private val view: ConsoleView, private val serverName: String) {

    companion object {
        private val GROOVY_CONSOLE = Key.create<AEMGroovyConsole>("AEMGroovyConsoleKey")
        val GROOVY_CONSOLE_CURRENT_SERVER = Key.create<String>("AEMGroovyConsoleCurrentServer")
        private const val NETWORK_TIMEOUT = 10 * 60 * 1000

        fun getOrCreateConsole(project: Project,
                               contentFile: VirtualFile) {
            val currentServer = contentFile.getUserData(GROOVY_CONSOLE_CURRENT_SERVER) ?: ""

//            val existingConsole = contentFile.getUserData<AEMGroovyConsole>(GROOVY_CONSOLE)
//            if (existingConsole != null) {
//                existingConsole.execute(String(contentFile.contentsToByteArray()))
//                return
//            }
            val console = createConsole(project, contentFile, currentServer)
            //TODO: Move this from get method
            console!!.execute(String(contentFile.contentsToByteArray()))
        }

        private val defaultExecutor = DefaultRunExecutor.getRunExecutorInstance()
        private val UTF_8 = Charset.forName("UTF-8")
        private val LOGGER = Logger.getInstance(AEMGroovyConsole::class.java)

        private fun createConsole(project: Project,
                                  contentFile: VirtualFile, serverName: String): AEMGroovyConsole? {
            val title = "$serverName:${contentFile.name}"
            val consoleView = ConsoleViewImpl(project, true)
            val descriptor = RunContentDescriptor(consoleView, null, JPanel(BorderLayout()), title)
            val console = AEMGroovyConsole(project, descriptor, consoleView, serverName)

            val consoleViewComponent = consoleView.component

            val ui = descriptor.component
            ui.add(consoleViewComponent, BorderLayout.CENTER)
            contentFile.putUserData<AEMGroovyConsole>(GROOVY_CONSOLE, console)
            ExecutionManager.getInstance(project).contentManager.showRunContent(defaultExecutor, descriptor)
            return console
        }

    }

    fun execute(scriptContent: String) {
        val service = ServiceManager.getService(project, PersistentStateService::class.java)
        val currentServerInfo = service.getAEMServers().find { it.name == serverName }

        val login = currentServerInfo!!.login
        val password = currentServerInfo.password
        val serverHost = currentServerInfo.url

        ExecutionManager.getInstance(project).contentManager.toFrontRunContent(defaultExecutor, descriptor)

        view.print("Running script on $serverName\n\n", ConsoleViewContentType.LOG_WARNING_OUTPUT)

        Fuel.post("$serverHost/bin/groovyconsole/post.json", listOf(Pair("script", scriptContent)))
                .timeout(NETWORK_TIMEOUT)
                .timeoutRead(NETWORK_TIMEOUT)
                .authenticate(login, password)
                .response { request, response, result ->
                    when (result) {
                        is Result.Failure -> {
                            view.print("ERROR: \n", ConsoleViewContentType.ERROR_OUTPUT)
                            view.print(result.getException().localizedMessage, ConsoleViewContentType.ERROR_OUTPUT)
                        }
                        is Result.Success -> {
                            val output = Gson().fromJson<GroovyConsoleOutput>(String(response.data), GroovyConsoleOutput::class.java)
                            if (output.exceptionStackTrace.isBlank()) {
                                view.print(output.output, ConsoleViewContentType.NORMAL_OUTPUT)
                            } else {
                                view.print(output.exceptionStackTrace, ConsoleViewContentType.ERROR_OUTPUT)
                            }
                            view.print("Execution Time:${output.runningTime}", ConsoleViewContentType.LOG_WARNING_OUTPUT)
                        }
                    }
                }
    }

    data class GroovyConsoleOutput(val output: String, val runningTime: String, val exceptionStackTrace: String)
}