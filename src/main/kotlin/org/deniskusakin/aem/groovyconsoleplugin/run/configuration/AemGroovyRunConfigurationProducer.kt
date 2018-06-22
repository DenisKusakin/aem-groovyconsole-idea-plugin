package org.deniskusakin.aem.groovyconsoleplugin.run.configuration

import com.intellij.execution.actions.ConfigurationContext
import com.intellij.execution.actions.RunConfigurationProducer
import com.intellij.execution.configurations.ConfigurationType
import com.intellij.openapi.util.Ref
import com.intellij.psi.PsiElement

class AemGroovyRunConfigurationProducer private constructor() : RunConfigurationProducer<AemGroovyRunConfiguration>(AemGroovyConfigurationType()) {
    override fun isConfigurationFromContext(configuration: AemGroovyRunConfiguration, context: ConfigurationContext): Boolean {
        if (context.location?.virtualFile?.path == configuration.scriptPath) {
            return true
        }
        return false
    }

    override fun setupConfigurationFromContext(configuration: AemGroovyRunConfiguration, context: ConfigurationContext, sourceElement: Ref<PsiElement>?): Boolean {
        if (context.location?.virtualFile?.extension == "groovy") {
            configuration.scriptPath = context.location?.virtualFile?.path
            configuration.serverHost = "http://localhost:4502"
            configuration.login = "admin"
            configuration.password = "admin"
            configuration.name = context.location?.virtualFile?.path

            return true
        }
        return false
    }
}