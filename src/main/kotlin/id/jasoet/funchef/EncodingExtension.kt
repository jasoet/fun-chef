package id.jasoet.funchef

import org.bouncycastle.jce.provider.BouncyCastleProvider
import org.bouncycastle.openssl.PEMKeyPair
import org.bouncycastle.openssl.PEMParser
import org.bouncycastle.openssl.jcajce.JcaPEMKeyConverter
import org.bouncycastle.util.encoders.Base64
import java.io.Reader
import java.security.MessageDigest
import java.security.Security
import java.security.Signature


fun String.sha1Digest(): ByteArray {
    val messageDigest: MessageDigest = MessageDigest.getInstance("SHA-1")
    return messageDigest.digest(this.toByteArray())
}

fun ByteArray.base64Encode(): String {
    return String(Base64.encode(this))
}

fun String.sha1AndBase64Encode(): String {
    return this.sha1Digest().base64Encode()
}

fun String.split60(): Array<String?> {
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

fun String.rsaSign(pemReader: Reader): String {
    Security.addProvider(BouncyCastleProvider())

    val pemKeyPair = PEMParser(pemReader).readObject() as PEMKeyPair
    val converter = JcaPEMKeyConverter().setProvider("BC")
    val keyPair = converter.getKeyPair(pemKeyPair)

    val privateKey = keyPair.private

    val instance = Signature.getInstance("RSA")
    instance.initSign(privateKey)
    instance.update(this.toByteArray())

    val signature = instance.sign()

    return signature.base64Encode()
}