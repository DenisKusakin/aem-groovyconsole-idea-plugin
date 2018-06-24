package org.deniskusakin.aem.groovyconsoleplugin.config

import com.intellij.openapi.options.Configurable
import com.intellij.openapi.options.ConfigurationException
import com.intellij.ui.*
import com.intellij.ui.components.JBList
import com.intellij.ui.layout.CCFlags
import com.intellij.ui.layout.LCFlags
import com.intellij.ui.layout.panel
import java.awt.CardLayout

import javax.swing.*

class AemServersConfigurable : Configurable {
    private val component: JComponent
    private val myAEMServers = mutableListOf(
            AemServerConfig(name = "Server 1", url = "test", login = "log", password = "qqq"),
            AemServerConfig(name = "Server 2", url = "test2", login = "hfgh", password = "fdsf")
    )
    private val serversListComponent = JBList<AemServerConfig>()
    private val myServerEditor = JPanel(CardLayout())

    init {
        myAEMServers.forEach {
            myServerEditor.add(getEditor(it), it.name)
        }
        myServerEditor.doLayout()
        if (myAEMServers.firstOrNull() != null) {
            (myServerEditor.layout as CardLayout).show(myServerEditor, myAEMServers.first().name)
        }

        component = panel {
            serversListComponent.emptyText.text = "No AEM servers"
            serversListComponent.cellRenderer = CellRenderer()
            serversListComponent.model = CollectionListModel(myAEMServers)
            serversListComponent.selectionModel.addListSelectionListener {
                val selected = serversListComponent.selectedValue
                if (selected != null) {
                    (myServerEditor.layout as CardLayout).show(myServerEditor, selected.name)
                }
            }
            val toolbarDecorator = ToolbarDecorator.createDecorator(serversListComponent).disableUpDownActions()
            toolbarDecorator.setAddAction({
                addNewServer()
            })
            row {
                toolbarDecorator.createPanel()(CCFlags.grow, CCFlags.push)
            }
            row {
                myServerEditor()
            }
        }
    }

    private fun getNewServerName(s: String = "unnamed", i: Int = 1): String {
        val exist = myAEMServers.find { it.name == s } != null
        return if (!exist)
            s
        else
            getNewServerName("unnamed ($i)", i + 1)
    }

    private fun addNewServer() {
        val newServerName = getNewServerName()
        val newAemServer = AemServerConfig(name = newServerName, url = "", login = "", password = "")
        myAEMServers += newAemServer
        (serversListComponent.model as CollectionListModel).add(newAemServer)
        myServerEditor.add(getEditor(newAemServer), newServerName)
        serversListComponent.selectedIndex = (serversListComponent.model as CollectionListModel).size - 1
    }

    class CellRenderer : ColoredListCellRenderer<AemServerConfig>() {
        override fun customizeCellRenderer(list: JList<out AemServerConfig>, value: AemServerConfig?, index: Int, selected: Boolean, hasFocus: Boolean) {
            append(value?.name ?: "")
        }

    }

    private fun getEditor(aemServerConfig: AemServerConfig): JComponent {
        return panel(LCFlags.fill) {
            val name = JTextField(aemServerConfig.name)
            val url = JTextField(aemServerConfig.url)
            val login = JTextField(aemServerConfig.login)
            val password = JPasswordField(aemServerConfig.password)
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
    }

    override fun getDisplayName(): String? {
        return "AEM Servers"
    }

    override fun createComponent(): JComponent? {
        return component
    }

    override fun isModified(): Boolean {
        return false
    }

    @Throws(ConfigurationException::class)
    override fun apply() {

    }

    override fun reset() {

    }
}
