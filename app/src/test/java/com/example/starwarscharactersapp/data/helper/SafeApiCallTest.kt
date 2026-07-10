package com.example.starwarscharactersapp.data.helper

import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.test.runTest
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Assert.fail
import org.junit.Test
import retrofit2.Response

class SafeApiCallTest {

    @Test
    fun `returns Success when response is successful with a body`() = runTest {
        val result = safeApiCall { Response.success("data") }

        assertTrue(result is ApiResult.Success)
        assertEquals("data", (result as ApiResult.Success).data)
    }

    @Test
    fun `returns Error when response is successful but body is null`() = runTest {
        val result = safeApiCall<String> { Response.success(null) }

        assertTrue(result is ApiResult.Error)
        assertEquals("Empty response body", (result as ApiResult.Error).message)
    }

    @Test
    fun `returns Error with code and message when response is unsuccessful`() = runTest {
        val errorBody = "not found".toResponseBody("text/plain".toMediaTypeOrNull())
        val response = Response.error<String>(404, errorBody)

        val result = safeApiCall { response }

        assertTrue(result is ApiResult.Error)
        val error = result as ApiResult.Error
        assertTrue(error.message.contains("404"))
        assertTrue(error.message.contains("not found"))
    }

    @Test
    fun `returns Error when apiCall throws an exception`() = runTest {
        val result = safeApiCall<String> { throw RuntimeException("exception") }

        assertTrue(result is ApiResult.Error)
        val error = result as ApiResult.Error
        assertTrue(error.message.contains("exception"))
        assertNotNull(error.throwable)
    }

    @Test
    fun `rethrows CancellationException instead of wrapping it`() = runTest {
        try {
            safeApiCall<String> { throw CancellationException("cancelled") }
            fail("Expected CancellationException to propagate")
        } catch (e: CancellationException) {
            assertEquals("cancelled", e.message)
        }
    }
}
