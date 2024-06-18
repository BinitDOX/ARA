package com.dox.ara.api

import com.dox.ara.manager.SharedPreferencesManager
import com.dox.ara.utility.Constants.BASE_URL
import com.dox.ara.utility.Constants.DYNAMIC_URL_KEY
import okhttp3.Interceptor
import okhttp3.Response
import javax.inject.Inject

class UrlInterceptor @Inject constructor(
    private val sharedPrefsManager: SharedPreferencesManager,
) : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request().newBuilder()

        val url = sharedPrefsManager.get(DYNAMIC_URL_KEY)
        if(!url.isNullOrEmpty()){
            val slash = if(url.last() == '/') "" else "/"
            request.url(url+slash+chain.request().url.toUrl().toString().replace(BASE_URL,""))
        }

        return chain.proceed(request.build())
    }
}