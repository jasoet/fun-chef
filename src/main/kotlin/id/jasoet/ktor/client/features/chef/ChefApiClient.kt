package id.jasoet.ktor.client.features.chef

import com.typesafe.config.ConfigFactory
import io.ktor.client.HttpClient
import io.ktor.client.engine.apache.Apache
import io.ktor.client.features.json.GsonSerializer
import io.ktor.client.features.json.JsonFeature
import org.apache.http.conn.ssl.NoopHostnameVerifier
import org.apache.http.ssl.SSLContextBuilder
import java.io.FileReader

private val config = ConfigFactory.load()

object ChefApiClient {
    private val apiClient by lazy {
        HttpClient(Apache) {
            engine {
                val sslContext = SSLContextBuilder()
                    .loadTrustMaterial(null) { _, _ -> true }.build()
                customizeClient {
                    this.setSSLContext(sslContext)
                    this.setSSLHostnameVerifier(NoopHostnameVerifier())
                }
            }

            install(JsonFeature) {
                serializer = GsonSerializer()
            }

            install(ChefClientFeature) {
                userId = config.getString("USER_ID")
                userPemReader = FileReader(config.getString("USER_PEM_LOCATION"))
                serverHost = config.getString("CHEF_SERVER_HOST")
                organization = config.getString("CHEF_ORGANIZATION")
            }
        }
    }

    operator fun invoke(): HttpClient {
        return apiClient
    }
}

