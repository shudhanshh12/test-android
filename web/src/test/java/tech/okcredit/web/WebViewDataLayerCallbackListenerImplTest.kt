package tech.okcredit.web

import `in`.okcredit.merchant.contract.GetActiveBusinessId
import android.content.Context
import com.google.common.truth.Truth
import com.mixpanel.android.mpmetrics.MixpanelAPI
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.whenever
import io.mockk.every
import io.mockk.mockkObject
import io.reactivex.Observable
import io.reactivex.Single
import io.reactivex.android.plugins.RxAndroidPlugins
import io.reactivex.plugins.RxJavaPlugins
import io.reactivex.schedulers.Schedulers
import org.junit.Before
import org.junit.Test
import org.mockito.Mockito
import tech.okcredit.android.ab.AbRepository
import tech.okcredit.android.auth.AccessTokenProvider
import tech.okcredit.android.base.language.LocaleManager
import tech.okcredit.contacts.contract.ContactsRepository
import tech.okcredit.contacts.contract.model.Contact
import tech.okcredit.web.web_interfaces.WebViewDataLayerCallbackListenerImpl

class WebViewDataLayerCallbackListenerImplTest {

    private val ab: AbRepository = mock()

    private val getActiveBusinessId: GetActiveBusinessId = mock()

    private val accessTokenProvider: AccessTokenProvider = mock()

    private val contactsRepository: ContactsRepository = mock()

    private var context: Context = Mockito.mock(Context::class.java)

    private val mixpanelAPI: MixpanelAPI = mock()

    private val webViewDataLayerCallbackListenerImpl: WebViewDataLayerCallbackListenerImpl = WebViewDataLayerCallbackListenerImpl(
        ab = { ab },
        getActiveBusinessId = { getActiveBusinessId },
        tokenProvider = { accessTokenProvider },
        contactsRepository = { contactsRepository },
        context = { context },
        mixpanelApi = { mixpanelAPI },
    )

    @Before
    fun setup() {
        RxJavaPlugins.setIoSchedulerHandler { Schedulers.trampoline() }
        RxAndroidPlugins.setInitMainThreadSchedulerHandler { Schedulers.trampoline() }
    }

    @Test
    fun `isFeatureEnabled() should return true for enable features`() {
        whenever(ab.isFeatureEnabled(TestData.FEATURE)).thenReturn(Observable.just(true))

        val result = webViewDataLayerCallbackListenerImpl.isFeatureEnabled(TestData.FEATURE)

        Truth.assertThat(result).isTrue()
    }

    @Test
    fun `isFeatureEnabled() should return true for disabled features`() {
        whenever(ab.isFeatureEnabled(TestData.FEATURE)).thenReturn(Observable.just(false))

        val result = webViewDataLayerCallbackListenerImpl.isFeatureEnabled(TestData.FEATURE)

        Truth.assertThat(result).isFalse()
    }

    @Test
    fun `isFeatureEnabled() should return false for errors`() {
        whenever(ab.isFeatureEnabled(TestData.FEATURE)).thenReturn(Observable.error(RuntimeException("")))

        val result = webViewDataLayerCallbackListenerImpl.isFeatureEnabled(TestData.FEATURE)

        Truth.assertThat(result).isFalse()
    }

    @Test
    fun `isExperimentEnabled() should return true for enable experiments`() {
        whenever(ab.isExperimentEnabled(TestData.EXPERIMENT)).thenReturn(Observable.just(true))

        val result = webViewDataLayerCallbackListenerImpl.isExperimentEnabled(TestData.EXPERIMENT)

        Truth.assertThat(result).isTrue()
    }

    @Test
    fun `isExperimentEnabled() should return false for errors`() {
        whenever(ab.isExperimentEnabled(TestData.EXPERIMENT)).thenReturn(Observable.error(RuntimeException("")))

        val result = webViewDataLayerCallbackListenerImpl.isExperimentEnabled(TestData.EXPERIMENT)

        Truth.assertThat(result).isFalse()
    }

