package com.dox.ara.module

import com.dox.ara.api.AssistantAPI
import com.dox.ara.api.AuthInterceptor
import com.dox.ara.api.AuthenticationAPI
import com.dox.ara.api.MessageAPI
import com.dox.ara.api.UrlInterceptor
import com.dox.ara.utility.Constants
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
annotation class WithAuthInterceptor

@Retention(AnnotationRetention.BINARY)
@Qualifier
annotation class WithoutAuthInterceptor

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
    @WithAuthInterceptor
    fun providesOkHttpClient(authInterceptor: AuthInterceptor,
                             urlInterceptor: UrlInterceptor): OkHttpClient {
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
            .addInterceptor(urlInterceptor)
            .addInterceptor(authInterceptor)
            .addInterceptor(loggingInterceptor)
            .sslSocketFactory(sslSocketFactory, trustManager)
            .hostnameVerifier { _, _ -> true }
            .build()
    }

    @Singleton
    @Provides
    @WithoutAuthInterceptor
    fun providesOkHttpClientWithoutInterceptor(urlInterceptor: UrlInterceptor): OkHttpClient {
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
            .addInterceptor(urlInterceptor)
            .addInterceptor(loggingInterceptor)
            .sslSocketFactory(sslSocketFactory, trustManager)
            .hostnameVerifier { _, _ -> true }
            .build()
    }

    @Singleton
    @Provides
    fun provideAuthenticationAPI(retrofitBuilder: Retrofit.Builder, @WithoutAuthInterceptor okHttpClient: OkHttpClient): AuthenticationAPI {
        return retrofitBuilder.client(okHttpClient).build().create(AuthenticationAPI::class.java)
    }

    @Singleton
    @Provides
    fun provideMessageAPI(retrofitBuilder: Retrofit.Builder, @WithAuthInterceptor okHttpClient: OkHttpClient): MessageAPI {
        return retrofitBuilder.client(okHttpClient).build().create(MessageAPI::class.java)
    }

    @Singleton
    @Provides
    fun provideAssistantAPI(retrofitBuilder: Retrofit.Builder, @WithAuthInterceptor okHttpClient: OkHttpClient): AssistantAPI {
        val customTimeoutClient = okHttpClient.newBuilder()
            .readTimeout(1, TimeUnit.MINUTES)
            .connectTimeout(1, TimeUnit.MINUTES)
            .build()

        return retrofitBuilder.client(customTimeoutClient).build().create(AssistantAPI::class.java)
    }
}