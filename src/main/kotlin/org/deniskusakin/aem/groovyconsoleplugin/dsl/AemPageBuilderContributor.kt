package org.deniskusakin.aem.groovyconsoleplugin.dsl

import com.icfolson.aem.groovy.extension.builders.PageBuilder

class AemPageBuilderContributor : BuilderMethodsContributor() {
    override fun getParentClassName(): String = FQN

    override fun getFqnClassName(): String = FQN

    override fun getOriginInfo(): String = ORIGIN_INFO

    companion object {
        private val FQN = PageBuilder::class.java.name

        private const val ORIGIN_INFO: String = "via AemPageBuilder"
    }
}