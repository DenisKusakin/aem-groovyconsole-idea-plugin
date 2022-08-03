package org.deniskusakin.aem.groovyconsoleplugin.dsl

import com.icfolson.aem.groovy.extension.builders.NodeBuilder
import com.intellij.openapi.application.PathManager
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.text.StringUtil
import com.intellij.openapi.vfs.JarFileSystem
import com.intellij.openapi.vfs.VfsUtil
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.psi.NonClasspathClassFinder
import com.intellij.psi.PsiClass
import com.intellij.psi.search.GlobalSearchScope
import com.intellij.psi.search.NonClasspathDirectoriesScope

/**
 * User: Andrey Bardashevsky
 * Date/Time: 01.08.2022 19:24
 *
 */
class AemScriptExtensionClassFinder(project: Project) : NonClasspathClassFinder(project) {

    override fun calcClassRoots(): List<VirtualFile> = roots

    override fun findClass(qualifiedName: String, scope: GlobalSearchScope): PsiClass? {
        val packageName = StringUtil.getPackageName(qualifiedName)

        return if (NodeBuilder::class.java.`package`.name == packageName) {
            super.findClass(qualifiedName, scope.uniteWith(searchScope))
        } else {
            null
        }
    }

    companion object {
        private val roots = buildClassesRoots()

        val searchScope = NonClasspathDirectoriesScope.compose(roots)

        private fun buildClassesRoots(): List<VirtualFile> {
            val jarForClass = PathManager.getJarForClass(NodeBuilder::class.java)

            if (jarForClass != null) {
                val virtualFile = VfsUtil.findFileByIoFile(jarForClass.toFile(), true)
                if (virtualFile != null) {
                    val classRoot = JarFileSystem.getInstance().getRootByLocal(virtualFile)

                    if (classRoot != null) {
                        return listOf(classRoot)
                    }
                }
            }
            return emptyList()
        }
    }
}