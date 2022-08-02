package org.deniskusakin.aem.groovyconsoleplugin.actions

import com.intellij.icons.AllIcons
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.CommonDataKeys
import org.deniskusakin.aem.groovyconsoleplugin.console.GroovyConsoleUserData.setCurrentAemServerId
import org.deniskusakin.aem.groovyconsoleplugin.services.model.AemServerConfig

/**
 * User: Andrey Bardashevsky
 * Date/Time: 02.08.2022 13:32
 */
class AemSelectServerAction(private val serverConfig: AemServerConfig) : AnAction() {

    init {
        templatePresentation.text = serverConfig.name
        templatePresentation.icon = AllIcons.Webreferences.Server
    }

    override fun actionPerformed(e: AnActionEvent) {
        val project = e.project
        val editor = CommonDataKeys.EDITOR.getData(e.dataContext)
        val virtualFile = CommonDataKeys.VIRTUAL_FILE.getData(e.dataContext)

        if (project != null && editor != null && virtualFile != null) {
            virtualFile.setCurrentAemServerId(serverConfig.id)
        }
    }
}