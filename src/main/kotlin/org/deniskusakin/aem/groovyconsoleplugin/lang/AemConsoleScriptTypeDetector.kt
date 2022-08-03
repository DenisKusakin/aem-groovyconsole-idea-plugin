package org.deniskusakin.aem.groovyconsoleplugin.lang

import org.deniskusakin.aem.groovyconsoleplugin.utils.AemFileTypeUtils.isAemFile
import org.jetbrains.plugins.groovy.extensions.GroovyScriptTypeDetector
import org.jetbrains.plugins.groovy.lang.psi.GroovyFile

/**
 * User: Andrey Bardashevsky
 * Date/Time: 03.08.2022 02:42
 */
class AemConsoleScriptTypeDetector: GroovyScriptTypeDetector(AemConsoleScriptType.instance) {
    override fun isSpecificScriptFile(script: GroovyFile): Boolean = script.isAemFile()
}