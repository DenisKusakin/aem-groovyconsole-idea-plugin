package org.deniskusakin.aem.groovyconsoleplugin.config.ui

/**
 * User: Andrey Bardashevsky
 * Date/Time: 29.07.2022 23:26
 */
data class AemServerConfigUI(
    var id: Long = newId(),
    var name: String = "",
    var url: String = "",
    var user: String = "",
    var password: String = ""
) {
    fun duplicate(): AemServerConfigUI = copy(id = newId())

    companion object {
        private fun newId(): Long = System.currentTimeMillis()
    }
}