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
import org.bouncycastle.util.encoders.Base64
import java.security.KeyPair
import java.security.MessageDigest
import java.security.Signature
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter

internal fun String.sha1Digest(): ByteArray {
    val messageDigest: MessageDigest = MessageDigest.getInstance("SHA-1")
    return messageDigest.digest(this.toByteArray())
}

internal fun ByteArray.base64Encode(): String {
    return String(Base64.encode(this))
}

internal fun String.sha1AndBase64Encode(): String {
    return this.sha1Digest().base64Encode()
}

internal fun nowFormatted(): String {
    return LocalDateTime.now(ZoneId.of("UTC"))
        .format(DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss'Z'"))
}

internal fun String.split60(): Array<String?> {
    val count = this.length / 60
    val out = arrayOfNulls<String>(count + 1)

    for (i in 0 until count) {
        val tmp = this.substring(i * 60, i * 60 + 60)
        out[i] = tmp
    }

    if (this.length > count * 60) {
        val tmp = this.substring(count * 60, this.length)
        out[count] = tmp
    }

    return out
}

internal fun String.rsaSign(keyPair: KeyPair): String {
    val privateKey = keyPair.private

    val instance = Signature.getInstance("RSA")
    instance.initSign(privateKey)
    instance.update(this.toByteArray())

    val signature = instance.sign()

    return signature.base64Encode()
}

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
