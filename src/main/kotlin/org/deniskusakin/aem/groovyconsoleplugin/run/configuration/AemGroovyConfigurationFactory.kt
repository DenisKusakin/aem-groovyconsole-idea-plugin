package org.deniskusakin.aem.groovyconsoleplugin.run.configuration

import com.intellij.execution.configurations.ConfigurationFactory
import com.intellij.execution.configurations.ConfigurationType
import com.intellij.execution.configurations.RunConfiguration
import com.intellij.openapi.project.Project

/**
 * @author Denis_Kusakin. 6/19/2018.
 */
class AemGroovyConfigurationFactory(type: ConfigurationType) : ConfigurationFactory(type) {

    override fun createTemplateConfiguration(project: Project): RunConfiguration {
        return AemGroovyRunConfiguration(project, this, "AEM Groovy Console")
    }

    override fun getName(): String {
        return "AEM Groovy Configuration Factory"
    }
}