    @Test
    fun `isExperimentEnabled() should return false for disabled experiments`() {
        whenever(ab.isExperimentEnabled(TestData.EXPERIMENT)).thenReturn(Observable.just(true))

        val result = webViewDataLayerCallbackListenerImpl.isExperimentEnabled(TestData.EXPERIMENT)

        Truth.assertThat(result).isTrue()
    }

    @Test
    fun `getExperimentVariant() should return right variant`() {
        val variant = "v1"
        whenever(ab.getExperimentVariant(TestData.EXPERIMENT)).thenReturn(Observable.just(variant))

        val result = webViewDataLayerCallbackListenerImpl.getExperimentVariant(TestData.EXPERIMENT)

        Truth.assertThat(result == variant).isTrue()
    }

    @Test
    fun `getExperimentVariant() should return empty string for errors`() {
        whenever(ab.getExperimentVariant(TestData.EXPERIMENT)).thenReturn(Observable.error(RuntimeException("")))

        val result = webViewDataLayerCallbackListenerImpl.getExperimentVariant(TestData.EXPERIMENT)

        Truth.assertThat(result == "").isTrue()
    }

    @Test
    fun `getVariantConfigurations() should return right variant`() {
        val variantConfigurations = mapOf("key1" to "value1")
        whenever(ab.getVariantConfigurations(TestData.EXPERIMENT)).thenReturn(Observable.just(variantConfigurations))

        val result = webViewDataLayerCallbackListenerImpl.getVariantConfigurations(TestData.EXPERIMENT)

        Truth.assertThat(result == variantConfigurations.toString()).isTrue()
    }

    @Test
    fun `getVariantConfigurations() should return empty for errors`() {
        whenever(ab.getVariantConfigurations(TestData.EXPERIMENT)).thenReturn(Observable.error(RuntimeException("")))

        val result = webViewDataLayerCallbackListenerImpl.getVariantConfigurations(TestData.EXPERIMENT)

        Truth.assertThat(result == "{}").isTrue()
    }

    @Test
    fun `getMerchantId() should return right merchant Id`() {
        whenever(getActiveBusinessId.execute()).thenReturn(Single.just(TestData.MERCHANT.id))

        val result = webViewDataLayerCallbackListenerImpl.getMerchantId()

        Truth.assertThat(result == TestData.MERCHANT.id).isTrue()
    }

    @Test
    fun `getMerchantId() should return empty for errors`() {
        whenever(getActiveBusinessId.execute()).thenReturn(Single.error(RuntimeException("")))

        val result = webViewDataLayerCallbackListenerImpl.getMerchantId()

        Truth.assertThat(result == "").isTrue()
    }

    @Test
    fun `getAuthToken() should return right merchant Id`() {
        val token = "asdfghjkl"
        whenever(accessTokenProvider.getAccessToken()).thenReturn(token)

        val result = webViewDataLayerCallbackListenerImpl.getAuthToken()

        Truth.assertThat(result == token).isTrue()
    }

    @Test
    fun `getAuthToken() should return empty for errors`() {
        whenever(accessTokenProvider.getAccessToken()).thenThrow(RuntimeException(""))

        val result = webViewDataLayerCallbackListenerImpl.getAuthToken()

        Truth.assertThat(result == "").isTrue()
    }

    @Test
    fun `getContacts() should return empty for errors`() {
        whenever(contactsRepository.getContacts()).thenReturn(Observable.error(RuntimeException("")))

        val result = webViewDataLayerCallbackListenerImpl.getContacts()

        Truth.assertThat(result == listOf<Contact>().toString()).isTrue()
    }

    @Test
    fun `getContacts() should return right list`() {
        val list = listOf(TestData.CONTACT1, TestData.CONTACT2)
        whenever(contactsRepository.getContacts()).thenReturn(Observable.just(list))

        val result = webViewDataLayerCallbackListenerImpl.getContacts()

        Truth.assertThat(result == list.toString()).isTrue()
    }

    @Test
    fun `getLanguage() should return right language`() {
        val langauge = "en"

        mockkObject(LocaleManager)
        every { LocaleManager.getLanguage(context) }.returns(langauge)
        val result = webViewDataLayerCallbackListenerImpl.getLanguage()

        Truth.assertThat(result == langauge).isTrue()
    }
}
