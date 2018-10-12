package id.jasoet.funchef

import id.jasoet.funchef.extension.rsaSign
import id.jasoet.funchef.extension.sha1AndBase64Encode
import id.jasoet.funchef.extension.split60
import io.ktor.client.HttpClient
import io.ktor.client.engine.apache.Apache
import io.ktor.client.features.defaultRequest
import io.ktor.client.request.HttpRequestBuilder
import io.ktor.client.request.headers
import io.ktor.client.request.url
import io.ktor.client.utils.EmptyContent
import java.io.FileReader
import java.io.Reader
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter


fun now(): String {
    return LocalDateTime.now(ZoneId.of("UTC"))
        .format(DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss'Z'"))
}

fun buildRequest(
    userId: String,
    pemReader: Reader
): HttpRequestBuilder.() -> Unit {
    val chefServerHost = "https://chef-server.gopay-internal.vpc"
    val organizationPath = "/organizations/gopay"
    return {
        val httpMethod = method
        val path = "$organizationPath/${url.encodedPath}"
        val requestBody = if (this.body is EmptyContent) {
            ""
        } else {
            this.body.toString()
        }

        url(chefServerHost + path)

        val hashedPath = path.sha1AndBase64Encode()
        val hashedBody = requestBody.sha1AndBase64Encode()

        val requestTime = now()

        val authBuilder = StringBuilder()
            .append("Method:").append(httpMethod.value).append("\n")
            .append("Hashed Path:").append(hashedPath).append("\n")
            .append("X-Ops-Content-Hash:").append(hashedBody).append("\n")
            .append("X-Ops-Timestamp:").append(requestTime).append("\n")
            .append("X-Ops-UserId:").append(userId)
            .toString()

        val authString = authBuilder.rsaSign(pemReader)
        val authHeaders = authString.split60()
        headers {
            clear()
            append("X-Ops-Timestamp", requestTime)
            append("X-Ops-Userid", userId)
            append("X-Chef-Version", "12.22.5")
            append("Accept", "application/json")
            append("X-Ops-Content-Hash", hashedBody)
            append("X-Ops-Sign", "version=1.0")

            authHeaders.forEachIndexed { i, value ->
                append("X-Ops-Authorization-" + (i + 1), value ?: "")
            }
        }
    }
}

val chefApiClient by lazy {
    val userId = "jasoet"
    val pemReader = FileReader("/Users/jasoet/Documents/in/infrastructure/.chef/jasoet.pem")
    HttpClient(Apache) {
        defaultRequest(buildRequest(userId, pemReader))
    }
}
