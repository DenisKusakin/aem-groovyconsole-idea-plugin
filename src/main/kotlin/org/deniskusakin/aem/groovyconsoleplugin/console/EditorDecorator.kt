package org.deniskusakin.aem.groovyconsoleplugin.console

import com.intellij.codeInsight.hint.HintManager
import com.intellij.notification.Notification
import com.intellij.notification.NotificationType
import com.intellij.notification.Notifications
import com.intellij.openapi.actionSystem.ActionManager
import com.intellij.openapi.actionSystem.CommonShortcuts
import com.intellij.openapi.actionSystem.DefaultActionGroup
import com.intellij.openapi.components.ServiceManager
import com.intellij.openapi.editor.impl.EditorHeaderComponent
import com.intellij.openapi.fileEditor.FileEditor
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.Key
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.ui.EditorNotifications
import org.deniskusakin.aem.groovyconsoleplugin.actions.AemGrExecuteAction
import org.deniskusakin.aem.groovyconsoleplugin.actions.AemGrSelectServerAction
import org.deniskusakin.aem.groovyconsoleplugin.services.PersistentStateService
import javax.swing.JComponent

class EditorDecorator(private val project: Project) : EditorNotifications.Provider<JComponent>() {
    companion object {
        private val myKey = Key.create<JComponent>("aem.groovy.console.toolbar")
    }

    override fun getKey(): Key<JComponent> = myKey

    override fun createNotificationPanel(file: VirtualFile, fileEditor: FileEditor): JComponent? {
        if (file.extension != "groovy" || !file.path.contains("groovyconsole")) return null
        val service = ServiceManager.getService(project, PersistentStateService::class.java)
        val currentServerName = file.getUserData(AEMGroovyConsole.GROOVY_CONSOLE_CURRENT_SERVER)
                ?: service.getAEMServers().map { it.name }.firstOrNull()
        if (currentServerName == null) {
            //HintManager.getInstance().showErrorHint(fileEditor, "")
            Notifications.Bus.notify(Notification("AEM Servers Missing Configuration", "Missing AEM Servers Configuration", "Missing AEM Servers Configuration", NotificationType.WARNING))
            return null
        }
        file.putUserData(AEMGroovyConsole.GROOVY_CONSOLE_CURRENT_SERVER, currentServerName)
        val execAction = AemGrExecuteAction()
        execAction.registerCustomShortcutSet(CommonShortcuts.CTRL_ENTER, fileEditor.component)
        val actionGroup = DefaultActionGroup(execAction, AemGrSelectServerAction(project, file, currentServerName))
        val menu = ActionManager.getInstance().createActionToolbar("AemGroovyConsole", actionGroup, true)

        return EditorHeaderComponent().apply {
            add(menu.component)
        }
    }
}