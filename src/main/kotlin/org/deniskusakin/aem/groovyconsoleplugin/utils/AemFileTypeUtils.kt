package org.deniskusakin.aem.groovyconsoleplugin.utils

import com.intellij.openapi.vfs.VirtualFile
import com.intellij.psi.PsiElement
import org.deniskusakin.aem.groovyconsoleplugin.lang.AemConsoleScriptType
import org.jetbrains.plugins.groovy.lang.psi.GroovyFile

/**
 * User: Andrey Bardashevsky
 * Date/Time: 01.08.2022 19:43
 */
@Suppress("unused")
object AemFileTypeUtils {

    fun VirtualFile.isAemFile(): Boolean {
        return this.extension == AemConsoleScriptType.EXTENSION
    }

    fun PsiElement.isAemFile(): Boolean {
        return this.containingFile.virtualFile?.isAemFile() ?: return false
    }

    fun GroovyFile.isAemFile(): Boolean {
        return this.originalFile.virtualFile?.isAemFile() ?: return false
    }
}