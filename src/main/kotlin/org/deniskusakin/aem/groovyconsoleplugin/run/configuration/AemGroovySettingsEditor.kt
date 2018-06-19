package org.deniskusakin.aem.groovyconsoleplugin.run.configuration

import com.intellij.openapi.options.SettingsEditor
import com.intellij.openapi.ui.ComponentWithBrowseButton
import com.intellij.openapi.ui.LabeledComponent
import com.intellij.openapi.ui.TextFieldWithBrowseButton
import com.intellij.openapi.util.Factory
import javax.swing.JComponent
import javax.swing.JPanel

/**
 * @author Denis_Kusakin. 6/19/2018.
 */
class AemGroovySettingsEditor : SettingsEditor<AemGroovyRunConfiguration>() {
    private var myPanel: JPanel? = null
//    private var myMainClass: LabeledComponent<ComponentWithBrowseButton<*>>? = null

    override fun resetEditorFrom(s: AemGroovyRunConfiguration) {

    }

    override fun createEditor(): JComponent {
        return myPanel!!
    }

    override fun applyEditorTo(s: AemGroovyRunConfiguration) {

    }

    private fun createUIComponents() {
//        myMainClass = LabeledComponent()
//        myMainClass!!.setComponent(TextFieldWithBrowseButton())
    }
}