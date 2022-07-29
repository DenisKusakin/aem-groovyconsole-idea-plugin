package org.deniskusakin.aem.groovyconsoleplugin.config.ui

/**
 * User: Andrey Bardashevsky
 * Date/Time: 28.07.2022 23:40
 */
data class AemServerTableItem(private var initialConfig: AemConfigUI) {

    val config: AemConfigUI = initialConfig.copy()

    var id: String
        get() = config.id
        set(value) {
            config.id = value
        }

    var name: String
        get() = config.name
        set(value) {
            config.name = value
        }

    var url: String
        get() = config.url
        set(value) {
            config.url = value
        }

    var user: String
        get() = config.user
        set(value) {
            config.user = value
        }

    var password: String
        get() = config.password
        set(value) {
            config.password = value
        }

    fun isModified(): Boolean = config != initialConfig

    fun applyChanges() {
        initialConfig = config.copy()
    }

    fun duplicate(): AemServerTableItem {
        val item = empty()

        item.id = config.id
        item.name = config.name
        item.url = config.url
        item.user = config.user
        item.password = config.password

        return item
    }

    companion object {
        fun empty(): AemServerTableItem = AemServerTableItem(AemConfigUI())
    }
}