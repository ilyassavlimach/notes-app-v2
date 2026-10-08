package com.machinarium.notesv2.core.network

import com.machinarium.notesv2.core.common.result.AppError
import com.machinarium.notesv2.core.common.result.AppResult
import java.io.IOException
import javax.net.ssl.SSLPeerUnverifiedException
import kotlin.test.assertEquals
import kotlinx.coroutines.test.runTest
import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.Test
import retrofit2.HttpException
import retrofit2.Response

class SafeApiCallTest {

    @Test
    fun `given a successful call, then returns success without retrying`() = runTest {
        var calls = 0

        val result = safeApiCall {
            calls++
            "ok"
        }

        assertEquals(AppResult.Success("ok"), result)
        assertEquals(1, calls)
    }

    @Test
    fun `given a network failure, when the retry succeeds, then returns success`() = runTest {
        var calls = 0

        val result = safeApiCall {
            calls++
            if (calls == 1) throw IOException("offline") else "ok"
        }

        assertEquals(AppResult.Success("ok"), result)
        assertEquals(2, calls)
    }

    @Test
    fun `given a client error, then fails immediately with the status code`() = runTest {
        var calls = 0

        val result = safeApiCall<String> {
            calls++
            throw httpError(404)
        }

        assertEquals(AppResult.Failure(AppError.Server(404)), result)
        assertEquals(1, calls)
    }

    @Test
    fun `given server errors on every attempt, then retries up to max attempts`() = runTest {
        var calls = 0

        val result = safeApiCall<String>(maxAttempts = 3) {
            calls++
            throw httpError(503)
        }

        assertEquals(AppResult.Failure(AppError.Server(503)), result)
        assertEquals(3, calls)
    }

    @Test
    fun `given a certificate pin mismatch, then fails immediately without retrying`() = runTest {
        var calls = 0

        val result = safeApiCall<String> {
            calls++
            throw SSLPeerUnverifiedException("Certificate pinning failure!")
        }

        assertEquals(AppResult.Failure(AppError.Unknown), result)
        assertEquals(1, calls)
    }

    private fun httpError(code: Int) = HttpException(Response.error<String>(code, "".toResponseBody()))
}
