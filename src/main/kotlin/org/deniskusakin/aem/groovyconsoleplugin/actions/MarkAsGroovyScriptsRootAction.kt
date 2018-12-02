package org.deniskusakin.aem.groovyconsoleplugin.actions

import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.CommonDataKeys
import org.deniskusakin.aem.groovyconsoleplugin.config.SettingsChangedNotifier
import org.deniskusakin.aem.groovyconsoleplugin.services.AemGroovyScriptsDetectionService
import org.deniskusakin.aem.groovyconsoleplugin.services.RootFoldersService

class MarkAsGroovyScriptsRootAction : AnAction() {
    override fun update(e: AnActionEvent) {
        val project = e.project
        if (project == null) {
            e.presentation.isEnabledAndVisible = false
            return
        }
        val virtualFile = CommonDataKeys.VIRTUAL_FILE.getData(e.dataContext) ?: return
        if (AemGroovyScriptsDetectionService.isGroovyScriptsRoot(virtualFile.path, project)) {
            e.presentation.text = "Unmark as AEM Groovy scripts root"
            e.presentation.description = "Unmark as Mark directory as AEM Groovy scripts root"
        } else {
            e.presentation.text = "AEM Groovy scripts root"
            e.presentation.description = "Mark directory as AEM Groovy scripts root"
            e.presentation.icon = Icons.GROOVY_ICON
        }
    }

    override fun actionPerformed(e: AnActionEvent) {
        val project = e.project ?: return
        val service = project.let { RootFoldersService.getInstance(it) } ?: return
        val virtualFile = CommonDataKeys.VIRTUAL_FILE.getData(e.dataContext) ?: return
        if (AemGroovyScriptsDetectionService.isGroovyScriptsRoot(virtualFile.path, project)) {
            service.removeRoot(virtualFile.path)
        } else {
            service.addRoot(virtualFile.path)
        }

        e.project?.messageBus?.syncPublisher(SettingsChangedNotifier.TOPIC)?.settingsChanged()
    }
}