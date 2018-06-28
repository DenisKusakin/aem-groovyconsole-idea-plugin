package org.deniskusakin.aem.groovyconsoleplugin.actions

import com.intellij.icons.AllIcons
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent

/**
 * @author Denis_Kusakin. 6/28/2018.
 */
class AemGrSelectServerAction : AnAction("Title", "Description", AllIcons.Nodes.Module) {
    override fun actionPerformed(e: AnActionEvent?) {

    }

    override fun displayTextInToolbar(): Boolean = true

    override fun update(e: AnActionEvent?) {
        super.update(e)
    }
}