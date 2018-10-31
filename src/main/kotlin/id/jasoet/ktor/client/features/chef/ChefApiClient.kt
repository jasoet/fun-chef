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

                if (config.hasPath("CHEF_ORGANIZATION")) {
                    organization = config.getString("CHEF_ORGANIZATION")
                }

                if (config.hasPath("CHEF_VERSION")) {
                    chefVersion = config.getString("CHEF_VERSION")
                }
            }
        }
    }

    operator fun invoke(): HttpClient {
        return apiClient
    }
}

