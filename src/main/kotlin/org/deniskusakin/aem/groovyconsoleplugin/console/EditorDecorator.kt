package org.deniskusakin.aem.groovyconsoleplugin.console

import com.intellij.openapi.actionSystem.ActionManager
import com.intellij.openapi.actionSystem.CommonShortcuts
import com.intellij.openapi.actionSystem.DefaultActionGroup
import com.intellij.openapi.components.ServiceManager
import com.intellij.openapi.editor.impl.EditorHeaderComponent
import com.intellij.openapi.fileEditor.FileEditor
import com.intellij.openapi.options.ShowSettingsUtil
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.Key
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.ui.EditorNotificationPanel
import com.intellij.ui.EditorNotifications
import org.deniskusakin.aem.groovyconsoleplugin.actions.AemGrExecuteAction
import org.deniskusakin.aem.groovyconsoleplugin.actions.AemGrSelectServerAction
import org.deniskusakin.aem.groovyconsoleplugin.config.AemServersConfigurable
import org.deniskusakin.aem.groovyconsoleplugin.config.SettingsChangedNotifier
import org.deniskusakin.aem.groovyconsoleplugin.services.AemGroovyScriptsDetectionService
import org.deniskusakin.aem.groovyconsoleplugin.services.PersistentStateService
import javax.swing.JComponent

class EditorDecorator(private val project: Project) : EditorNotifications.Provider<JComponent>() {
    companion object {
        private val myKey = Key.create<JComponent>("aem.groovy.console.toolbar")
    }

    init {
        val notifications = project.getService(EditorNotifications::class.java)
        project.messageBus.connect(project).subscribe(SettingsChangedNotifier.TOPIC, object : SettingsChangedNotifier {
            override fun settingsChanged() {
                notifications.updateAllNotifications()
            }
        })
    }

    override fun getKey(): Key<JComponent> = myKey

    override fun createNotificationPanel(file: VirtualFile, fileEditor: FileEditor): JComponent? {
        if (file.extension != "groovy" || !AemGroovyScriptsDetectionService.isAemGroovyFile(file.path, project))
            return null

        val service = ServiceManager.getService(project, PersistentStateService::class.java)
        val serverFromFile = file.getUserData(AEMGroovyConsole.GROOVY_CONSOLE_CURRENT_SERVER)
        val availableServerNames = service.getAEMServers().map { it.name }
        val currentServerName =
            (if (serverFromFile == null || availableServerNames.contains(serverFromFile)) serverFromFile else null)
                ?: availableServerNames.firstOrNull()
        file.putUserData(AEMGroovyConsole.GROOVY_CONSOLE_CURRENT_SERVER, currentServerName)
        if (currentServerName == null) {
            val notificationPanel = EditorNotificationPanel()
            notificationPanel.setText("AEM Servers configuration is missing")
            notificationPanel.createActionLabel("Configure") {
                ShowSettingsUtil.getInstance().showSettingsDialog(project, AemServersConfigurable::class.java)
            }
            return notificationPanel
        }
        val execAction = AemGrExecuteAction()
        execAction.registerCustomShortcutSet(CommonShortcuts.CTRL_ENTER, fileEditor.component)
        val actionGroup = DefaultActionGroup(execAction, AemGrSelectServerAction(project, file, currentServerName))
        val menu = ActionManager.getInstance().createActionToolbar("AemGroovyConsole", actionGroup, true)

        return EditorHeaderComponent().apply {
            add(menu.component)
        }
    }
}