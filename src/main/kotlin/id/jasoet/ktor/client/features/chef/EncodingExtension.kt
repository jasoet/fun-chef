package id.jasoet.ktor.client.features.chef

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

internal fun now(): String {
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