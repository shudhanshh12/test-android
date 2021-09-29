package tech.okcredit.base.network

import com.google.common.truth.Truth.assertThat
import com.google.gson.Gson
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.Protocol
import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.Test
import retrofit2.Response

class ApiErrorTest {

    @Test
    fun `should return default values if all arguments are not passed`() {
        val error = ApiError()

        assertThat(error.code).isEqualTo(500)
        assertThat(error.error).isEqualTo("unknown")
        assertThat(error.error).isEqualTo("unknown")
    }

    @Test
    fun `should error as message when error is present`() {
        val error = ApiError(413, "Payload Too Large")

        assertThat(error.message).isEqualTo("Payload Too Large")
    }

    @Test
    fun `should default message when error is null`() {
        val error = ApiError(413, null)

        assertThat(error.message).isEqualTo("HTTP request failed (code=413)")
    }

    @Test
    fun `should default message when error is blank`() {
        val error = ApiError(413, "")

        assertThat(error.message).isEqualTo("HTTP request failed (code=413)")
    }

    @Test
    fun `should throw exception when asError is called with successful response`() {
        val response = Response.success(200, "body")

        var result: Exception? = null
        try {
            response.asError()
        } catch (e: Exception) {
            result = e
        }

        assertThat(result).isInstanceOf(IllegalStateException::class.java)
        assertThat(result!!.message).isEqualTo("cannot parse ApiError from a successful api call")
    }

    @Test
    fun `should return ApiError with code when response payload is not according to contract`() {
        val response = Response.error<String>(
            501, "Internal Server Error".toResponseBody("text/plain".toMediaType())
        )

        val result = response.asError()

        assertThat(result).isEqualTo(ApiError(501, "unknown"))
    }

    @Test
    fun `should ApiError with code and error passed in response`() {
        val payload = ApiError(400, "Missing param customer id")
        val response = Response.error<ApiError>(
            400, Gson().toJson(payload).toResponseBody("text/plain".toMediaType())
        )

        val result = response.asError()

        assertThat(result).isEqualTo(payload)
    }

    @Test
    fun `should throw exception when asError is called with successful okhttp response`() {
        val request: okhttp3.Request = okhttp3.Request.Builder()
            .url("https://www.youtube.com/watch?v=TIQ5hrfermg")
            .build()
        val response = okhttp3.Response.Builder()
            .request(request)
            .protocol(Protocol.HTTP_2)
            .code(200)
            .message("")
            .body("bodu".toResponseBody("text/plain".toMediaType()))
            .build()

        var result: Exception? = null
        try {
            response.asError()
        } catch (e: Exception) {
            result = e
        }

        assertThat(result).isInstanceOf(IllegalStateException::class.java)
        assertThat(result!!.message).isEqualTo("cannot parse ApiError from a successful api call")
    }

    @Test
    fun `should return ApiError with code when okhttp response payload is not according to contract`() {
        val request: okhttp3.Request = okhttp3.Request.Builder()
            .url("https://www.youtube.com/watch?v=dZdW7sQT3Pg")
            .build()
        val response = okhttp3.Response.Builder()
            .request(request)
            .protocol(Protocol.HTTP_2)
            .code(501)
            .message("")
            .body("Internal Server Error".toResponseBody("text/plain".toMediaType()))
            .build()

        val result = response.asError()

        assertThat(result).isEqualTo(ApiError(501, "unknown", "https://www.youtube.com/watch?v=dZdW7sQT3Pg"))
    }

    @Test
    fun `should ApiError with code and error passed in okhttp response`() {
        val payload = ApiError(400, "Missing param customer id")
        val request: okhttp3.Request = okhttp3.Request.Builder()
            .url("https://www.youtube.com/watch?v=Ocu35D_h1EE")
            .build()
        val response = okhttp3.Response.Builder()
            .request(request)
            .protocol(Protocol.HTTP_2)
            .code(501)
            .message("")
            .body(Gson().toJson(payload).toResponseBody("text/plain".toMediaType()))
            .build()

        val result = response.asError()

        assertThat(result).isEqualTo(payload)
    }
}
