package org.deniskusakin.aem.groovyconsoleplugin.run.configuration

import com.intellij.execution.actions.ConfigurationContext
import com.intellij.execution.actions.RunConfigurationProducer
import com.intellij.openapi.components.ServiceManager
import com.intellij.openapi.util.Ref
import com.intellij.psi.PsiElement
import org.deniskusakin.aem.groovyconsoleplugin.services.PersistentStateService

class AemGroovyRunConfigurationProducer private constructor() : RunConfigurationProducer<AemGroovyRunConfiguration>(AemGroovyConfigurationType()) {

    override fun isConfigurationFromContext(configuration: AemGroovyRunConfiguration, context: ConfigurationContext): Boolean {
        val service = ServiceManager.getService(context.project, PersistentStateService::class.java)
        val firstServerFromGlobalConfig = service.getAEMServers().firstOrNull()

        return context.location?.virtualFile?.path == configuration.scriptPath && configuration.serverName == firstServerFromGlobalConfig?.name
    }

    override fun setupConfigurationFromContext(configuration: AemGroovyRunConfiguration, context: ConfigurationContext, sourceElement: Ref<PsiElement>): Boolean {
        val service = ServiceManager.getService(context.project, PersistentStateService::class.java)
        val firstServerFromGlobalConfig = service.getAEMServers().firstOrNull()
        if (true && firstServerFromGlobalConfig != null) {
            configuration.scriptPath = context.location?.virtualFile?.path
            configuration.serverName = configuration.serverName ?: firstServerFromGlobalConfig.name
            configuration.name = "${firstServerFromGlobalConfig.name}:${context.location?.virtualFile?.path}"

            return true
        }
        return false
    }
}