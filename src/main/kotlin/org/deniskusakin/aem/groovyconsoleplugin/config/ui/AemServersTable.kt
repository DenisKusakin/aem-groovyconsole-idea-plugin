package org.deniskusakin.aem.groovyconsoleplugin.config.ui

import com.intellij.icons.AllIcons
import com.intellij.ui.DoubleClickListener
import com.intellij.ui.table.TableView
import com.intellij.util.PlatformIcons
import com.intellij.util.ui.ColumnInfo
import com.intellij.util.ui.ListTableModel
import java.awt.Component
import java.awt.event.MouseEvent
import javax.swing.Icon
import javax.swing.JLabel
import javax.swing.JTable
import javax.swing.ListSelectionModel
import javax.swing.table.TableCellRenderer

class AemServersTable(tableItems: List<AemServerTableItem>) : TableView<AemServerTableItem>() {

    init {
        autoResizeMode = JTable.AUTO_RESIZE_SUBSEQUENT_COLUMNS
        showVerticalLines = false
        gridColor = foreground
        setSelectionMode(ListSelectionModel.SINGLE_SELECTION)
        setShowGrid(false)

        setModelAndUpdateColumns(
            ListTableModel(
                createInjectionColumnInfos(tableItems),
                tableItems,
                0
            )
        )
    }
    
    @Suppress("UNCHECKED_CAST")
    override fun getModel(): ListTableModel<AemServerTableItem> {
        return super.getModel() as ListTableModel<AemServerTableItem>
    }

    fun isModified(): Boolean {
        return model.items.any { it.isModified() }
    }

    fun addDoubleClickListener(listener: () -> Unit) {
        object : DoubleClickListener() {
            override fun onDoubleClick(e: MouseEvent): Boolean {
                val row = rowAtPoint(e.point)
                val column = columnAtPoint(e.point)

                if (row >= 0 && column >= 0) {
                    getSelectionModel().setSelectionInterval(row, row)

                    listener()
                }

                return false
            }
        }.installOn(this)
    }

    private fun createInjectionColumnInfos(items: List<AemServerTableItem>): Array<ColumnInfo<AemServerTableItem, AemServerTableItem>> {
        val nameCellRenderer: TableCellRenderer = createCellRenderer(text = { it.name })
        val urlCellRenderer: TableCellRenderer =
            createCellRenderer(text = { it.url }, icon = { PlatformIcons.WEB_ICON })

        val userCellRenderer: TableCellRenderer =
            createCellRenderer(text = { it.user }, icon = { AllIcons.General.User })

        val maxName = items.map { it.name }.maxByOrNull { it.length }
        val maxUrl = items.map { it.url }.maxByOrNull { it.length }
        val maxLogin = items.map { it.user }.maxByOrNull { it.length }

        return arrayOf(
            createColumnInfo(
                "Server Name",
                nameCellRenderer,
                maxName
            ) { o1, o2 -> o1.name.compareTo(o2.name) },
            createColumnInfo(
                "URL",
                urlCellRenderer,
                maxUrl
            ) { o1, o2 -> o1.url.compareTo(o2.url) },
            createColumnInfo(
                "User",
                userCellRenderer,
                maxLogin
            ) { o1, o2 -> o1.user.compareTo(o2.user) },
        )
    }

    private fun <T> createColumnInfo(
        name: String,
        renderer: TableCellRenderer,
        preferredStringWidthValue: String?,
        comparator: Comparator<T>
    ): ColumnInfo<T, T> {
        return object : ColumnInfo<T, T>(name) {

            override fun valueOf(config: T): T = config

            override fun getComparator(): Comparator<T> = comparator

            override fun getRenderer(config: T): TableCellRenderer = renderer

            override fun getPreferredStringValue(): String? = preferredStringWidthValue
        }
    }

    private fun createCellRenderer(
        text: (config: AemServerTableItem) -> String,
        icon: (() -> Icon)? = null
    ): TableCellRenderer {
        return object : TableCellRenderer {
            val myLabel = JLabel()

            override fun getTableCellRendererComponent(
                table: JTable,
                value: Any?,
                isSelected: Boolean,
                hasFocus: Boolean,
                row: Int,
                column: Int
            ): Component {
                if (value != null) {

                    myLabel.text = text(value as AemServerTableItem)

                    if (icon != null) {
                        myLabel.icon = icon()
                    }

                    myLabel.foreground = if (isSelected) table.selectionForeground else table.foreground
                    myLabel.background = if (isSelected) table.selectionBackground else table.background
                }

                return myLabel
            }
        }
    }
}