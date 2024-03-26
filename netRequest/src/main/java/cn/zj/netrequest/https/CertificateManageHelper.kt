package cn.zj.netrequest.https

import java.security.SecureRandom
import java.security.cert.X509Certificate
import javax.net.ssl.*

object CertificateManageHelper {
     fun getTrustManager() = object : X509TrustManager {
        override fun checkClientTrusted(chain: Array<X509Certificate>, authType: String) {
        }

        override fun checkServerTrusted(chain: Array<X509Certificate>, authType: String) {
        }

        override fun getAcceptedIssuers(): Array<X509Certificate> {
            return arrayOf()
        }
    }

    fun getSSLSocketFactory(): SSLSocketFactory {
        try {
            val sslContext = SSLContext.getInstance("SSL")
            sslContext.init(null, arrayOf(getTrustManager()), SecureRandom())
            return sslContext.socketFactory
        } catch (e: Exception) {
            throw RuntimeException(e)
        }
    }

    //获取HostnameVerifier
    fun getHostnameVerifier(): HostnameVerifier {
        return HostnameVerifier { _, _ -> true }
    }
}