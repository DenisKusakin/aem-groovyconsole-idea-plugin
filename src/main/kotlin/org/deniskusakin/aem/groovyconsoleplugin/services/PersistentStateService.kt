package org.deniskusakin.aem.groovyconsoleplugin.services

import com.intellij.openapi.components.PersistentStateComponent
import com.intellij.openapi.components.State

/**
 * @author Denis_Kusakin. 6/25/2018.
 */
@State(
        name = "AEMServers"
)
class PersistentStateService : PersistentStateComponent<PersistentStateService.State> {
    var myState: State = State()

    override fun getState(): State? {
        return myState
    }

    override fun loadState(state: State) {
        myState = state
    }

    fun getAEMServers(): Collection<AemServerConfig> {
        return myState.aemServers
    }

    fun setAEMServers(servers: List<AemServerConfig>) {
        myState = State(servers.toMutableList())
    }

    data class State(var aemServers: MutableList<AemServerConfig> = mutableListOf())
    data class AemServerConfig(var name: String = "", var url: String = "", var login: String = "", var password: String = "")
}