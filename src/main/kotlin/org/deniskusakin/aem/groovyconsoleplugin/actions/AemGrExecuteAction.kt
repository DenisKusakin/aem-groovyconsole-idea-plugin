package org.deniskusakin.aem.groovyconsoleplugin.actions

import com.intellij.icons.AllIcons
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.CommonDataKeys
import org.deniskusakin.aem.groovyconsoleplugin.console.AEMGroovyConsole

class AemGrExecuteAction : AnAction(AllIcons.Toolwindows.ToolWindowRun) {
    override fun actionPerformed(e: AnActionEvent) {
        val project = e.project
        val editor = CommonDataKeys.EDITOR.getData(e.dataContext)
        val virtualFile = CommonDataKeys.VIRTUAL_FILE.getData(e.dataContext)
        if (project == null || editor == null || virtualFile == null) return

        AEMGroovyConsole.getOrCreateConsole(project, virtualFile)
    }
}