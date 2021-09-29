package tech.okcredit.android.auth.server

import okhttp3.Interceptor
import okhttp3.Response
import tech.okcredit.android.auth.AccessTokenProvider
import tech.okcredit.android.auth.AuthLocalSource
import tech.okcredit.android.auth.AuthServiceImpl.Companion.AUTH_HEADER
import tech.okcredit.android.auth.AuthServiceImpl.Companion.BEARER_AUTHORIZATION
import tech.okcredit.android.auth.Grant
import tech.okcredit.android.auth.Unauthorized
import tech.okcredit.android.auth.isValid
import tech.okcredit.base.network.asError
import javax.inject.Inject

// access token provider
class AccessTokenProviderImpl @Inject constructor(
    private val authLocalSource: AuthLocalSource,
    private val authApiClient: AuthApiClient,
) : AccessTokenProvider {

    @Synchronized
    override fun getAccessToken(forceRefresh: Boolean): String? {
        var grant: Grant = authLocalSource.getGrant() ?: return null

        // check if grant is already valid
        if (forceRefresh.not()) {
            if (grant.isValid()) return grant.accessToken
        }

        // if not valid, refresh token
        val res = authApiClient.authenticate(
            AuthApiClient.AuthenticateRequest(
                grant_type = AuthApiClient.GRANT_TYPE_REFRESH_TOKEN,
                refresh_token = grant.refreshToken
            )
        ).execute()

        if (!res.isSuccessful) throw res.asError().mapCode(400 to Unauthorized())

        grant = res.body()!!.toGrant()

        // save new grant
        authLocalSource.setGrant(grant)
        return grant.accessToken
    }
}

// http interceptor
class AuthInterceptor @Inject constructor(
    private val tokenProvider: AccessTokenProvider,
) : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        val req = chain.request()
        val accessToken = tokenProvider.getAccessToken()

        val maxAttempts = 2
        for (attempt in 1..maxAttempts) {
            val requestBuilder = req.newBuilder()
            requestBuilder.addHeader(AUTH_HEADER, "$BEARER_AUTHORIZATION $accessToken")
            val res = chain.proceed(requestBuilder.build())
            if (res.code != 401) return res
        }

        throw Unauthorized()
    }
}
