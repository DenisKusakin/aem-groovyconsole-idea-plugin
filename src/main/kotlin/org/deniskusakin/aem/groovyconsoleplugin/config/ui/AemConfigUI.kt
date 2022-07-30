package org.deniskusakin.aem.groovyconsoleplugin.config.ui

/**
 * User: Andrey Bardashevsky
 * Date/Time: 29.07.2022 23:26
 */
data class AemConfigUI(
    var id: Long = System.currentTimeMillis(),
    var name: String = "",
    var url: String = "",
    var user: String = "",
    var password: String = ""
)