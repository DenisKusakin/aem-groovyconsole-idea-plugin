package org.deniskusakin.aem.groovyconsoleplugin.config

import com.intellij.util.messages.Topic

interface SettingsChangedNotifier {
    companion object {
        val TOPIC: Topic<SettingsChangedNotifier> = Topic.create("AEM Servers configuration changed", SettingsChangedNotifier::class.java)
    }

    fun settingsChanged()
}