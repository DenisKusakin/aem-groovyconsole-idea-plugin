package org.deniskusakin.aem.groovyconsoleplugin.dsl

import org.deniskusakin.aem.groovyconsoleplugin.services.AemGroovyScriptsDetectionService
import org.jetbrains.plugins.groovy.lang.psi.GroovyFile

/**
 * User: Andrey Bardashevsky
 * Date/Time: 01.08.2022 19:43
 */
object Utils {
    fun GroovyFile.isAemFile(): Boolean {
        val virtualFile = this.originalFile.virtualFile ?: return false

        return AemGroovyScriptsDetectionService.isAemGroovyFile(virtualFile.path, this.project)
    }
}