package org.deniskusakin.aem.groovyconsoleplugin.dsl

import org.deniskusakin.aem.groovyconsoleplugin.utils.AemFileTypeUtils.isAemFile
import org.jetbrains.plugins.groovy.lang.psi.GroovyFile
import org.jetbrains.plugins.groovy.lang.resolve.imports.GrImportContributor
import org.jetbrains.plugins.groovy.lang.resolve.imports.GroovyImport
import org.jetbrains.plugins.groovy.lang.resolve.imports.StarImport


/**
 * User: Andrey Bardashevsky
 * Date/Time: 01.08.2022 14:27
 */
class AemImportContributor : GrImportContributor {

    private val packageImports = listOf(
        "com.day.cq.dam.api",
        "com.day.cq.replication",
        "com.day.cq.search",
        "com.day.cq.tagging",
        "com.day.cq.wcm.api",
        "javax.jcr",
        "org.apache.sling.api",
        "org.apache.sling.api.resource"
    )

    private val imports: List<GroovyImport> by lazy { packageImports.map(::StarImport) }

    override fun getFileImports(file: GroovyFile): List<GroovyImport> = if (file.isAemFile()) imports else emptyList()
}