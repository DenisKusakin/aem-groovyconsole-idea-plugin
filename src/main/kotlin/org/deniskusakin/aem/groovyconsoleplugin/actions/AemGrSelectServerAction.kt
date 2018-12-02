package org.deniskusakin.aem.groovyconsoleplugin.actions

import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.components.ServiceManager
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.popup.PopupStep
import com.intellij.openapi.ui.popup.util.BaseListPopupStep
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.ui.popup.list.ListPopupImpl
import org.deniskusakin.aem.groovyconsoleplugin.console.AEMGroovyConsole
import org.deniskusakin.aem.groovyconsoleplugin.console.AEMGroovyConsole.Companion.GROOVY_CONSOLE_CURRENT_SERVER
import org.deniskusakin.aem.groovyconsoleplugin.services.PersistentStateService

/**
 * @author Denis_Kusakin. 6/28/2018.
 */
class AemGrSelectServerAction(private val project: Project, private val file: VirtualFile, private val serverName: String)
    : AnAction(serverName, "AEM Server", Icons.AEM_ICON) {
    override fun actionPerformed(e: AnActionEvent) {
        val service = ServiceManager.getService(project, PersistentStateService::class.java)
        val component = e.inputEvent?.component ?: return
        val step = object : BaseListPopupStep<String>("On which server the script should be applied?", service.getAEMServers().map { it.name }) {
            override fun onChosen(selectedValue: String?, finalChoice: Boolean): PopupStep<*>? {
                file.putUserData(GROOVY_CONSOLE_CURRENT_SERVER, selectedValue)
                return null
            }
        }
        val popup = ListPopupImpl(step)
        popup.showUnderneathOf(component)
    }

    override fun displayTextInToolbar(): Boolean = true

    override fun update(e: AnActionEvent) {
        e.presentation.text = file.getUserData(AEMGroovyConsole.GROOVY_CONSOLE_CURRENT_SERVER).orEmpty()
    }
}