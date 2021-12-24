package org.deniskusakin.aem.groovyconsoleplugin.run.configuration

import com.intellij.openapi.fileChooser.FileChooserDescriptor
import com.intellij.openapi.options.ConfigurationException
import com.intellij.openapi.options.SettingsEditor
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.ComboBox
import com.intellij.openapi.ui.TextFieldWithBrowseButton
import com.intellij.ui.layout.CCFlags
import com.intellij.ui.layout.panel
import org.deniskusakin.aem.groovyconsoleplugin.services.PersistentStateService
import javax.swing.JComponent
import javax.swing.JPanel

class AemGroovySettingsEditor(private val project: Project) : SettingsEditor<AemGroovyRunConfiguration>() {
    private var contentPane: JPanel
    private val scriptPath: TextFieldWithBrowseButton = TextFieldWithBrowseButton()
    private val service = project.getService(PersistentStateService::class.java)
    private val items = service.getAEMServers().map { it.name } + ""
    private val aemServerComboBox = ComboBox(items.toTypedArray())

    init {
//        aemServerComboBox.model = CollectionComboBoxModel(items)
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
        aemServerComboBox.selectedIndex = items.indexOf(s.serverName)
    }

    override fun applyEditorTo(runConfiguration: AemGroovyRunConfiguration) {
        if (scriptPath.text.isBlank()) {
            throw ConfigurationException("Script Path is not defined")
        }
        if((aemServerComboBox.selectedItem as String?).isNullOrBlank()){
            throw ConfigurationException("AEM Server is not specified")
        }
        runConfiguration.scriptPath = scriptPath.text
        runConfiguration.serverName = aemServerComboBox.selectedItem as String?
    }

    override fun createEditor(): JComponent {
        val fileChooserDescriptor = FileChooserDescriptor(true, false, false, false, false, false)
        scriptPath.addBrowseFolderListener("title", "desc", project, fileChooserDescriptor)
        return contentPane
    }

}
