package org.deniskusakin.aem.groovyconsoleplugin.services

import com.intellij.openapi.project.Project

object AemGroovyScriptsDetectionService {
    fun isAemGroovyFile(filePath: String, project: Project): Boolean {
        return filePath.contains("groovyconsole") || RootFoldersService.getInstance(project)
                ?.myState?.roots?.any { filePath.startsWith("$it/") } ?: false
    }

    fun isGroovyScriptsRoot(filePath: String, project: Project): Boolean {
        return RootFoldersService.getInstance(project)
                ?.myState?.roots?.contains(filePath) ?: false
    }
}