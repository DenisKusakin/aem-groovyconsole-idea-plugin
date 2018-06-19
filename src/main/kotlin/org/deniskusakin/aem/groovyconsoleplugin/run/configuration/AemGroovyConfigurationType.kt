package org.deniskusakin.aem.groovyconsoleplugin.run.configuration

import com.intellij.execution.configurations.ConfigurationFactory
import com.intellij.execution.configurations.ConfigurationType
import com.intellij.icons.AllIcons
import javax.swing.Icon

/**
 * @author Denis_Kusakin. 6/19/2018.
 */
class AemGroovyConfigurationType : ConfigurationType{
    override fun getIcon(): Icon {
        return AllIcons.General.Information
    }

    override fun getConfigurationTypeDescription(): String {
        return "AEM Groovy Console Description"
    }

    override fun getId(): String {
        return "AEM_GROOVYCONSOLE_CONFIGURATION"
    }

    override fun getDisplayName(): String {
        return "AEM GroovyConsole Configuration"
    }

    override fun getConfigurationFactories(): Array<ConfigurationFactory> {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

}