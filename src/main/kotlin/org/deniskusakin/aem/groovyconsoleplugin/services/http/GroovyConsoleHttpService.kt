package org.deniskusakin.aem.groovyconsoleplugin.services.http

import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.intellij.openapi.Disposable
import com.intellij.openapi.project.Project
import org.apache.http.HttpException
import org.apache.http.HttpResponse
import org.apache.http.HttpStatus
import org.apache.http.auth.AuthScope
import org.apache.http.auth.Credentials
import org.apache.http.auth.UsernamePasswordCredentials
import org.apache.http.client.config.RequestConfig
import org.apache.http.client.methods.HttpPost
import org.apache.http.client.protocol.HttpClientContext
import org.apache.http.client.utils.URIBuilder
import org.apache.http.client.utils.URIUtils
import org.apache.http.entity.mime.MultipartEntityBuilder
import org.apache.http.impl.auth.BasicScheme
import org.apache.http.impl.client.BasicAuthCache
import org.apache.http.impl.client.BasicCredentialsProvider
import org.apache.http.impl.client.CloseableHttpClient
import org.apache.http.impl.client.HttpClientBuilder
import org.deniskusakin.aem.groovyconsoleplugin.services.PasswordsService
import org.deniskusakin.aem.groovyconsoleplugin.services.http.model.GroovyConsoleOutput
import org.deniskusakin.aem.groovyconsoleplugin.services.http.model.GroovyConsoleResponseHandler
import org.deniskusakin.aem.groovyconsoleplugin.services.model.AemServerConfig
import java.io.InputStreamReader
import java.net.URI
import java.util.concurrent.TimeUnit

/**
 * User: Andrey Bardashevsky
 * Date/Time: 02.08.2022 16:06
 */
class GroovyConsoleHttpService : Disposable {
    private val httpClient: CloseableHttpClient = createHttpRequest()

    private val gson: Gson = GsonBuilder().create()

    private fun createHttpRequest(): CloseableHttpClient {
        val timeout = TimeUnit.MINUTES.toMillis(10).toInt()

        return HttpClientBuilder.create()
            .setDefaultRequestConfig(
                RequestConfig.custom()
                    .setConnectTimeout(timeout)
                    .setConnectionRequestTimeout(timeout)
                    .setSocketTimeout(timeout)
                    .build()
            )
            .build()
    }

    fun execute(config: AemServerConfig, script: ByteArray, responseHandler: GroovyConsoleResponseHandler) {
        try {
            val uri = URIBuilder().also {
                val configHostUri = URI.create(config.url)

                it.scheme = configHostUri.scheme
                it.host = configHostUri.host
                it.port = configHostUri.port

                it.path = GROOVY_CONSOLE_PATH
            }.build()

            val httpHost = URIUtils.extractHost(uri)

            val context: HttpClientContext = HttpClientContext.create().also { context ->
                context.authCache = BasicAuthCache().also { cache ->
                    cache.put(httpHost, BasicScheme())
                }
                context.credentialsProvider = BasicCredentialsProvider().also { credentialsProvider ->
                    credentialsProvider.setCredentials(AuthScope(httpHost), getCredentials(config))
                }
            }

            val request = HttpPost(uri).also {
                it.entity = MultipartEntityBuilder.create().addBinaryBody("script", script).build()
            }

            val result = httpClient.execute(httpHost, request, ::handleResponse, context)

            responseHandler.onSuccess(result)
        } catch (e: Throwable) {
            responseHandler.onFail(e)
        }
    }

    private fun handleResponse(response: HttpResponse): GroovyConsoleOutput {
        if (HttpStatus.SC_OK == response.statusLine.statusCode) {
            return InputStreamReader(response.entity.content).use {
                return@use gson.fromJson(it, GroovyConsoleOutput::class.java)
            }
        } else {
            throw HttpException("HTTP Error: ${response.statusLine}")
        }
    }

    private fun getCredentials(config: AemServerConfig): Credentials {
        val credentials = PasswordsService.getCredentials(config.id)

        val user = credentials?.userName.orEmpty()
        val password = credentials?.getPasswordAsString().orEmpty()

        if (user.isBlank() || password.isBlank()) {
            throw IllegalArgumentException("Credentials is not found for server: ${config.id}")
        }

        return UsernamePasswordCredentials(user, password)
    }

    override fun dispose() {
        httpClient.close()
    }

    companion object {
        private const val GROOVY_CONSOLE_PATH = "/bin/groovyconsole/post.json"

        fun getInstance(project: Project): GroovyConsoleHttpService {
            return project.getService(GroovyConsoleHttpService::class.java)
        }
    }
}