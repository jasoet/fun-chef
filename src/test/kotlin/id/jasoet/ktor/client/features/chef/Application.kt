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

import io.ktor.client.HttpClient
import io.ktor.client.engine.apache.Apache
import io.ktor.client.features.json.GsonSerializer
import io.ktor.client.features.json.JsonFeature
import io.ktor.client.request.get
import kotlinx.coroutines.runBlocking
import org.slf4j.LoggerFactory
import java.io.FileReader

/**
 * Documentation of your class
 */
object Application {
    private val log = LoggerFactory.getLogger(Application::class.java)

    private val chefApiClient by lazy {
        HttpClient(Apache) {
            install(JsonFeature) {
                serializer = GsonSerializer()
            }

            install(ChefClientFeature) {
                userId = "jasoet"
                userPemReader = FileReader("/Users/jasoet/Documents/in/infrastructure/.chef/jasoet.pem")
                serverHost = "https://chef-server.gopay-internal.vpc"
                organization = "gopay"
            }
        }
    }

    @JvmStatic
    fun main(args: Array<String>) = runBlocking<Unit> {
        val roles = chefApiClient.get<ChefResult>("/nodes/i-gopay-gitlab-runner-01")
        println(roles["automatic.filesystem"].formatted())
        println(roles["automatic.filesystem"])
    }
}
