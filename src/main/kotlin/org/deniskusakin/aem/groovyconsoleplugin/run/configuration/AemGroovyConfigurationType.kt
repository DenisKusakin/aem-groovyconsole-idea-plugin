package org.deniskusakin.aem.groovyconsoleplugin.run.configuration

import com.intellij.execution.configurations.ConfigurationFactory
import com.intellij.execution.configurations.ConfigurationType
import com.intellij.openapi.util.IconLoader
import org.deniskusakin.aem.groovyconsoleplugin.actions.Icons
import javax.swing.Icon

/**
 * @author Denis_Kusakin. 6/19/2018.
 */
class AemGroovyConfigurationType : ConfigurationType {
    override fun getIcon(): Icon {
        return Icons.GROOVY_ICON
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