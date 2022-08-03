package org.deniskusakin.aem.groovyconsoleplugin.dsl

import com.intellij.psi.JavaPsiFacade
import com.intellij.psi.PsiType
import org.deniskusakin.aem.groovyconsoleplugin.utils.AemFileTypeUtils.isAemFile
import org.jetbrains.plugins.groovy.lang.psi.api.GrFunctionalExpression
import org.jetbrains.plugins.groovy.lang.psi.api.statements.arguments.GrArgumentList
import org.jetbrains.plugins.groovy.lang.psi.api.statements.expressions.GrMethodCall
import org.jetbrains.plugins.groovy.lang.psi.api.statements.expressions.GrReferenceExpression
import org.jetbrains.plugins.groovy.lang.psi.api.statements.params.GrParameter
import org.jetbrains.plugins.groovy.lang.psi.impl.statements.expressions.TypesUtil
import org.jetbrains.plugins.groovy.lang.psi.typeEnhancers.AbstractClosureParameterEnhancer
import org.jetbrains.plugins.groovy.lang.psi.util.isCompileStatic

/**
 * User: Andrey Bardashevsky
 * Date/Time: 01.08.2022 21:33
 */
class ClosureParameterEnhancer : AbstractClosureParameterEnhancer() {
    private val simpleTypes: Map<String, String> = mapOf(
        Pair("withBinary", "javax.jcr.Binary")
    )

    override fun getClosureParameterType(expression: GrFunctionalExpression, index: Int): PsiType? {
        if (!expression.isAemFile()) return null

        if (isCompileStatic(expression)) {
            return null
        }

        var parent = expression.parent
        if (parent is GrArgumentList) parent = parent.getParent()
        if (parent !is GrMethodCall) {
            return null
        }

        val methodName = findMethodName(parent)

        val invokedExpression = parent.invokedExpression as? GrReferenceExpression ?: return null

        val qualifier = invokedExpression.qualifierExpression ?: return null

        qualifier.type ?: return null

        val params: Array<GrParameter> = expression.allParameters

        if (params.size == 1 && simpleTypes.containsKey(methodName)) {
            val typeText = simpleTypes[methodName]
            return if (typeText!!.indexOf('<') < 0) {
                TypesUtil.createTypeByFQClassName(typeText, expression)
            } else {
                JavaPsiFacade.getElementFactory(expression.project).createTypeFromText(typeText, expression)
            }
        }

        return null
    }

    private fun findMethodName(methodCall: GrMethodCall): String? {
        val expression = methodCall.invokedExpression
        return if (expression is GrReferenceExpression) {
            expression.referenceName
        } else null
    }
}