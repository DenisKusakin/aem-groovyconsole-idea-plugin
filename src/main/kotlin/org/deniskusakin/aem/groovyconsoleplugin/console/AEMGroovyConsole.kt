package org.deniskusakin.aem.groovyconsoleplugin.console

import com.github.kittinunf.fuel.Fuel
import com.github.kittinunf.fuel.core.extensions.authentication
import com.github.kittinunf.result.Result
import com.google.gson.Gson
import com.intellij.execution.executors.DefaultRunExecutor
import com.intellij.execution.filters.RegexpFilter
import com.intellij.execution.filters.RegexpFilter.FILE_PATH_MACROS
import com.intellij.execution.filters.RegexpFilter.LINE_MACROS
import com.intellij.execution.filters.TextConsoleBuilderFactory
import com.intellij.execution.ui.*
import com.intellij.execution.ui.actions.CloseAction
import com.intellij.icons.AllIcons
import com.intellij.openapi.actionSystem.ActionManager
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.DefaultActionGroup
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.Key
import com.intellij.openapi.vfs.VirtualFile
import org.deniskusakin.aem.groovyconsoleplugin.console.GroovyConsoleUserData.getCurrentAemConfig
import org.deniskusakin.aem.groovyconsoleplugin.services.PasswordsService
import org.deniskusakin.aem.groovyconsoleplugin.services.PersistentStateService
import org.deniskusakin.aem.groovyconsoleplugin.services.model.AemServerConfig
import java.awt.BorderLayout
import java.util.concurrent.TimeUnit
import javax.swing.JPanel

/**
 * @author Denis_Kusakin. 6/26/2018.
 */
