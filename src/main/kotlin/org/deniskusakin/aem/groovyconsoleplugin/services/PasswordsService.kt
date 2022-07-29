package org.deniskusakin.aem.groovyconsoleplugin.services

import com.intellij.credentialStore.CredentialAttributes
import com.intellij.credentialStore.Credentials
import com.intellij.credentialStore.generateServiceName
import com.intellij.ide.passwordSafe.PasswordSafe


/**
 * User: Andrey Bardashevsky
 * Date/Time: 29.07.2022 22:41
 */
object PasswordsService {
    private const val AEM_GROOVY_CONSOLE = "AEMGroovyConsole"

    fun setCredentials(id: String, user: String, password: String) {
        val passwordSafe = PasswordSafe.instance

        val attributes = CredentialAttributes(
            generateServiceName(AEM_GROOVY_CONSOLE, id),
        )

        passwordSafe.set(attributes, Credentials(user, password))
    }

    fun getCredentials(id: String): Credentials? {
        val passwordSafe = PasswordSafe.instance

        val attributes = CredentialAttributes(
            generateServiceName(AEM_GROOVY_CONSOLE, id),
        )

        return passwordSafe.get(attributes)
    }
}