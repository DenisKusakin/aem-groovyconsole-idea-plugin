package org.deniskusakin.aem.groovyconsoleplugin.services

import com.intellij.openapi.components.PersistentStateComponent
import com.intellij.openapi.components.ServiceManager
import com.intellij.openapi.components.State
import com.intellij.openapi.project.Project

/**
 * @author Denis_Kusakin. 6/25/2018.
 */
@State(
        name = "AemGroovyScriptsRoots"
)
class RootFoldersService : PersistentStateComponent<RootFoldersService.State> {
    var myState: State = State()

    override fun getState(): State? {
        return myState
    }

    override fun loadState(state: State) {
        myState = state
    }

    fun addRoot(path: String) {
        if (!myState.roots.contains(path)) {
            myState.roots.add(path)
        }
    }

    fun removeRoot(path: String) {
        if (myState.roots.contains(path)) {
            myState.roots.remove(path)
        }
    }

    data class State(var roots: MutableList<String> = mutableListOf())

    companion object {
        fun getInstance(project: Project): RootFoldersService? {
            return ServiceManager.getService(project, RootFoldersService::class.java)
        }
    }
}