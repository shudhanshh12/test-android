package `in`.okcredit.dynamicview.data.server

import `in`.okcredit.dynamicview.BuildConfig
import `in`.okcredit.dynamicview.data.store.CustomizationTestHelper
import `in`.okcredit.merchant.contract.GetActiveBusinessId
import com.google.common.truth.Truth.assertThat
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.whenever
import io.reactivex.Single
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4
import tech.okcredit.android.base.coroutines.DispatcherProvider
import tech.okcredit.android.base.language.LocaleManager

@RunWith(JUnit4::class)
class CustomizationServerTest {

    private val testHelper = CustomizationTestHelper()
    private val localeManager: LocaleManager = mock()
    private val apiService: CustomizationApiService = mock()
    private val dispatcherProvider: DispatcherProvider = mock()
    private val getActiveBusinessId: GetActiveBusinessId = mock()

    private val server = CustomizationServer(
        { localeManager },
        { apiService },
    )

    @Test
    fun `should call listCustomizations with correct argument`() {
        runBlocking {
            val businessId = "business-id"
            // Given
            whenever(getActiveBusinessId.execute()).thenReturn(Single.just(businessId))
            whenever(localeManager.getLanguage()).thenReturn("en")
            whenever(
                apiService.listCustomizations(
                    GetCustomizationRequest(
                        BuildConfig.VERSION_CODE,
                        "en"
                    ),
                    businessId
                )
            ).thenReturn(testHelper.getDummyCustomizations())
            whenever(dispatcherProvider.io()).thenReturn(Dispatchers.Unconfined)

            // When
            val result = server.getCustomizations(businessId)

            // Then
            assertThat(result).isEqualTo(testHelper.getDummyCustomizations())
        }
    }
}
