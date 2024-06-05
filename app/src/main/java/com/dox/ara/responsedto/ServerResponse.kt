package com.dox.ara.responsedto

data class ServerResponse<T> (
    val isSuccess: Boolean,
    val statusCode: Int,
    val errorMessage: String,
    val errorMessages: Map<String, String>,
    val payload: T,
)