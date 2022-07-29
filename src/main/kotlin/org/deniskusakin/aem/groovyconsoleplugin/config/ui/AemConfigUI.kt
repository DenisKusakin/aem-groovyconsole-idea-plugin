package org.deniskusakin.aem.groovyconsoleplugin.config.ui

import java.util.*

/**
 * User: Andrey Bardashevsky
 * Date/Time: 29.07.2022 23:26
 */
data class AemConfigUI(
    var id: String = UUID.randomUUID().toString(),
    var name: String = "",
    var url: String = "",
    var user: String = "",
    var password: String = ""
)