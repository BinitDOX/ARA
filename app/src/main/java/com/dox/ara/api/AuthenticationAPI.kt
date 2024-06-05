package com.dox.ara.api

import com.dox.ara.responsedto.ServerResponse
import com.dox.ara.utility.Constants.APP_ID
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Header

interface AuthenticationAPI {

    @GET("/$APP_ID/token")
    suspend fun getAuthToken(@Header("auth_key") authKey: String) : Response<ServerResponse<String>>

    @GET("/$APP_ID/status")
    suspend fun getStatus() : Response<ServerResponse<String>>
}