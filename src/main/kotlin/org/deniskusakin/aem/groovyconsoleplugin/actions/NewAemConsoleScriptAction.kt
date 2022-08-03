package org.deniskusakin.aem.groovyconsoleplugin.actions

import com.intellij.ide.actions.CreateFileFromTemplateDialog
import com.intellij.ide.actions.JavaCreateTemplateInPackageAction
import com.intellij.openapi.actionSystem.DataContext
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiDirectory
import com.intellij.psi.PsiElement
import com.intellij.util.IncorrectOperationException
import icons.JetgroovyIcons
import org.deniskusakin.aem.groovyconsoleplugin.lang.AemConsoleScriptType
import org.jetbrains.plugins.groovy.actions.GroovyTemplatesFactory
import org.jetbrains.plugins.groovy.lang.psi.GroovyFile

class NewAemConsoleScriptAction : JavaCreateTemplateInPackageAction<GroovyFile>(
    "Aem Console Script",
    "Creates a new Aem Console Script",
    JetgroovyIcons.Groovy.GroovyFile,
    false
) {
    override fun buildDialog(project: Project, directory: PsiDirectory, builder: CreateFileFromTemplateDialog.Builder) {
        builder
            .setTitle("Enter a name for a new Aem Console Script")
            .addKind(
                "New Aem Console Script",
                JetgroovyIcons.Groovy.GroovyFile,
                AemConsoleScriptType.TEMPLATE
            )
    }

    override fun isAvailable(dataContext: DataContext?): Boolean {
        return true
    }

    override fun getActionName(directory: PsiDirectory?, newName: String, templateName: String?): String =
        "AemConsole Script"

    override fun doCreate(dir: PsiDirectory, newName: String, templateName: String): GroovyFile {
        val file = GroovyTemplatesFactory.createFromTemplate(
            dir,
            newName,
            "$newName.${AemConsoleScriptType.EXTENSION}",
            templateName,
            true
        )

        if (file is GroovyFile) return file

        val description = file.fileType.description

        throw IncorrectOperationException(
            "*.${AemConsoleScriptType.EXTENSION} files are mapped to '${description}'.\nYou can map them to Groovy in Settings | File types"
        )
    }

    override fun getNavigationElement(createdElement: GroovyFile): PsiElement? = createdElement.lastChild
}