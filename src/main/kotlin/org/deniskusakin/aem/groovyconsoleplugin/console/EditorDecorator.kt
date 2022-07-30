package org.deniskusakin.aem.groovyconsoleplugin.console

import com.intellij.openapi.actionSystem.ActionManager
import com.intellij.openapi.actionSystem.CommonShortcuts
import com.intellij.openapi.actionSystem.DefaultActionGroup
import com.intellij.openapi.editor.impl.EditorHeaderComponent
import com.intellij.openapi.fileEditor.FileEditor
import com.intellij.openapi.options.ShowSettingsUtil
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.Key
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.ui.EditorNotificationPanel
import com.intellij.ui.EditorNotifications
import org.deniskusakin.aem.groovyconsoleplugin.actions.AemConsoleExecuteAction
import org.deniskusakin.aem.groovyconsoleplugin.actions.AemSelectServerAction
import org.deniskusakin.aem.groovyconsoleplugin.config.SettingsChangedNotifier
import org.deniskusakin.aem.groovyconsoleplugin.config.ui.AemServersConfigurable
import org.deniskusakin.aem.groovyconsoleplugin.console.GroovyConsoleUserData.getCurrentAemConfig
import org.deniskusakin.aem.groovyconsoleplugin.console.GroovyConsoleUserData.setCurrentAemServerId
import org.deniskusakin.aem.groovyconsoleplugin.services.AemGroovyScriptsDetectionService
import org.deniskusakin.aem.groovyconsoleplugin.services.PersistentStateService
import org.deniskusakin.aem.groovyconsoleplugin.services.model.AemServerConfig
import javax.swing.JComponent

class EditorDecorator(project: Project) : EditorNotifications.Provider<JComponent>() {
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

    override fun createNotificationPanel(file: VirtualFile, fileEditor: FileEditor, project: Project): JComponent? {
        if (file.extension != "groovy" || !AemGroovyScriptsDetectionService.isAemGroovyFile(file.path, project)) {
            return null
        }

        val configFromFile = file.getCurrentAemConfig(project)
        val availableServers = PersistentStateService.getInstance(project).getAEMServers()

        val currentServer: AemServerConfig? = configFromFile ?: availableServers.firstOrNull()

        file.setCurrentAemServerId(currentServer?.id)

        if (currentServer == null) {
            return EditorNotificationPanel().apply {
                text = "AEM Servers configuration is missing"
                createActionLabel("Configure") {
                    ShowSettingsUtil.getInstance().showSettingsDialog(project, AemServersConfigurable::class.java)
                }
            }
        }

        return EditorHeaderComponent().apply {
            val execAction = AemConsoleExecuteAction().apply {
                registerCustomShortcutSet(CommonShortcuts.CTRL_ENTER, fileEditor.component)
            }

            val actionGroup = DefaultActionGroup(execAction, AemSelectServerAction(project, file, currentServer.name))
            val menu = ActionManager.getInstance().createActionToolbar("AemGroovyConsole", actionGroup, true)

            add(menu.component)
        }
    }
}