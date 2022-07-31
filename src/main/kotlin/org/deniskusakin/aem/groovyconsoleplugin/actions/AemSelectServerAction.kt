package org.deniskusakin.aem.groovyconsoleplugin.actions

import com.intellij.icons.AllIcons
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.popup.PopupStep
import com.intellij.openapi.ui.popup.util.BaseListPopupStep
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.ui.popup.list.ListPopupImpl
import org.deniskusakin.aem.groovyconsoleplugin.console.GroovyConsoleUserData.getCurrentAemConfig
import org.deniskusakin.aem.groovyconsoleplugin.console.GroovyConsoleUserData.setCurrentAemServerId
import org.deniskusakin.aem.groovyconsoleplugin.services.PersistentStateService
import org.deniskusakin.aem.groovyconsoleplugin.services.model.AemServerConfig

/**
 * @author Denis_Kusakin. 6/28/2018.
 */
class AemSelectServerAction(
    private val project: Project,
    private val file: VirtualFile,
    name: String
) : AnAction(name, "AEM server", AllIcons.Webreferences.Server) {

    private val persistentStateService by lazy {
        PersistentStateService.getInstance(project)
    }

    override fun actionPerformed(e: AnActionEvent) {
        val component = e.inputEvent?.component ?: return

        val step = object : BaseListPopupStep<AemServerConfig>(
            "On Which AEM Server The Script Should Be Applied?",
            persistentStateService.getAEMServers(),
            AllIcons.Webreferences.Server
        ) {
            override fun getTextFor(value: AemServerConfig): String {
                return value.name
            }

            override fun onChosen(selectedValue: AemServerConfig?, finalChoice: Boolean): PopupStep<*>? {
                if (selectedValue != null) {
                    file.setCurrentAemServerId(selectedValue.id)
                }

                return super.onChosen(selectedValue, finalChoice)
            }
        }

        ListPopupImpl(project, step).apply {
            showUnderneathOf(component)
        }
    }

    override fun displayTextInToolbar(): Boolean = true

    override fun update(e: AnActionEvent) {
        e.presentation.text = file.getCurrentAemConfig(project)?.name.orEmpty()
    }
}