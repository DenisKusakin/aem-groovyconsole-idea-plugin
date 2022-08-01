package org.deniskusakin.aem.groovyconsoleplugin.dsl

import org.deniskusakin.aem.groovyconsoleplugin.dsl.Utils.isAemFile
import org.jetbrains.plugins.groovy.lang.psi.GroovyFile
import org.jetbrains.plugins.groovy.lang.resolve.imports.GrImportContributor
import org.jetbrains.plugins.groovy.lang.resolve.imports.GroovyImport
import org.jetbrains.plugins.groovy.lang.resolve.imports.RegularImport


/**
 * User: Andrey Bardashevsky
 * Date/Time: 01.08.2022 14:27
 */
class AemBuildersImportContributor : GrImportContributor {

    private val classImports = listOf(
        "com.icfolson.aem.groovy.extension.builders.PageBuilder",
        "com.icfolson.aem.groovy.extension.builders.NodeBuilder"
    )

    private val imports: List<GroovyImport> by lazy { classImports.map(::RegularImport) }

    override fun getFileImports(file: GroovyFile): List<GroovyImport> = if (file.isAemFile()) imports else emptyList()
}