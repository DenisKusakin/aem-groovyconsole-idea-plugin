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
import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.module.Module
import com.intellij.openapi.util.Key
import com.intellij.openapi.util.text.StringUtil
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.util.Consumer
import java.awt.BorderLayout
import java.io.IOException
import java.nio.charset.Charset
import javax.swing.JPanel

/**
 * @author Denis_Kusakin. 6/26/2018.
 */
class AEMGroovyConsole(private val project: Project, private val descriptor: RunContentDescriptor, private val view: ConsoleView) {

    companion object {
        val GROOVY_CONSOLE = Key.create<AEMGroovyConsole>("AEMGroovyConsoleKey")
        fun getOrCreateConsole(project: Project,
                               contentFile: VirtualFile) {
//            val existingConsole = contentFile.getUserData<AEMGroovyConsole>(GROOVY_CONSOLE)
//            if (existingConsole != null) {
//                existingConsole.execute(String(contentFile.contentsToByteArray()))
//                return
//            }
            val console = createConsole(project, contentFile)
            console!!.execute(String(contentFile.contentsToByteArray()))
//        val initializer = { module ->
//            val console = createConsole(project, contentFile, module)
//            if (console != null) {
//                callback.consume(console)
//            }
//        }
//
//        val module = GroovyConsoleStateService.getInstance(project).getSelectedModule(contentFile)
//        if (module == null || module!!.isDisposed()) {
//            // if not, then select module, then run initializer
//            GroovyConsoleUtil.selectModuleAndRun(project, initializer)
//        } else {
//            // if module for console is already selected, then use it for creation
//            initializer.consume(module)
//        }
        }

        private val defaultExecutor = DefaultRunExecutor.getRunExecutorInstance()
        private val UTF_8 = Charset.forName("UTF-8")
        private val LOGGER = Logger.getInstance(AEMGroovyConsole::class.java)

        fun createConsole(project: Project,
                          contentFile: VirtualFile): AEMGroovyConsole? {
            val title = "ServerName:${contentFile.name}"
            val consoleView = ConsoleViewImpl(project, true)
            val descriptor = RunContentDescriptor(consoleView, null, JPanel(BorderLayout()), title)
            val console = AEMGroovyConsole(project, descriptor, consoleView)

            val consoleViewComponent = consoleView.component

            val ui = descriptor.component
            ui.add(consoleViewComponent, BorderLayout.CENTER)
            //consoleView.print("!!!!!!!!!!!!!!!!!!!!!!!!!", ConsoleViewContentType.LOG_WARNING_OUTPUT)

            contentFile.putUserData<AEMGroovyConsole>(GROOVY_CONSOLE, console)
            ExecutionManager.getInstance(project).contentManager.showRunContent(defaultExecutor, descriptor)
            return console
        }

    }

    private fun doExecute(command: String) {
        for (line in command.trim { it <= ' ' }.split("\n".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()) {
            view.print("> ", ConsoleViewContentType.USER_INPUT)
            view.print(line, ConsoleViewContentType.USER_INPUT)
            view.print("\n", ConsoleViewContentType.NORMAL_OUTPUT)
        }
//        ApplicationManager.getApplication().executeOnPooledThread { send( StringUtil.replace(command, "\n", "###\\n")) }
    }

    fun execute(scriptContent: String) {
//        if (!StringUtil.isEmptyOrSpaces(command)) doExecute(command)
        ExecutionManager.getInstance(project).contentManager.toFrontRunContent(defaultExecutor, descriptor)
        val login = "admin"
        val password = "admin"
        val serverHost = "http://localhost:4503"
        Fuel.post("$serverHost/bin/groovyconsole/post.json", listOf(Pair("script", scriptContent))).authenticate(login, password).response { request, response, result ->
            when (result) {
                is Result.Failure -> {

                }
                is Result.Success -> {
                    val output = Gson().fromJson<GroovyConsoleOutput>(String(response.data), GroovyConsoleOutput::class.java)
                    view.print("Execution Time:${output.runningTime}", ConsoleViewContentType.LOG_WARNING_OUTPUT)
                    view.print("\n\n", ConsoleViewContentType.NORMAL_OUTPUT)
                    if (output.exceptionStackTrace.isBlank()) {
                        view.print(output.output, ConsoleViewContentType.NORMAL_OUTPUT)
                    } else {
                        view.print(output.exceptionStackTrace, ConsoleViewContentType.ERROR_OUTPUT)
                    }
                }
            }
        }
    }

    data class GroovyConsoleOutput(val output: String, val runningTime: String, val exceptionStackTrace: String)
}