class AEMGroovyConsole(
    private val project: Project,
    private val descriptor: RunContentDescriptor,
    private val view: ConsoleView,
    private val contentFile: VirtualFile,
    private val serverId: Long
) {

    companion object {
        private val GROOVY_CONSOLES = Key.create<MutableMap<Long, AEMGroovyConsole>>("AEMGroovyConsoles")

        private val NETWORK_TIMEOUT = TimeUnit.MINUTES.toMillis(10).toInt()

        fun getOrCreateConsole(
            project: Project,
            contentFile: VirtualFile,
            callback: (console: AEMGroovyConsole) -> Unit
        ) {
            val currentServerConfig = contentFile.getCurrentAemConfig(project) ?: return

            val existingConsole = contentFile.getConsole(currentServerConfig.id)

            if (existingConsole != null) {
                callback(existingConsole)
            } else {
                val console = createConsole(project, contentFile, currentServerConfig)

                contentFile.addConsole(currentServerConfig.id, console)

                callback(console)
            }
        }

        private val defaultExecutor = DefaultRunExecutor.getRunExecutorInstance()

        private fun createConsole(
            project: Project,
            contentFile: VirtualFile,
            server: AemServerConfig
        ): AEMGroovyConsole {

            val consoleView = TextConsoleBuilderFactory.getInstance()
                .createBuilder(project)
                .also {
                    it.addFilter(RegexpFilter(project, "at Script1.run($FILE_PATH_MACROS:$LINE_MACROS).*"))
                }.console

            val descriptor = object : RunContentDescriptor(
                consoleView,
                null,
                JPanel(BorderLayout()),
                "${server.name}: ${contentFile.name}"
            ) {
                override fun dispose() {
                    contentFile.removeConsole(server.id)
                    super.dispose()
                }
            }.also {
                it.executionId = server.id
                it.reusePolicy = object : RunContentDescriptorReusePolicy() {
                    override fun canBeReusedBy(newDescriptor: RunContentDescriptor): Boolean =
                        it.executionId == newDescriptor.executionId
                }
            }

            val console = AEMGroovyConsole(project, descriptor, consoleView, contentFile, server.id)

            descriptor.component.also { ui ->
                val consoleViewComponent = consoleView.component

                val actionGroup = DefaultActionGroup().also { ag ->
                    ag.add(object : AnAction("Restart The Script", "Run the script again", AllIcons.Actions.Restart) {
                        override fun actionPerformed(e: AnActionEvent) {
                            console.execute()
                        }
                    })
                    ag.addSeparator()
                    ag.addAll(*consoleView.createConsoleActions())
                    ag.add(CloseAction(defaultExecutor, descriptor, project))
                }

                val toolbar = ActionManager.getInstance()
                    .createActionToolbar("AEMGroovyConsole", actionGroup, false)
                    .also { tb ->
                        tb.setTargetComponent(consoleViewComponent)
                    }

                ui.add(consoleViewComponent, BorderLayout.CENTER)
                ui.add(toolbar.component, BorderLayout.WEST)
            }

            RunContentManager.getInstance(project).showRunContent(defaultExecutor, descriptor)

            return console
        }

        private fun VirtualFile.getConsole(serverId: Long): AEMGroovyConsole? {
            return getUserData(GROOVY_CONSOLES)?.get(serverId)
        }

        private fun VirtualFile.addConsole(serverId: Long, console: AEMGroovyConsole) {
            putUserDataIfAbsent(GROOVY_CONSOLES, mutableMapOf())[serverId] = console
        }

        private fun VirtualFile.removeConsole(serverId: Long): AEMGroovyConsole? {
            return putUserDataIfAbsent(GROOVY_CONSOLES, mutableMapOf()).remove(serverId)
        }
    }

    fun execute() {
        val aemConfig = getCurrentConsoleAemConfig()

        view.clear()

        if (!isValidAemConfig(aemConfig)) return

        view.print("\nRunning script on ${aemConfig!!.name}\n\n", ConsoleViewContentType.NORMAL_OUTPUT)

        RunContentManager.getInstance(project).toFrontRunContent(defaultExecutor, descriptor)

        doExecute(aemConfig)
    }

    private fun doExecute(aemConfig: AemServerConsoleConfig) {
        val scriptContent = String(contentFile.contentsToByteArray())

        Fuel.post("${aemConfig.url}/bin/groovyconsole/post.json", listOf(Pair("script", scriptContent)))
            .timeout(NETWORK_TIMEOUT)
            .timeoutRead(NETWORK_TIMEOUT)
            .authentication()
            .basic(aemConfig.user, aemConfig.password)
            .response { _, response, result ->
                when (result) {
                    is Result.Failure -> {
                        view.print("ERROR: \n", ConsoleViewContentType.ERROR_OUTPUT)
                        view.print(result.getException().localizedMessage, ConsoleViewContentType.ERROR_OUTPUT)
                    }

                    is Result.Success -> {
                        val output = Gson().fromJson(String(response.data), GroovyConsoleOutput::class.java)

                        if (output.exceptionStackTrace.isBlank()) {
                            view.print(output.output, ConsoleViewContentType.NORMAL_OUTPUT)
                        } else {
                            //This looks a bit weird, but it works
                            view.scrollTo(0)

                            //This code relies on fact that AEM Groovy Console uses Script1.groovy as file name, so this code is highly dangerous
                            //In some obvious cases it could work incorrectly, but it provides user with better experience
                            view.print(
                                output.exceptionStackTrace.replace("Script1.groovy", contentFile.url),
                                ConsoleViewContentType.ERROR_OUTPUT
                            )
                        }

                        view.print("Execution Time:${output.runningTime}", ConsoleViewContentType.LOG_WARNING_OUTPUT)
                    }
                }
            }
    }

    private fun isValidAemConfig(aemConfig: AemServerConsoleConfig?): Boolean {
        if (aemConfig == null) {
            view.print(
                "\nAEM Config is not found for server: $serverId\n\n",
                ConsoleViewContentType.LOG_WARNING_OUTPUT
            )

            return false
        }

        if (aemConfig.user.isBlank() || aemConfig.password.isBlank()) {
            view.print(
                "\nCredentials is not specified for server: $serverId\n\n",
                ConsoleViewContentType.LOG_WARNING_OUTPUT
            )

            return false
        }

        return true
    }

    private fun getCurrentConsoleAemConfig(): AemServerConsoleConfig? {
        val config = PersistentStateService.getInstance(project).findById(serverId)

        return config?.let {
            val credentials = PasswordsService.getCredentials(it.id)

            return AemServerConsoleConfig(
                aemServerConfig = it,
                user = credentials?.userName.orEmpty(),
                password = credentials?.getPasswordAsString().orEmpty()
            )
        }
    }

    data class GroovyConsoleOutput(val output: String, val runningTime: String, val exceptionStackTrace: String)

    data class AemServerConsoleConfig(
        val aemServerConfig: AemServerConfig,
        val user: String,
        val password: String
    ) {
        val id: Long = aemServerConfig.id
        val name: String = aemServerConfig.name
        val url: String = aemServerConfig.url
    }
}