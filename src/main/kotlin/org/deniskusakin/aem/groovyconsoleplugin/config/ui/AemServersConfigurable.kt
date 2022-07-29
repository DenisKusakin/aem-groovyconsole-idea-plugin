package org.deniskusakin.aem.groovyconsoleplugin.config.ui

import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.options.Configurable
import com.intellij.openapi.project.Project
import com.intellij.ui.ToolbarDecorator
import com.intellij.ui.layout.CCFlags
import com.intellij.ui.layout.panel
import com.intellij.util.PlatformIcons
import org.deniskusakin.aem.groovyconsoleplugin.config.SettingsChangedNotifier
import org.deniskusakin.aem.groovyconsoleplugin.services.PasswordsService
import org.deniskusakin.aem.groovyconsoleplugin.services.PersistentStateService
import javax.swing.JComponent


class AemServersConfigurable(private val project: Project) : Configurable {

    private val persistentStateService = project.getService(PersistentStateService::class.java)

    private val aemStoredServers: Collection<PersistentStateService.AemServerConfig>
        get() {
            return persistentStateService.getAEMServers()
        }

    private val aemConfigs: List<AemConfigUI>
        get() {
            val result = mutableListOf<AemConfigUI>()

            for (aemServer in aemStoredServers) {
                if (aemServer.id.isNotEmpty()) {
                    val credentials = PasswordsService.getCredentials(aemServer.id)

                    result.add(
                        AemConfigUI(
                            id = aemServer.id,
                            name = aemServer.name,
                            url = aemServer.url,
                            user = credentials?.userName.orEmpty(),
                            password = credentials?.getPasswordAsString().orEmpty()
                        )
                    )
                }
            }

            return result
        }

    private val table: AemServersTable = AemServersTable(aemConfigs.map { AemServerTableItem(it) })

    private fun addToolbarActions(toolbarDecorator: ToolbarDecorator) {
        toolbarDecorator.setAddAction {
            val model = AemServerTableItem.empty()

            if (AemServerEditDialog(project, model).showAndGet()) {
                table.model.addRow(model)
            }
        }

        toolbarDecorator.setRemoveAction {
            val selectedRow = table.selectedRow

            if (selectedRow >= 0) {
                table.model.removeRow(selectedRow)
            }
        }

        toolbarDecorator.setEditAction {
            editSelectedRow()
        }

        toolbarDecorator.addExtraAction(
            object : ToolbarDecorator.ElementActionButton("Duplicate", "Duplicate", PlatformIcons.COPY_ICON) {
                override fun actionPerformed(e: AnActionEvent) {
                    val selectedRow = table.selectedRow
                    if (selectedRow >= 0) {
                        val model = table.model.getItem(selectedRow)

                        table.model.addRow(model.duplicate())
                    }
                }
            }
        )
    }

    private fun editSelectedRow() {
        val selectedRow = table.selectedRow

        if (selectedRow >= 0) {
            val model = table.model.getItem(selectedRow)

            if (AemServerEditDialog(project, model).showAndGet()) {
                table.model.fireTableRowsUpdated(selectedRow, selectedRow)
            }
        }
    }

    override fun createComponent(): JComponent = panel {
        table.addDoubleClickListener { editSelectedRow() }

        val toolbarDecorator = ToolbarDecorator.createDecorator(table).disableUpDownActions()

        addToolbarActions(toolbarDecorator)

        row {
            toolbarDecorator.createPanel()(CCFlags.grow, CCFlags.push)
        }
    }

    override fun getDisplayName(): String {
        return "AEM Groovy Console"
    }

    override fun isModified(): Boolean {
        return table.isModified()
    }

    override fun apply() {
        val service = persistentStateService

        val items = table.model.items

        items.forEach { it.applyChanges() }

        service.setAEMServers(items.map { PersistentStateService.AemServerConfig(it.id, it.name, it.url) })

        items.forEach {
            PasswordsService.setCredentials(it.id, it.user, it.password)
        }

        project.messageBus.syncPublisher(SettingsChangedNotifier.TOPIC).settingsChanged()
    }

    override fun reset() {
        table.model.items = aemConfigs.map { AemServerTableItem(it) }
    }

}
