package org.deniskusakin.aem.groovyconsoleplugin.run.configuration

import com.intellij.execution.configurations.ConfigurationFactory
import com.intellij.execution.configurations.ConfigurationType
import com.intellij.openapi.util.IconLoader
import javax.swing.Icon

/**
 * @author Denis_Kusakin. 6/19/2018.
 */
class AemGroovyConfigurationType : ConfigurationType {
    private val icon = IconLoader.getIcon("/icons/groovy_16x16.png")
    override fun getIcon(): Icon {
        return icon
    }

    override fun getConfigurationTypeDescription(): String {
        return "AEM Groovy Console"
    }

    override fun getId(): String {
        return "AEM_GROOVY_CONSOLE_CONFIGURATION"
    }

    override fun getDisplayName(): String {
        return "AEM GroovyConsole"
    }

    override fun getConfigurationFactories(): Array<ConfigurationFactory> {
        return arrayOf(AemGroovyConfigurationFactory(this))
    }

}