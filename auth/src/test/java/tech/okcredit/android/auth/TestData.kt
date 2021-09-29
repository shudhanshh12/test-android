package tech.okcredit.android.auth

import okhttp3.Request
import okio.Timeout
import org.joda.time.DateTime
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import tech.okcredit.android.auth.server.AuthApiClient

internal object TestData {
    const val MOBILE = "7760747507"
    const val PASSWORD = "123456"
    const val PASSWORD_HASH = "/qwsN5SooEZScf1pwZqw+vielK/LtxkzcA+aWnxq3xs="

    val CURRENT_TIME = DateTime(2018, 4, 3, 0, 0, 0)

    val INVALID_GRANT = Grant(
        accessToken = "some_invalid_access_token",
        refreshToken = "some_refresh_token",
        expireTime = CURRENT_TIME.minusMinutes(1)
    )

    val VALID_GRANT = Grant(
        accessToken = "some_valid_access_token",
        refreshToken = "some_other_refresh_token",
        expireTime = CURRENT_TIME.plusMinutes(10)
    )

    val VALID_CREDENTIAL_GRANT = VALID_GRANT.copy(mobile = MOBILE)

    val VALID_REFRESH_TOKEN_REQUEST = AuthApiClient.AuthenticateRequest(
        grant_type = AuthApiClient.GRANT_TYPE_REFRESH_TOKEN,
        refresh_token = INVALID_GRANT.refreshToken
    )

    val VALID_REFRESH_TOKEN_RESPONSE = object : Call<AuthApiClient.AuthenticateResponse> {
        override fun enqueue(callback: Callback<AuthApiClient.AuthenticateResponse>) {
            throw NotImplementedError()
        }

        override fun isExecuted(): Boolean {
            throw NotImplementedError()
        }

        override fun clone(): Call<AuthApiClient.AuthenticateResponse> {
            throw NotImplementedError()
        }

        override fun isCanceled(): Boolean {
            throw NotImplementedError()
        }

        override fun cancel() {
            throw NotImplementedError()
        }

        override fun execute(): Response<AuthApiClient.AuthenticateResponse> = Response.success(
            200,
            AuthApiClient.AuthenticateResponse(
                access_token = VALID_GRANT.accessToken,
                refresh_token = VALID_GRANT.refreshToken,
                expires_in = 600
            )
        )

        override fun request(): Request {
            throw NotImplementedError()
        }

        override fun timeout(): Timeout {
            throw NotImplementedError()
        }
    }
}
