package org.deniskusakin.aem.groovyconsoleplugin.console

import com.intellij.openapi.project.Project
import com.intellij.openapi.util.Key
import com.intellij.openapi.vfs.VirtualFile
import org.deniskusakin.aem.groovyconsoleplugin.services.PersistentStateService
import org.deniskusakin.aem.groovyconsoleplugin.services.model.AemServerConfig

/**
 * User: Andrey Bardashevsky
 * Date/Time: 30.07.2022 15:10
 */
object GroovyConsoleUserData {

    private val GROOVY_CONSOLE_CURRENT_SERVER_ID = Key.create<Long>("AEMGroovyConsoleCurrentServerId")

    private fun VirtualFile.getCurrentAemServerId(): Long? = getUserData(GROOVY_CONSOLE_CURRENT_SERVER_ID)

    fun VirtualFile.setCurrentAemServerId(id: Long?) {
        putUserData(GROOVY_CONSOLE_CURRENT_SERVER_ID, id)
    }

    fun VirtualFile.getCurrentAemConfig(
        project: Project
    ): AemServerConfig? {
        val serverId = getCurrentAemServerId()

        if (serverId != null) {
            val stateService = PersistentStateService.getInstance(project)

            return stateService.findById(serverId)
        }

        return null
    }
}