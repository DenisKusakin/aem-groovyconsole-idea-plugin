// Copyright 2000-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.deniskusakin.aem.groovyconsoleplugin.dsl

import com.intellij.psi.CommonClassNames.JAVA_LANG_OBJECT
import com.intellij.psi.CommonClassNames.JAVA_UTIL_MAP
import com.intellij.psi.PsiClass
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiType
import com.intellij.psi.ResolveState
import com.intellij.psi.scope.PsiScopeProcessor
import com.intellij.util.Processor
import org.deniskusakin.aem.groovyconsoleplugin.utils.AemFileTypeUtils.isAemFile
import org.jetbrains.plugins.groovy.builder.setContainingClass
import org.jetbrains.plugins.groovy.lang.psi.impl.synthetic.GrLightMethodBuilder
import org.jetbrains.plugins.groovy.lang.psi.util.GroovyCommonClassNames.GROOVY_LANG_CLOSURE
import org.jetbrains.plugins.groovy.lang.resolve.NonCodeMembersContributor
import org.jetbrains.plugins.groovy.lang.resolve.ResolveUtil
import org.jetbrains.plugins.groovy.lang.resolve.delegatesTo.DELEGATES_TO_TYPE_KEY
import org.jetbrains.plugins.groovy.lang.resolve.shouldProcessDynamicMethods

abstract class BuilderMethodsContributor : NonCodeMembersContributor() {
    override fun processDynamicElements(
        qualifierType: PsiType,
        clazz: PsiClass?,
        processor: PsiScopeProcessor,
        place: PsiElement,
        state: ResolveState
    ) {
        if (clazz == null) return
        if (!place.isAemFile()) return
        val name = ResolveUtil.getNameHint(processor) ?: return
        if (!shouldProcessDynamicMethods(processor)) return
        processDynamicMethods(clazz, name, place) { e: PsiElement? ->
            processor.execute(
                e!!, state
            )
        }
    }

    private fun processDynamicMethods(
        clazz: PsiClass,
        name: String,
        place: PsiElement,
        processor: Processor<in PsiElement?>
    ): Boolean {
        @Suppress("JoinDeclarationAndAssignment")
        var res: GrLightMethodBuilder

        // ()
        res = createMethod(name, clazz, place)
        if (!processor.process(res)) return false

        // (Closure)
        res = createMethod(name, clazz, place)
        res.addAndGetParameter("body", GROOVY_LANG_CLOSURE)
            .putUserData(DELEGATES_TO_TYPE_KEY, getFqnClassName())
        if (!processor.process(res)) return false

        // (Object, Closure)
        res = createMethod(name, clazz, place)
        res.addParameter("value", JAVA_LANG_OBJECT)
        res.addAndGetParameter("body", GROOVY_LANG_CLOSURE)
            .putUserData(DELEGATES_TO_TYPE_KEY, getFqnClassName())
        if (!processor.process(res)) return false

        // (Map, Closure)
        res = createMethod(name, clazz, place)
        res.addParameter("properties", JAVA_UTIL_MAP)
        res.addAndGetParameter("body", GROOVY_LANG_CLOSURE)
            .putUserData(DELEGATES_TO_TYPE_KEY, getFqnClassName())
        if (!processor.process(res)) return false

        // (Map)
        // (Map, Object)
        // (Map, Object, Closure)
        res = createMethod(name, clazz, place)
        res.addParameter("properties", JAVA_UTIL_MAP)
        res.addOptionalParameter("value", JAVA_LANG_OBJECT)
        res.addAndGetOptionalParameter("body", GROOVY_LANG_CLOSURE)
            .putUserData(DELEGATES_TO_TYPE_KEY, getFqnClassName())
        for (method in res.reflectedMethods) {
            if (!processor.process(method)) return false
        }

        // (Object)
        // (Object, Map)
        // (Object, Map, Closure)
        res = createMethod(name, clazz, place)
        res.addParameter("value", JAVA_LANG_OBJECT)
        res.addOptionalParameter("properties", JAVA_UTIL_MAP)
        res.addAndGetOptionalParameter("body", GROOVY_LANG_CLOSURE)
            .putUserData(DELEGATES_TO_TYPE_KEY, getFqnClassName())
        for (method in res.reflectedMethods) {
            if (!processor.process(method)) return false
        }
        return true
    }

    private fun createMethod(name: String, clazz: PsiClass, place: PsiElement): GrLightMethodBuilder {
        val res = GrLightMethodBuilder(place.manager, name)
        res.setReturnType(JCR_NODE_CLASS, place.resolveScope)
        res.originInfo = getOriginInfo()
        setContainingClass(res, clazz)
        return res
    }

    abstract fun getFqnClassName(): String

    abstract fun getOriginInfo(): String

    companion object {
        private const val JCR_NODE_CLASS = "javax.jcr.Node"
    }
}