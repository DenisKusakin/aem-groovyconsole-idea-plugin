package org.deniskusakin.aem.groovyconsoleplugin.console

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
class AEMGroovyConsole(private val project: Project, private val descriptor: RunContentDescriptor, private val view: ConsoleView, private val handler: ProcessHandler) {

    companion object {
        val GROOVY_CONSOLE = Key.create<AEMGroovyConsole>("AEMGroovyConsoleKey")
        fun getOrCreateConsole(project: Project,
                               contentFile: VirtualFile) {
            val existingConsole = contentFile.getUserData<AEMGroovyConsole>(GROOVY_CONSOLE)
            if (existingConsole != null) return
            val console = createConsole(project, contentFile)
            console!!.execute("")
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
            val processHandler = createProcessHandler(contentFile.path) ?: return null

//        val consoleStateService = GroovyConsoleStateService.getInstance(project)
//        consoleStateService.setFileModule(contentFile, module)
//        val title = consoleStateService.getSelectedModuleTitle(contentFile)
            val title = "QQQQQQQQQQ"
            val consoleView = ConsoleViewImpl(project, true)
            val descriptor = RunContentDescriptor(consoleView, processHandler, JPanel(BorderLayout()), title)
            val console = AEMGroovyConsole(project, descriptor, consoleView, processHandler)

            // must call getComponent before createConsoleActions()
            val consoleViewComponent = consoleView.getComponent()

//        val actionGroup = DefaultActionGroup()
//        actionGroup.add(BuildAndRestartConsoleAction(module, project, defaultExecutor, descriptor, restarter(project, contentFile)))
//        actionGroup.addSeparator()
//        actionGroup.addAll(*consoleView.createConsoleActions())
//        actionGroup.add(object : CloseAction(defaultExecutor, descriptor, project) {
//            override fun actionPerformed(e: AnActionEvent?) {
//                processHandler!!.destroyProcess() // use force
//                super.actionPerformed(e)
//            }
//        })
//
//        val toolbar = ActionManager.getInstance().createActionToolbar("GroovyConsole", actionGroup, false)
//        toolbar.setTargetComponent(consoleViewComponent)

            val ui = descriptor.component
            ui.add(consoleViewComponent, BorderLayout.CENTER)
//        ui.add(toolbar.component, BorderLayout.WEST)

            processHandler!!.addProcessListener(object : ProcessAdapter() {
                override fun processTerminated(event: ProcessEvent) {
                    if (contentFile.getUserData<AEMGroovyConsole>(GROOVY_CONSOLE) === console) {
                        // process terminated either by closing file or by close action
                        contentFile.putUserData<AEMGroovyConsole>(GROOVY_CONSOLE, null)
                    }
                }
            })

            contentFile.putUserData<AEMGroovyConsole>(GROOVY_CONSOLE, console)
            consoleView.attachToProcess(processHandler)
            processHandler!!.startNotify()

            ExecutionManager.getInstance(project).contentManager.showRunContent(defaultExecutor, descriptor)
            return console
        }

        private fun send(processHandler: ProcessHandler, command: String) {
            val outputStream = processHandler.processInput ?: error("output stream is null")
            val charset = if (processHandler is BaseOSProcessHandler)
                processHandler.charset
            else
                null
            val bytes = (command + "\n").toByteArray(charset ?: UTF_8)
            try {
                outputStream.write(bytes)
                outputStream.flush()
            } catch (ignored: IOException) {
                LOGGER.warn(ignored)
            }

        }

        private fun createProcessHandler(scriptPath: String): ProcessHandler? {
            val login = "admin"
            val password = "admin"
            val serverHost = "http://localhost:4503"

            val commandLine = GeneralCommandLine("curl", "-u", "$login:$password", "--data-urlencode", "script@$scriptPath", "-X", "POST", "-H", "Content-Type: application/x-www-form-urlencoded; charset=UTF-8", "$serverHost/bin/groovyconsole/post.json")
                    .withParameters("-s")
            val processHandler = OSProcessHandler(commandLine)
            processHandler.setShouldDestroyProcessRecursively(false)
            return processHandler

        }

    }

    private fun doExecute(command: String) {
        for (line in command.trim { it <= ' ' }.split("\n".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()) {
            view.print("> ", ConsoleViewContentType.USER_INPUT)
            view.print(line, ConsoleViewContentType.USER_INPUT)
            view.print("\n", ConsoleViewContentType.NORMAL_OUTPUT)
        }
        ApplicationManager.getApplication().executeOnPooledThread { send(handler, StringUtil.replace(command, "\n", "###\\n")) }
    }

    fun execute(command: String) {
        if (!StringUtil.isEmptyOrSpaces(command)) doExecute(command)
        ExecutionManager.getInstance(project).contentManager.toFrontRunContent(defaultExecutor, descriptor)
    }

}