package org.deniskusakin.aem.groovyconsoleplugin.console

import com.github.kittinunf.fuel.Fuel
import com.github.kittinunf.result.Result
import com.google.gson.Gson
import com.intellij.execution.ExecutionManager
import com.intellij.execution.executors.DefaultRunExecutor
import com.intellij.execution.impl.ConsoleViewImpl
import com.intellij.execution.ui.ConsoleView
import com.intellij.execution.ui.ConsoleViewContentType
import com.intellij.execution.ui.RunContentDescriptor
import com.intellij.openapi.components.ServiceManager
import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.Key
import com.intellij.openapi.vfs.VirtualFile
import org.deniskusakin.aem.groovyconsoleplugin.services.PersistentStateService
import java.awt.BorderLayout
import java.nio.charset.Charset
import javax.swing.JPanel

/**
 * @author Denis_Kusakin. 6/26/2018.
 */
class AEMGroovyConsole(private val project: Project, private val descriptor: RunContentDescriptor, private val view: ConsoleView, private val serverName: String) {

    companion object {
        private val GROOVY_CONSOLE = Key.create<AEMGroovyConsoles>("AEMGroovyConsoleKey")
        val GROOVY_CONSOLE_CURRENT_SERVER = Key.create<String>("AEMGroovyConsoleCurrentServer")
        private const val NETWORK_TIMEOUT = 10 * 60 * 1000

        fun getOrCreateConsole(project: Project,
                               contentFile: VirtualFile): AEMGroovyConsole {
            val currentServer = contentFile.getUserData(GROOVY_CONSOLE_CURRENT_SERVER) ?: ""
            val existingConsole = contentFile.getConsole(serverName = currentServer)
            if (existingConsole != null) {
                return existingConsole
            }
//            val existingConsole = contentFile.getUserData<AEMGroovyConsole>(GROOVY_CONSOLE)
//            if (existingConsole != null) {
//                existingConsole.execute(String(contentFile.contentsToByteArray()))
//                return
//            }
            val console = createConsole(project, contentFile, currentServer)
            contentFile.addConsole(serverName = currentServer, console = console!!)
            return console
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
            descriptor.executionId = title.hashCode().toLong()
            val consoleViewComponent = consoleView.component

            val ui = descriptor.component
            ui.add(consoleViewComponent, BorderLayout.CENTER)
            //contentFile.putUserData<AEMGroovyConsole>(GROOVY_CONSOLE, console)
            ExecutionManager.getInstance(project).contentManager.showRunContent(defaultExecutor, descriptor)
            return console
        }

        private fun VirtualFile.getConsole(serverName: String): AEMGroovyConsole? {
            return getUserData(GROOVY_CONSOLE)?.get(serverName)
        }

        private fun VirtualFile.addConsole(serverName: String, console: AEMGroovyConsole) {
            val consoles = this.getUserData(GROOVY_CONSOLE)
            if (consoles == null) {
                this.putUserData(GROOVY_CONSOLE, AEMGroovyConsoles(serverName, console))
            } else {
                consoles.put(serverName, console)
            }
        }

    }

    fun execute(scriptContent: String) {
        val service = ServiceManager.getService(project, PersistentStateService::class.java)
        val currentServerInfo = service.getAEMServers().find { it.name == serverName }

        val login = currentServerInfo!!.login
        val password = currentServerInfo.password
        val serverHost = currentServerInfo.url

        ExecutionManager.getInstance(project).contentManager.toFrontRunContent(defaultExecutor, descriptor)

        view.print("\nRunning script on $serverName\n\n", ConsoleViewContentType.LOG_WARNING_OUTPUT)

        Fuel.post("$serverHost/bin/groovyconsole/post.json", listOf(Pair("script", scriptContent)))
                .timeout(NETWORK_TIMEOUT)
                .timeoutRead(NETWORK_TIMEOUT)
                .authenticate(login, password)
                .response { _, response, result ->
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

class AEMGroovyConsoles(serverName: String, console: AEMGroovyConsole) {
    private val map = mutableMapOf(Pair(serverName, console))

    fun put(serverName: String, console: AEMGroovyConsole) {
        map += Pair(serverName, console)
    }

    fun get(serverName: String): AEMGroovyConsole? {
        return map.getOrDefault(serverName, null)
    }
}