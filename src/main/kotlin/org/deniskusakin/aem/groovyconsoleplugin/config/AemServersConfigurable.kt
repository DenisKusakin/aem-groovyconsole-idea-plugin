package org.deniskusakin.aem.groovyconsoleplugin.config

import com.intellij.openapi.components.ServiceManager
import com.intellij.openapi.options.Configurable
import com.intellij.ui.*
import com.intellij.ui.components.JBList
import com.intellij.ui.layout.CCFlags
import com.intellij.ui.layout.LCFlags
import com.intellij.ui.layout.panel
import java.awt.CardLayout
import com.intellij.openapi.project.Project
import org.deniskusakin.aem.groovyconsoleplugin.services.PersistentStateService

import javax.swing.*

class AemServersConfigurable(val project: Project) : Configurable {
    private val component: JComponent
    //    private val myAEMServers = mutableListOf<AemServerConfig>()
    private val myAEMServersEditors = mutableListOf<AemServerEditor>()
    private val serversListComponent = JBList<AemServerConfigUI>()
    private val myServerEditor = JPanel(CardLayout())

    data class AemServerEditor(val name: JTextField, val url: JTextField, val login: JTextField, val password: JTextField)

    init {
//        myAEMServersEditors.forEach {
//            myServerEditor.add(getEditor(it), it.name)
//        }
//        myServerEditor.doLayout()
//        if (myAEMServers.firstOrNull() != null) {
//            (myServerEditor.layout as CardLayout).show(myServerEditor, myAEMServers.first().name)
//        }

        component = panel {
            serversListComponent.emptyText.text = "No AEM servers"
            serversListComponent.cellRenderer = CellRenderer()
            serversListComponent.model = CollectionListModel(emptyList())
            serversListComponent.selectionModel.addListSelectionListener {
                val selected = serversListComponent.selectedValue
                if (selected != null) {
                    (myServerEditor.layout as CardLayout).show(myServerEditor, selected.oldName)
                }
            }
            val toolbarDecorator = ToolbarDecorator.createDecorator(serversListComponent).disableUpDownActions()
            toolbarDecorator.setAddAction {
                addNewServer()
            }
            toolbarDecorator.setRemoveAction { removeServer() }
            row {
                toolbarDecorator.createPanel()(CCFlags.grow, CCFlags.push)
            }
            row {
                myServerEditor(CCFlags.grow)
            }
        }
    }

    private fun getNewServerName(s: String = "unnamed", i: Int = 1): String {
        val exist = myAEMServersEditors.find { it.name.text == s } != null
        return if (!exist)
            s
        else
            getNewServerName("unnamed ($i)", i + 1)
    }

    private fun addNewServer() {
        val newServerName = getNewServerName()
        val newAemServer = AemServerConfigUI(name = newServerName, url = "", login = "", password = "")
        val (newServerEditor, editor) = getEditor(newAemServer)
        myAEMServersEditors += editor
        (serversListComponent.model as CollectionListModel).add(newAemServer)
        myServerEditor.add(newServerEditor, newServerName)
        serversListComponent.selectedIndex = (serversListComponent.model as CollectionListModel).size - 1
    }

    private fun removeServer() {
        val selected = serversListComponent.selectedValue
        val selectedIndex = serversListComponent.selectedIndex
        if (selected != null) {
            val model = serversListComponent.model as CollectionListModel
            model.remove(selected)
            myAEMServersEditors.removeAt(selectedIndex)
            if (model.size > 0) {
                serversListComponent.setSelectedValue(model.getElementAt(selectedIndex - 1), true)
            }
        }
    }

    class CellRenderer : ColoredListCellRenderer<AemServerConfigUI>() {
        override fun customizeCellRenderer(list: JList<out AemServerConfigUI>, value: AemServerConfigUI?, index: Int, selected: Boolean, hasFocus: Boolean) {
            append(value?.name ?: "")
        }

    }

    private fun getEditor(aemServerConfig: AemServerConfigUI): Pair<JComponent, AemServerEditor> {
        val name = JTextField(aemServerConfig.name)
        val url = JTextField(aemServerConfig.url)
        val login = JTextField(aemServerConfig.login)
        val password = JPasswordField(aemServerConfig.password)
        val editor = AemServerEditor(name = name, url = url, login = login, password = password)
        val panel = panel(LCFlags.fill) {
            row(label = "Server Name") {
                name(CCFlags.grow, CCFlags.push)
            }
            row(label = "URL") {
                url(CCFlags.grow, CCFlags.push)
            }
            row(label = "Login") {
                login(CCFlags.grow, CCFlags.push)
            }
            row(label = "Password") {
                password(CCFlags.grow, CCFlags.push)
            }
        }
        return Pair(panel, editor)
    }

    override fun getDisplayName(): String? {
        return "AEM Servers"
    }

    override fun createComponent(): JComponent? {
        return component
    }

    override fun isModified(): Boolean {
        val service = ServiceManager.getService(project, PersistentStateService::class.java)
        val persistedData = service.getAEMServers()
        val actualData: List<AemServerConfigUI> = getDataFromEditors()
        val isModified = actualData != persistedData.map { it.toUIRepresentation() }
        if (isModified) {
            val model = (serversListComponent.model as CollectionListModel)
            //TODO: This implementation is too complicated and looks terrible. How it could be improved?
            model.items.forEachIndexed { index, aemServerConfig ->
                val actualItem = actualData[index]
                if (aemServerConfig.name != actualItem.name) {
                    aemServerConfig.name = actualItem.name
                    model.contentsChanged(aemServerConfig)
                }
            }
        }
        return isModified
    }

    private fun getDataFromEditors(): List<AemServerConfigUI> {
        return myAEMServersEditors.map { AemServerConfigUI(name = it.name.text, url = it.url.text, login = it.login.text, password = it.password.text) }
    }

    override fun apply() {
        val service = ServiceManager.getService(project, PersistentStateService::class.java)
        service.setAEMServers(getDataFromEditors().map { it.toAemServerConfig() })
    }

    private fun resetData(aemServers: Collection<AemServerConfigUI>) {
        var selectedIndex = serversListComponent.selectedIndex
        myAEMServersEditors.clear()
        myServerEditor.removeAll()
        val model = (serversListComponent.model as CollectionListModel)
        model.removeAll()

        aemServers.forEach {
            val (component, editor) = getEditor(it)
            myAEMServersEditors += editor
            myServerEditor.add(component, it.name)
            model.add(it)
        }

        if (selectedIndex == -1) {
            selectedIndex = 0
        }
        if (selectedIndex > aemServers.size - 1) {
            selectedIndex = aemServers.size - 1
        }
        serversListComponent.selectedIndex = selectedIndex
        myServerEditor.doLayout()
        if (myAEMServersEditors.getOrNull(selectedIndex) != null) {
            (myServerEditor.layout as CardLayout).show(myServerEditor, myAEMServersEditors[selectedIndex].name.text)
        }
    }

    override fun reset() {
        val service = ServiceManager.getService(project, PersistentStateService::class.java)
        resetData(service.getAEMServers().map { it.toUIRepresentation() })
    }

    data class AemServerConfigUI(var name: String = "", var url: String = "", var login: String = "", var password: String = "", var oldName: String = name)

    private fun AemServerConfigUI.toAemServerConfig(): PersistentStateService.AemServerConfig {
        return PersistentStateService.AemServerConfig(name = this.name, url = this.url, login = this.login, password = this.password)
    }

    private fun PersistentStateService.AemServerConfig.toUIRepresentation(): AemServerConfigUI {
        return AemServerConfigUI(name = this.name, url = this.url, login = this.login, password = this.password)
    }
}
