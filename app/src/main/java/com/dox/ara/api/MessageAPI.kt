package com.dox.ara.api

import com.dox.ara.requestdto.MessageRequest
import com.dox.ara.responsedto.ServerResponse
import com.dox.ara.utility.Constants.APP_ID
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path

interface MessageAPI {

    @POST("/$APP_ID/message/upload")
    suspend fun upload(@Body messageRequest: MessageRequest) : Response<ServerResponse<String>>

    @PUT("/$APP_ID/message/edit")
    suspend fun edit(@Body messageRequest: MessageRequest) : Response<ServerResponse<String>>

    @DELETE("/$APP_ID/message/delete/{assistantId}/{messageId}")
    suspend fun delete(@Path("assistantId") assistantId: Long, @Path("messageId") messageId: Long) : Response<ServerResponse<String>>
}