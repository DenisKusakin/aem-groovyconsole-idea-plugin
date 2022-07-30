package org.deniskusakin.aem.groovyconsoleplugin.services

import com.intellij.openapi.components.PersistentStateComponent
import com.intellij.openapi.components.State
import com.intellij.openapi.components.service
import com.intellij.openapi.project.Project
import org.deniskusakin.aem.groovyconsoleplugin.services.model.AemServerConfig

/**
 * @author Denis_Kusakin. 6/25/2018.
 */
@State(name = "AEMServers")
class PersistentStateService : PersistentStateComponent<PersistentStateService.State> {
    var myState: State = State()

    override fun getState(): State {
        return myState
    }

    override fun loadState(state: State) {
        myState = state
    }

    fun getAEMServers(): List<AemServerConfig> {
        return myState.aemServers
    }

    fun setAEMServers(servers: List<AemServerConfig>) {
        myState = State(servers.toMutableList())
    }

    fun findById(id: Long): AemServerConfig? = getAEMServers().find { it.id == id }

    data class State(var aemServers: MutableList<AemServerConfig> = mutableListOf())

    companion object {
        @JvmStatic
        fun getInstance(project: Project): PersistentStateService = project.service()
    }
}