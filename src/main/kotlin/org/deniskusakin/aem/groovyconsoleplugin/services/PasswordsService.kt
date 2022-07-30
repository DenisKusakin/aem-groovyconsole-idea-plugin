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

    fun removeCredentials(id: Long) {
        PasswordSafe.instance.set(credentialAttributes(id), null)
    }

    fun setCredentials(id: Long, user: String, password: String) {
        PasswordSafe.instance.set(credentialAttributes(id), Credentials(user, password))
    }

    fun getCredentials(id: Long): Credentials? = PasswordSafe.instance.get(credentialAttributes(id))

    private fun credentialAttributes(id: Long) = CredentialAttributes(
        generateServiceName(AEM_GROOVY_CONSOLE, id.toString()),
    )
}