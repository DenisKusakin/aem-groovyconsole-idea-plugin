package org.deniskusakin.aem.groovyconsoleplugin.console

import com.github.kittinunf.fuel.Fuel
import com.github.kittinunf.result.Result
import com.google.gson.Gson
import com.intellij.execution.executors.DefaultRunExecutor
import com.intellij.execution.filters.RegexpFilter
import com.intellij.execution.filters.RegexpFilter.FILE_PATH_MACROS
import com.intellij.execution.filters.RegexpFilter.LINE_MACROS
import com.intellij.execution.filters.TextConsoleBuilderFactory
import com.intellij.execution.ui.ConsoleView
import com.intellij.execution.ui.ConsoleViewContentType
import com.intellij.execution.ui.RunContentDescriptor
import com.intellij.execution.ui.RunContentManager
import com.intellij.execution.ui.actions.CloseAction
import com.intellij.icons.AllIcons
import com.intellij.openapi.actionSystem.ActionManager
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.DefaultActionGroup
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.Key
import com.intellij.openapi.vfs.VirtualFile
import org.deniskusakin.aem.groovyconsoleplugin.services.PersistentStateService
import java.awt.BorderLayout
import javax.swing.JComponent
import javax.swing.JPanel

/**
 * @author Denis_Kusakin. 6/26/2018.
 */
class AEMGroovyConsole(val project: Project, val descriptor: RunContentDescriptor, private val view: ConsoleView, private val serverName: String, private val filePath: String) {

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
            val console = createConsole(project, contentFile, currentServer)
            contentFile.addConsole(serverName = currentServer, console = console!!)
            return console
        }

        private val defaultExecutor = DefaultRunExecutor.getRunExecutorInstance()

        private fun createConsole(project: Project,
                                  contentFile: VirtualFile, serverName: String): AEMGroovyConsole? {
            val title = "$serverName:${contentFile.name}"
            val consoleView = TextConsoleBuilderFactory.getInstance().createBuilder(project).console
            val descriptor = object : RunContentDescriptor(consoleView, null, JPanel(BorderLayout()) as JComponent, title) {
                //TODO: Why does this change resolved the problem? How does it work in default Groovy Console?
                override fun isContentReuseProhibited(): Boolean {
                    return true
                }
            }

            //This filter relies on replacement which is made in case of exception
            consoleView.addMessageFilter(RegexpFilter(project, "at Script1.run($FILE_PATH_MACROS:$LINE_MACROS).*"))

            val console = AEMGroovyConsole(project, descriptor, consoleView, serverName, contentFile.presentableUrl)
            descriptor.executionId = title.hashCode().toLong()
            val consoleViewComponent = consoleView.component

            val actionGroup = DefaultActionGroup()
            val restartAction = object : AnAction("Restart the script", "Run the script again", AllIcons.Actions.Restart) {
                override fun actionPerformed(e: AnActionEvent) {
                    console.execute(String(contentFile.contentsToByteArray()))
                }

            }
            actionGroup.add(restartAction)
            actionGroup.addSeparator()
            actionGroup.addAll(*consoleView.createConsoleActions())
            actionGroup.add(CloseAction(defaultExecutor, descriptor, project))
            val toolbar = ActionManager.getInstance().createActionToolbar("AEMGroovyConsole", actionGroup, false)
            toolbar.setTargetComponent(consoleViewComponent)

            val ui = descriptor.component
            ui.add(consoleViewComponent, BorderLayout.CENTER)
            ui.add(toolbar.component, BorderLayout.WEST)
            RunContentManager.getInstance(project).showRunContent(defaultExecutor, descriptor)
            return console
        }

        private fun VirtualFile.getConsole(serverName: String): AEMGroovyConsole? {
            val console = getUserData(GROOVY_CONSOLE)?.get(serverName) ?: return null
            if (RunContentManager.getInstance(console.project).allDescriptors.contains(console.descriptor)) {
                return console
            }
            //TODO: In default Groovy Console implementation this somehow works without such hack
            getUserData(GROOVY_CONSOLE)!!.cleanUpServer(serverName)
            return null
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
        val service = project.getService(PersistentStateService::class.java)
        val currentServerInfo = service.getAEMServers().find { it.name == serverName }
        val login = currentServerInfo!!.login
        val password = currentServerInfo.password
        val serverHost = currentServerInfo.url
        view.clear()
        view.print("\nRunning script on $serverName\n\n", ConsoleViewContentType.LOG_WARNING_OUTPUT)
        RunContentManager.getInstance(project).toFrontRunContent(defaultExecutor, descriptor)

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
                                //This looks a little bit weird, but it works
                                view.scrollTo(0)
                                //TODO: This code relies of fact that AEM Groovy Console uses Script1.groovy as file name, so this code is highly dangerous
                                //In some obvious cases it could work incorrectly, but it provide user with better experience
                                view.print(output.exceptionStackTrace.replace("Script1.groovy", filePath), ConsoleViewContentType.ERROR_OUTPUT)
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

    fun cleanUpServer(serverName: String) {
        map.remove(serverName)
    }
}