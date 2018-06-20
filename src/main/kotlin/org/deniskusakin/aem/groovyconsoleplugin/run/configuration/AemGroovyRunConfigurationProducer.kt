package org.deniskusakin.aem.groovyconsoleplugin.run.configuration

import com.intellij.execution.actions.ConfigurationContext
import com.intellij.execution.actions.RunConfigurationProducer
import com.intellij.execution.configurations.ConfigurationType
import com.intellij.openapi.util.Ref
import com.intellij.psi.PsiElement

class AemGroovyRunConfigurationProducer(configurationType: ConfigurationType?) : RunConfigurationProducer<AemGroovyRunConfiguration>(configurationType) {
    override fun isConfigurationFromContext(configuration: AemGroovyRunConfiguration?, context: ConfigurationContext?): Boolean {
        return true
    }

    override fun setupConfigurationFromContext(configuration: AemGroovyRunConfiguration?, context: ConfigurationContext?, sourceElement: Ref<PsiElement>?): Boolean {
        return true
    }
}