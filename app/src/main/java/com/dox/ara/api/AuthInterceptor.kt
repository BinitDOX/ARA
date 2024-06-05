package com.dox.ara.api

import com.dox.ara.manager.SharedPreferencesManager
import com.dox.ara.repository.AuthenticationRepository
import com.dox.ara.utility.Constants.AUTH_TOKEN_KEY
import com.dox.ara.utility.Constants.DEVICE_ID_KEY
import com.dox.ara.utility.Constants.DYNAMIC_URL_KEY
import kotlinx.coroutines.runBlocking
import okhttp3.Interceptor
import okhttp3.Response
import okhttp3.ResponseBody.Companion.toResponseBody
import org.json.JSONException
import org.json.JSONObject
import timber.log.Timber
import java.io.IOException
import java.net.HttpURLConnection
import javax.inject.Inject

class AuthInterceptor @Inject constructor(
    private val sharedPrefsManager: SharedPreferencesManager,
    private val authenticationRepository: AuthenticationRepository,
) : Interceptor {

    private var authToken: String? = null;
    private var deviceId: String? = null;

    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request().newBuilder()

        if(authToken.isNullOrEmpty()){
            authToken = sharedPrefsManager.get(AUTH_TOKEN_KEY)

            if(authToken.isNullOrEmpty()){
                Timber.d("[${::intercept.name}] Auth token is null, requesting new token")
                runBlocking {
                    authToken = authenticationRepository.getAuthToken()
                }
            }
        }

        if(authToken.isNullOrEmpty()){
            Timber.e("[${::intercept.name}] Token request failed")
            throw IOException("Token request failed")
        }

        deviceId = sharedPrefsManager.get(DEVICE_ID_KEY)
        request.addHeader("Authorization", "$authToken")
        request.addHeader("DeviceID", "$deviceId")

        val url = sharedPrefsManager.get(DYNAMIC_URL_KEY)
        if(!url.isNullOrEmpty()){
            request.url(url)
        }

        val response = chain.proceed(request.build())

        if (response.isSuccessful &&
            !(response.body?.contentType()?.type == "application" &&
            response.body?.contentType()?.subtype == "json")) {
            return response
        }

        val responseBody = response.body

        responseBody?.let {
            val responseBodyString = it.string()

            try {
                val jsonObject = JSONObject(responseBodyString)

                if (jsonObject.getInt("statusCode") == HttpURLConnection.HTTP_GONE) {  // Token expired
                    Timber.w("[${::intercept.name}] Auth token expired, requesting new token")
                    runBlocking {
                        authToken = authenticationRepository.getAuthToken()
                    }
                    if (!authToken.isNullOrEmpty()) {
                        val newRequest = chain.request().newBuilder()
                            .addHeader("Authorization", "$authToken")
                            .addHeader("DeviceID", "$deviceId")
                            .build()
                        return chain.proceed(newRequest)
                    }
                }
            } catch (exception: JSONException) {
                Timber.w("[${::intercept.name}] JSON Error: $exception")
            }
            return response.newBuilder()
                .body(responseBodyString.toResponseBody(responseBody.contentType()))
                .build()
        }
        return response
    }
}