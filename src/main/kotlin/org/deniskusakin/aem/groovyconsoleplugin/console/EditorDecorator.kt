package org.deniskusakin.aem.groovyconsoleplugin.console

import com.intellij.openapi.actionSystem.ActionManager
import com.intellij.openapi.actionSystem.DefaultActionGroup
import com.intellij.openapi.editor.impl.EditorHeaderComponent
import com.intellij.openapi.fileEditor.FileEditor
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.Key
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.ui.EditorNotifications
import org.deniskusakin.aem.groovyconsoleplugin.actions.AemGrExecuteAction
import org.deniskusakin.aem.groovyconsoleplugin.actions.AemGrSelectServerAction
import javax.swing.JComponent

class EditorDecorator(private val project: Project) : EditorNotifications.Provider<JComponent>() {
    companion object {
        private val myKey = Key.create<JComponent>("aem.groovy.console.toolbar")
    }

    override fun getKey(): Key<JComponent> = myKey

    override fun createNotificationPanel(file: VirtualFile, fileEditor: FileEditor): JComponent? {
        val actionGroup = DefaultActionGroup(AemGrExecuteAction(), AemGrSelectServerAction())
        val menu = ActionManager.getInstance().createActionToolbar("AemGroovyConsole", actionGroup, true)

        return EditorHeaderComponent().apply {
            add(menu.component)
        }
    }
}