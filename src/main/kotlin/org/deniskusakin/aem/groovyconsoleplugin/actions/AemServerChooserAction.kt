package org.deniskusakin.aem.groovyconsoleplugin.actions

import com.intellij.icons.AllIcons
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.CommonDataKeys
import com.intellij.openapi.actionSystem.DefaultActionGroup
import com.intellij.openapi.actionSystem.Presentation
import com.intellij.openapi.actionSystem.ex.ComboBoxAction
import com.intellij.openapi.project.Project
import com.intellij.util.ui.JBUI
import com.intellij.util.ui.JBUI.Fonts
import org.deniskusakin.aem.groovyconsoleplugin.console.GroovyConsoleUserData.getCurrentAemConfig
import org.deniskusakin.aem.groovyconsoleplugin.services.PersistentStateService
import java.awt.BorderLayout
import javax.swing.JComponent
import javax.swing.JLabel
import javax.swing.JPanel

/**
 * @author Denis_Kusakin. 6/28/2018.
 */
class AemServerChooserAction(
    private val project: Project,
) : ComboBoxAction() {

    private val persistentStateService by lazy {
        PersistentStateService.getInstance(project)
    }

    init {
        isSmallVariant = true
        templatePresentation.icon = AllIcons.Webreferences.Server
    }

    override fun createCustomComponent(presentation: Presentation, place: String): JComponent {
        return JPanel(BorderLayout(JBUI.scale(3), 0)).also { panel ->
            val comboBoxButton = createComboBoxButton(presentation)

            val label = JLabel("Run On:").also {
                it.font = Fonts.toolbarSmallComboBoxFont()
                it.labelFor = comboBoxButton
                it.border = JBUI.Borders.empty(0, 0, 0, 3)
            }

            panel.add(comboBoxButton, BorderLayout.CENTER)
            panel.add(label, BorderLayout.WEST)
            panel.border = JBUI.Borders.empty(0, 6, 0, 3)
        }
    }

    override fun createPopupActionGroup(button: JComponent): DefaultActionGroup {
        return DefaultActionGroup(
            persistentStateService.getAEMServers().map { AemSelectServerAction(it) }
        )
    }

    override fun update(e: AnActionEvent) {
        val project = e.project
        val editor = CommonDataKeys.EDITOR.getData(e.dataContext)
        val virtualFile = CommonDataKeys.VIRTUAL_FILE.getData(e.dataContext)

        if (project != null && editor != null && virtualFile != null) {
            e.presentation.text = virtualFile.getCurrentAemConfig(project)?.name.orEmpty()
        }
    }
}