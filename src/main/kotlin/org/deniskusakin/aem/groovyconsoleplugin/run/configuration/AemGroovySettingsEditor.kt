package org.deniskusakin.aem.groovyconsoleplugin.run.configuration

import com.intellij.openapi.fileChooser.FileChooserDescriptor
import com.intellij.openapi.options.ConfigurationException
import com.intellij.openapi.options.SettingsEditor
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.TextFieldWithBrowseButton

import javax.swing.*

class AemGroovySettingsEditor(private val project: Project) : SettingsEditor<AemGroovyRunConfiguration>() {
    private var contentPane: JPanel? = null
    private var serverHost: JTextField? = null
    private var loginField: JTextField? = null
    private var passwordField: JPasswordField? = null
    private var scriptPath: TextFieldWithBrowseButton? = null

    override fun resetEditorFrom(s: AemGroovyRunConfiguration) {

    }

    @Throws(ConfigurationException::class)
    override fun applyEditorTo(s: AemGroovyRunConfiguration) {

    }

    override fun createEditor(): JComponent {
        val fileChooserDescriptor = FileChooserDescriptor(true, false, false, false, false, false)
        scriptPath!!.addBrowseFolderListener("title", "desc", project, fileChooserDescriptor)
        return contentPane!!
    }

    private fun createUIComponents() {
        // TODO: place custom component creation code here
    }
}
