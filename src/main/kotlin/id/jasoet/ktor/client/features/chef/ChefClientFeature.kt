/*
 * Copyright (C)2018 - Deny Prasetyo <jasoet87@gmail.com>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package id.jasoet.ktor.client.features.chef

import com.google.gson.GsonBuilder
import com.google.gson.JsonElement
import io.ktor.client.HttpClient
import io.ktor.client.features.HttpClientFeature
import io.ktor.client.request.HttpRequestBuilder
import io.ktor.client.request.HttpRequestPipeline
import io.ktor.client.request.headers
import io.ktor.client.request.url
import io.ktor.client.utils.EmptyContent
import io.ktor.content.TextContent
import io.ktor.util.AttributeKey
import org.bouncycastle.jce.provider.BouncyCastleProvider
import org.bouncycastle.openssl.PEMKeyPair
import org.bouncycastle.openssl.PEMParser
import org.bouncycastle.openssl.jcajce.JcaPEMKeyConverter
import java.io.Reader
import java.security.KeyPair
import java.security.Security

typealias ChefResult = JsonElement

private val gson = GsonBuilder().setPrettyPrinting().create()

operator fun ChefResult?.get(path: String): ChefResult? {
    val paths = path.split(".")

    return paths.fold(this) { item, key ->
        if (item?.isJsonObject == true) {
            item.asJsonObject[key]
        } else {
            null
        }
    }
}

fun ChefResult?.formatted(): String {
    return gson.toJson(this)
}

class ChefConfig {
    lateinit var userId: String
    lateinit var userPemReader: Reader
    lateinit var serverHost: String
    var chefVersion: String? = null
    var organization: String? = null

    internal fun build(): ChefClientFeature {
        Security.addProvider(BouncyCastleProvider())

        val pemKeyPair = PEMParser(userPemReader).readObject() as PEMKeyPair
        val converter = JcaPEMKeyConverter().setProvider("BC")
        val keyPair = converter.getKeyPair(pemKeyPair)
        val organizationPath = organization?.let { "/organizations/${it.replace("/", "")}" } ?: ""

        val defaultVersion = "12.22.5"
        return ChefClientFeature(userId, keyPair, serverHost, organizationPath, chefVersion ?: defaultVersion)
    }
}

class ChefClientFeature(
    private val userId: String,
    private val keyPair: KeyPair,
    private val serverHost: String,
    private val organizationPath: String,
    private val chefVersion: String
) {

    companion object Feature : HttpClientFeature<ChefConfig, ChefClientFeature> {
        override val key: AttributeKey<ChefClientFeature> = AttributeKey("ChefClient")

        override fun prepare(block: ChefConfig.() -> Unit): ChefClientFeature {
            return ChefConfig().apply(block).build()
        }

        override fun install(feature: ChefClientFeature, scope: HttpClient) {
            scope.requestPipeline.intercept(HttpRequestPipeline.Render) {
                val subject = this.subject

                when (subject) {
                    is TextContent -> feature.buildAuth(context, subject.text)
                    is EmptyContent -> feature.buildAuth(context, "")
                }

                proceedWith(subject)
            }
        }
    }

    internal fun buildAuth(context: HttpRequestBuilder, requestBody: String) {
        val httpMethod = context.method
        val path = "$organizationPath/${context.url.encodedPath}"

        context.url(serverHost + path)

        val hashedPath = path.sha1AndBase64Encode()
        val hashedBody = requestBody.sha1AndBase64Encode()

        val requestTime = nowFormatted()

        val authBuilder = StringBuilder()
            .append("Method:").append(httpMethod.value).append("\n")
            .append("Hashed Path:").append(hashedPath).append("\n")
            .append("X-Ops-Content-Hash:").append(hashedBody).append("\n")
            .append("X-Ops-Timestamp:").append(requestTime).append("\n")
            .append("X-Ops-UserId:").append(userId)
            .toString()

        val authString = authBuilder.rsaSign(keyPair)
        val authHeaders = authString.split60()
        context.headers {
            append("X-Ops-Timestamp", requestTime)
            append("X-Ops-Userid", userId)
            append("X-Chef-Version", chefVersion)
            append("Accept", "application/json")
            append("X-Ops-Content-Hash", hashedBody)
            append("X-Ops-Sign", "version=1.0")

            authHeaders.forEachIndexed { i, value ->
                append("X-Ops-Authorization-" + (i + 1), value ?: "")
            }
        }
    }
}

