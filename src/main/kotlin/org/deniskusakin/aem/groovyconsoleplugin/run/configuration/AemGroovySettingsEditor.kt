package org.deniskusakin.aem.groovyconsoleplugin.run.configuration

import com.intellij.openapi.components.ServiceManager
import com.intellij.openapi.fileChooser.FileChooserDescriptor
import com.intellij.openapi.options.ConfigurationException
import com.intellij.openapi.options.SettingsEditor
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.ComboBox
import com.intellij.openapi.ui.TextFieldWithBrowseButton
import com.intellij.ui.CollectionComboBoxModel
import com.intellij.ui.layout.CCFlags
import com.intellij.ui.layout.LCFlags
import com.intellij.ui.layout.panel
import org.deniskusakin.aem.groovyconsoleplugin.services.PersistentStateService

import javax.swing.*

class AemGroovySettingsEditor(private val project: Project) : SettingsEditor<AemGroovyRunConfiguration>() {
    private var contentPane: JPanel
    private val scriptPath: TextFieldWithBrowseButton = TextFieldWithBrowseButton()
    private val aemServerComboBox = ComboBox<String>()
    private val service = ServiceManager.getService(project, PersistentStateService::class.java)
    private val items = service.getAEMServers().map { it.name }

    init {
//        val service = ServiceManager.getService(project, PersistentStateService::class.java)
//        val items = service.getAEMServers().map { it.name }
        aemServerComboBox.model = CollectionComboBoxModel(items)

        contentPane = panel {
            //TODO: Check AppearanceConfigurable to get info about ComboBox
            row(label = "Script Path: ") {
                scriptPath(CCFlags.grow, CCFlags.push)
            }
            row(label = "AEM Server: ") {
                aemServerComboBox(CCFlags.grow, CCFlags.push)
            }
        }
    }

    override fun resetEditorFrom(s: AemGroovyRunConfiguration) {
        scriptPath.text = s.scriptPath ?: ""
        aemServerComboBox.editor.item = s.name
    }

    @Throws(ConfigurationException::class)
    override fun applyEditorTo(runConfiguration: AemGroovyRunConfiguration) {
        runConfiguration.scriptPath = scriptPath.text
        runConfiguration.serverName = aemServerComboBox.editor.item as String?
    }

    override fun createEditor(): JComponent {
        val fileChooserDescriptor = FileChooserDescriptor(true, false, false, false, false, false)
        scriptPath.addBrowseFolderListener("title", "desc", project, fileChooserDescriptor)
        return contentPane
    }

    private fun createUIComponents() {
        // TODO: place custom component creation code here
    }

}
