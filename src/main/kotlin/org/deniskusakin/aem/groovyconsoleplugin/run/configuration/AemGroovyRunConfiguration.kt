package org.deniskusakin.aem.groovyconsoleplugin.run.configuration

import com.intellij.execution.Executor
import com.intellij.execution.configurations.*
import com.intellij.execution.process.OSProcessHandler
import com.intellij.execution.process.ProcessHandler
import com.intellij.execution.runners.ExecutionEnvironment
import com.intellij.openapi.options.SettingsEditor
import com.intellij.openapi.project.Project

/**
 * @author Denis_Kusakin. 6/19/2018.
 */
class AemGroovyRunConfiguration(project: Project, factory: ConfigurationFactory, name: String?) : LocatableConfigurationBase(project, factory, name) {
    override fun getConfigurationEditor(): SettingsEditor<out RunConfiguration> {
        return AemGroovySettingsEditor(project)
    }

    override fun getState(executor: Executor, environment: ExecutionEnvironment): RunProfileState? {
        //val commandLine = GeneralCommandLine("curl")
        return GrState(environment)
    }
}

class GrState(env: ExecutionEnvironment) : CommandLineState(env){
    val path = "D:\\Projects\\aem-groovyconsole-idea-plugin\\src\\main\\resources\\t.groovy"
    val username = "admin"
    val password = "admin"
    val str = "curl -u ${username}:${password} --data-urlencode script@${path} -X POST -H \"Content-Type: application/x-www-form-urlencoded; charset=UTF-8\" http://localhost:4502/bin/groovyconsole/post.json"
    override fun startProcess(): ProcessHandler {
        val commandLine = GeneralCommandLine(str)
//                .withParameters("-d", "\"scriptPath=${path}\"", "-X", "POST", "-u", "admin:admin", "http://localhost:4502/bin/groovyconsole/post.json")
        val processHandler = OSProcessHandler(commandLine)
        processHandler.setShouldDestroyProcessRecursively(false)
        //processHandler.startNotify()
        return processHandler
    }

}