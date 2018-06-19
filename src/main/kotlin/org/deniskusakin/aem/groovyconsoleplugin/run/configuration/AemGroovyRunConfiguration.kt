package org.deniskusakin.aem.groovyconsoleplugin.run.configuration

import com.intellij.execution.Executor
import com.intellij.execution.configurations.*
import com.intellij.execution.runners.ExecutionEnvironment
import com.intellij.openapi.options.SettingsEditor
import com.intellij.openapi.project.Project

/**
 * @author Denis_Kusakin. 6/19/2018.
 */
class AemGroovyRunConfiguration(project: Project, factory: ConfigurationFactory, name: String?) : LocatableConfigurationBase(project, factory, name) {
    override fun getConfigurationEditor(): SettingsEditor<out RunConfiguration> {
        return AemGroovySettingsEditor()
    }

    override fun getState(executor: Executor, environment: ExecutionEnvironment): RunProfileState? {
        return null
    }
}