package com.example.starwarscharactersapp.data.helper

import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import retrofit2.Response

suspend fun <T> safeApiCall(
    apiCall: suspend () -> Response<T>,
): ApiResult<T> = withContext(Dispatchers.IO) {
    try {
        val response = apiCall()
        if (response.isSuccessful) {
            val body = response.body()
            if (body != null) {
                ApiResult.Success(body)
            } else {
                // Successful response but empty body.
                ApiResult.Error("Empty response body")
            }
        } else {
            val errorMsg = response.errorBody()?.string() ?: response.message()
            ApiResult.Error("API Error ${response.code()}: $errorMsg")
        }
    } catch (e: Exception) {
        if (e is CancellationException) throw e
        ApiResult.Error(
            "Network error: ${e.localizedMessage ?: "Unknown error"}",
            e,
        )
    }
}
