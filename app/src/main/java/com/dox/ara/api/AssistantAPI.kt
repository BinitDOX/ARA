package com.dox.ara.api

import com.dox.ara.requestdto.AssistantRequest
import com.dox.ara.requestdto.MessageRequest
import com.dox.ara.responsedto.MessageResponse
import com.dox.ara.responsedto.ServerResponse
import com.dox.ara.responsedto.VoiceModelsResponse
import com.dox.ara.utility.Constants.APP_ID
import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path

interface AssistantAPI {
    @POST("/$APP_ID/assistant/create")
    suspend fun create(@Body assistantRequest: AssistantRequest) : Response<ServerResponse<String>>

    @PUT("/$APP_ID/assistant/update")
    suspend fun update(@Body assistantRequest: AssistantRequest) : Response<ServerResponse<String>>

    @GET("/$APP_ID/assistant/voice-models")
    suspend fun voiceModels() : Response<ServerResponse<VoiceModelsResponse>>

    @POST("/$APP_ID/assistant/response")
    suspend fun response(@Body messageRequest: MessageRequest) : Response<ServerResponse<MessageResponse>>

    @GET("/$APP_ID/assistant/response_audio/{assistantId}/{messageId}")
    suspend fun responseAudio(
        @Path("assistantId") assistantId: Long,
        @Path("messageId") messageId: Long
    ): Response<ResponseBody>

    @DELETE("/$APP_ID/assistant/delete/{assistantId}")
    suspend fun delete(@Path ("assistantId") assistantId: Long) : Response<ServerResponse<String>>
}