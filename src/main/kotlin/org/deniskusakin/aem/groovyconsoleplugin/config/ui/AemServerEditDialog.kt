package org.deniskusakin.aem.groovyconsoleplugin.config.ui

import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.DialogWrapper
import com.intellij.openapi.ui.ValidationInfo
import com.intellij.ui.layout.PropertyBinding
import com.intellij.ui.layout.ValidationInfoBuilder
import com.intellij.ui.layout.panel
import com.intellij.ui.layout.withTextBinding
import javax.swing.JComponent
import javax.swing.JPasswordField

/**
 * User: Andrey Bardashevsky
 * Date/Time: 29.07.2022 15:45
 */
class AemServerEditDialog(project: Project, private val tableItem: AemServerConfigUI) : DialogWrapper(project) {

    private val urlRegex = Regex(
        "https?://(www\\.)?[-a-zA-Z0-9@:%._+~#=]{1,256}(\\.[a-zA-Z0-9()]{1,6})?\\b([-a-zA-Z0-9()@:%_+.~#?&/=]*)"
    )

    init {
        title = "Aem Server"

        init()
    }

    override fun createCenterPanel(): JComponent = panel {
        row("Server Name") {
            textField({ tableItem.name }, { tableItem.name = it })
                .withValidationOnInput { validateNotEmpty(it.text) }
                .withValidationOnApply { validateNotEmpty(it.text) }
                .focused()
        }

        row("URL") {
            textField({ tableItem.url }, { tableItem.url = it })
                .withValidationOnInput { validateUrl(it.text) }
                .withValidationOnApply { validateUrl(it.text) }
        }

        row("Username") {
            textField({ tableItem.user }, { tableItem.user = it })
                .withValidationOnInput { validateNotEmpty(it.text) }
                .withValidationOnApply { validateNotEmpty(it.text) }
        }

        row("Password") {
            val binding = PropertyBinding({ tableItem.password }, { tableItem.password = it })

            component(JPasswordField(binding.get(), 0)).withTextBinding(binding)
                .withValidationOnInput { validateNotEmpty(String(it.password)) }
                .withValidationOnApply { validateNotEmpty(String(it.password)) }
        }
    }.withPreferredWidth(500)

    private fun ValidationInfoBuilder.validateNotEmpty(value: String): ValidationInfo? {
        if (value.isEmpty()) {
            return error("Must not be empty")
        }

        return null
    }

    private fun ValidationInfoBuilder.validateUrl(value: String): ValidationInfo? {
        val validationInfo = validateNotEmpty(value)

        if (validationInfo != null) {
            return validationInfo
        }

        if (!urlRegex.matches(value)) {
            return error("Not a valid url")
        }

        return null
    }
}