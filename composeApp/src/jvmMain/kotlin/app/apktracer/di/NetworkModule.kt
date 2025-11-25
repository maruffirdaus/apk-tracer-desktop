package app.apktracer.di

import io.ktor.client.HttpClient
import io.ktor.client.engine.cio.CIO
import io.ktor.client.plugins.HttpTimeout
import org.koin.dsl.module
import java.security.cert.X509Certificate
import javax.net.ssl.X509TrustManager
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Duration.Companion.seconds

val networkModule = module {
    single {
        HttpClient(CIO) {
            engine {
                https {
                    trustManager = object : X509TrustManager {
                        override fun checkClientTrusted(
                            chain: Array<X509Certificate>?,
                            authType: String?
                        ) {
                        }

                        override fun checkServerTrusted(
                            chain: Array<X509Certificate>?,
                            authType: String?
                        ) {
                        }

                        override fun getAcceptedIssuers(): Array<X509Certificate> = arrayOf()
                    }
                }
            }
            install(HttpTimeout) {
                requestTimeoutMillis = 60.minutes.inWholeMilliseconds
                connectTimeoutMillis = 45.seconds.inWholeMilliseconds
                socketTimeoutMillis = 3.minutes.inWholeMilliseconds
            }
        }
    }
}