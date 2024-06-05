package com.dox.ara.module

import com.dox.ara.api.AssistantAPI
import com.dox.ara.api.AuthInterceptor
import com.dox.ara.api.MessageAPI
import com.dox.ara.utility.Constants
import com.dox.ara.api.AuthenticationAPI
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.security.cert.X509Certificate
import java.util.concurrent.TimeUnit
import javax.inject.Qualifier
import javax.inject.Singleton
import javax.net.ssl.SSLContext
import javax.net.ssl.TrustManager
import javax.net.ssl.X509TrustManager

@Retention(AnnotationRetention.BINARY)
@Qualifier
annotation class WithInterceptor

@Retention(AnnotationRetention.BINARY)
@Qualifier
annotation class WithoutInterceptor

@InstallIn(SingletonComponent::class)
@Module
class NetworkModule {

    @Singleton
    @Provides
    fun provideRetrofit(): Retrofit.Builder {
        return Retrofit.Builder().baseUrl(Constants.BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
    }

    @Singleton
    @Provides
    @WithInterceptor
    fun providesOkHttpClient(authInterceptor: AuthInterceptor): OkHttpClient {
        val loggingInterceptor = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.HEADERS
        }

        val trustAllCerts = arrayOf<TrustManager>(
            object : X509TrustManager {
                override fun checkClientTrusted(
                    chain: Array<out X509Certificate>?,
                    authType: String?
                ) = Unit

                override fun checkServerTrusted(
                    chain: Array<out X509Certificate>?,
                    authType: String?
                ) = Unit

                override fun getAcceptedIssuers(): Array<X509Certificate> = arrayOf()
            }
        )

        val sslContext = SSLContext.getInstance("SSL")
        sslContext.init(null, trustAllCerts, java.security.SecureRandom())

        val sslSocketFactory = sslContext.socketFactory
        val trustManager = trustAllCerts[0] as X509TrustManager

        return OkHttpClient.Builder()
            .addInterceptor(authInterceptor)
            .addInterceptor(loggingInterceptor)
            .sslSocketFactory(sslSocketFactory, trustManager)
            .hostnameVerifier { _, _ -> true }
            .build()
    }

    @Singleton
    @Provides
    @WithoutInterceptor
    fun providesOkHttpClientWithoutInterceptor(): OkHttpClient {
        val loggingInterceptor = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        }

        val trustAllCerts = arrayOf<TrustManager>(
            object : X509TrustManager {
                override fun checkClientTrusted(
                    chain: Array<out X509Certificate>?,
                    authType: String?
                ) = Unit

                override fun checkServerTrusted(
                    chain: Array<out X509Certificate>?,
                    authType: String?
                ) = Unit

                override fun getAcceptedIssuers(): Array<X509Certificate> = arrayOf()
            }
        )

        val sslContext = SSLContext.getInstance("SSL")
        sslContext.init(null, trustAllCerts, java.security.SecureRandom())

        val sslSocketFactory = sslContext.socketFactory
        val trustManager = trustAllCerts[0] as X509TrustManager

        return OkHttpClient.Builder()
            .addInterceptor(loggingInterceptor)
            .sslSocketFactory(sslSocketFactory, trustManager)
            .hostnameVerifier { _, _ -> true }
            .build()
    }

    @Singleton
    @Provides
    fun provideAuthenticationAPI(retrofitBuilder: Retrofit.Builder, @WithoutInterceptor okHttpClient: OkHttpClient): AuthenticationAPI {
        return retrofitBuilder.client(okHttpClient).build().create(AuthenticationAPI::class.java)
    }

    @Singleton
    @Provides
    fun provideMessageAPI(retrofitBuilder: Retrofit.Builder, @WithInterceptor okHttpClient: OkHttpClient): MessageAPI {
        return retrofitBuilder.client(okHttpClient).build().create(MessageAPI::class.java)
    }

    @Singleton
    @Provides
    fun provideAssistantAPI(retrofitBuilder: Retrofit.Builder, @WithInterceptor okHttpClient: OkHttpClient): AssistantAPI {
        val customTimeoutClient = okHttpClient.newBuilder()
            .readTimeout(1, TimeUnit.MINUTES)
            .connectTimeout(1, TimeUnit.MINUTES)
            .build()

        return retrofitBuilder.client(customTimeoutClient).build().create(AssistantAPI::class.java)
    }
}