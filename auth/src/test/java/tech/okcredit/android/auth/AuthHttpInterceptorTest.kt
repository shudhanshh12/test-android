package tech.okcredit.android.auth

import com.google.common.truth.Truth.assertThat
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.times
import com.nhaarman.mockitokotlin2.verify
import io.reactivex.Observable
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.joda.time.DateTimeUtils
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.mockito.Mockito.`when`
import tech.okcredit.android.auth.AuthServiceImpl.Companion.AUTH_HEADER
import tech.okcredit.android.auth.TestData.CURRENT_TIME
import tech.okcredit.android.auth.TestData.INVALID_GRANT
import tech.okcredit.android.auth.TestData.VALID_GRANT
import tech.okcredit.android.auth.TestData.VALID_REFRESH_TOKEN_REQUEST
import tech.okcredit.android.auth.TestData.VALID_REFRESH_TOKEN_RESPONSE
import tech.okcredit.android.auth.server.AccessTokenProviderImpl
import tech.okcredit.android.auth.server.AuthApiClient
import tech.okcredit.android.auth.server.AuthInterceptor
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.CountDownLatch
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit

internal class AuthHttpInterceptorTest {

    // subject
    lateinit var interceptor: Interceptor
    lateinit var okHttpClient: OkHttpClient

    // mocks
    lateinit var authLocalSource: AuthLocalSource

    private val authApiClient: AuthApiClient = mock()

    lateinit var accessTokenProvider: AccessTokenProvider
    lateinit var mockWebServer: MockWebServer

    @Before
    fun setup() {
        // set CURRENT_TIME as now
        DateTimeUtils.setCurrentMillisFixed(CURRENT_TIME.millis)

        authLocalSource = object : AuthLocalSource {

            var currentGrant: Grant? = null

            override fun getGrant(): Grant? = currentGrant

            override fun setGrant(grant: Grant) = synchronized(this) { currentGrant = grant }

            override fun observeAccessToken(): Observable<String> {
                throw NotImplementedError()
            }

            override fun deleteAllExceptMobile() {
                throw NotImplementedError()
            }

            override fun setCurrentMobileOTPToken(token: String) {
                throw NotImplementedError()
            }

            override fun setNewMobileOtpToken(token: String) {
                throw NotImplementedError()
            }

            override fun getNewMobileOtpToken(): String? {
                throw NotImplementedError()
            }

            override fun getCurrentMobileOtpToken(): String? {
                throw NotImplementedError()
            }

            override suspend fun invalidateAccessToken() {
                throw NotImplementedError()
            }

            override fun getMobile(): String? {
                throw NotImplementedError()
            }

            override fun setMobile(mobile: String) {
                throw NotImplementedError()
            }

            override fun getAccessToken(): String? {
                throw NotImplementedError()
            }

            override fun getPasswordHash(): String? {
                throw NotImplementedError()
            }

            override fun setPasswordHash(passwordHash: String) {
                throw NotImplementedError()
            }
        }

        accessTokenProvider = AccessTokenProviderImpl(authLocalSource, authApiClient)
        interceptor = AuthInterceptor(accessTokenProvider)
        okHttpClient = OkHttpClient().newBuilder().addInterceptor(interceptor).build()
        mockWebServer = MockWebServer()
        mockWebServer.start()
    }

    @After
    fun teardown() {
        mockWebServer.shutdown()

        // reset date time
        DateTimeUtils.setCurrentMillisSystem()
    }

    @Test
    fun `auth header is added to the request if a valid grant is available`() {
        authLocalSource.setGrant(VALID_GRANT)

        mockWebServer.enqueue(MockResponse())
        okHttpClient.newCall(Request.Builder().url(mockWebServer.url("/")).build()).execute()

        val receivedReq = mockWebServer.takeRequest()
        assertThat(receivedReq.getHeader(AUTH_HEADER)).isEqualTo("Bearer ${VALID_GRANT.accessToken}")
    }

    @Test
    fun `auth header is added to the request after refreshing and saving the grant if it is invalid`() {
        authLocalSource.setGrant(INVALID_GRANT)
        `when`(authApiClient.authenticate(VALID_REFRESH_TOKEN_REQUEST)).thenReturn(VALID_REFRESH_TOKEN_RESPONSE)

        mockWebServer.enqueue(MockResponse())
        okHttpClient.newCall(Request.Builder().url(mockWebServer.url("/")).build()).execute()

        val receivedReq = mockWebServer.takeRequest()
        assertThat(receivedReq.getHeader(AUTH_HEADER)).isEqualTo("Bearer ${VALID_GRANT.accessToken}")
        assertThat(authLocalSource.getGrant()!!.accessToken).isEqualTo(VALID_GRANT.accessToken)
        assertThat(authLocalSource.getGrant()!!.refreshToken).isEqualTo(VALID_GRANT.refreshToken)
    }

    @Test
    fun `refreshing of grant is done synchronously`() {
        authLocalSource.setGrant(INVALID_GRANT)
        `when`(authApiClient.authenticate(VALID_REFRESH_TOKEN_REQUEST)).thenReturn(VALID_REFRESH_TOKEN_RESPONSE)

        val concurrency = 10
        val startLatch = CountDownLatch(1) // synchronizes start
        val finishLatch = CountDownLatch(concurrency) // synchronizes finish
        val executorService = Executors.newFixedThreadPool(concurrency)
        val requestAuthHeaders = ConcurrentHashMap<Int, String>()

        var i = 0
        while (i < concurrency) {
            val index = i
            executorService.submit {
                startLatch.await()
                mockWebServer.enqueue(MockResponse())
                okHttpClient.newCall(Request.Builder().url(mockWebServer.url("/")).build()).execute()
                val receivedReq = mockWebServer.takeRequest()
                requestAuthHeaders[index] = receivedReq.getHeader(AUTH_HEADER)!!
                finishLatch.countDown()
            }
            i++
        }
        startLatch.countDown() // start execution
        finishLatch.await(10, TimeUnit.SECONDS) // wait for execution to finish

        for ((_, header) in requestAuthHeaders) {
            assertThat(header).isEqualTo("Bearer ${VALID_GRANT.accessToken}")
        }
        verify(authApiClient, times(1)).authenticate(VALID_REFRESH_TOKEN_REQUEST)
    }
}
