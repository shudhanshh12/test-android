package tech.okcredit.home.usecase

import `in`.okcredit.backend.contract.Version
import `in`.okcredit.shared.service.keyval.KeyValService
import com.google.gson.Gson
import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.eq
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.whenever
import io.reactivex.Observable
import io.reactivex.Single
import io.reactivex.android.plugins.RxAndroidPlugins
import io.reactivex.schedulers.Schedulers
import org.junit.Before
import org.junit.Test
import tech.okcredit.android.base.language.LocaleManager
import tech.okcredit.android.base.preferences.DefaultPreferences.Keys.PREF_INDIVIDUAL_KEY_SERVER_VERSION

class GetSupplierKnowMoreWebLinkTest {
    private val mockKeyValService: KeyValService = mock()
    private val mockLocaleManager: LocaleManager = mock()

    private val getSupplierKnowMoreWebLink = GetSupplierKnowMoreWebLink(
        mockKeyValService,
        mockLocaleManager,
    )

    @Before
    fun setup() {
        RxAndroidPlugins.setInitMainThreadSchedulerHandler { Schedulers.trampoline() }
    }

    @Test
    fun `Should return nothing if KEY_SERVER_VERSION is not present`() {
        whenever(mockKeyValService.contains(eq(PREF_INDIVIDUAL_KEY_SERVER_VERSION), any()))
            .thenReturn(Single.just(false))

        val testObserver = getSupplierKnowMoreWebLink.execute().test()

        testObserver.assertComplete()
        testObserver.dispose()
    }

    @Test
    fun `Should return value if KEY_SERVER_VERSION is present`() {
        val fakeVersionString = Gson().toJson(fakeVersion)
        whenever(mockKeyValService[eq(PREF_INDIVIDUAL_KEY_SERVER_VERSION), any()])
            .thenReturn(Observable.just(fakeVersionString))
        whenever(mockKeyValService.contains(eq(PREF_INDIVIDUAL_KEY_SERVER_VERSION), any()))
            .thenReturn(Single.just(true))
        whenever(mockLocaleManager.getLanguage())
            .thenReturn(fakeLanguage)

        val testObserver = getSupplierKnowMoreWebLink.execute().test()

        testObserver.assertValue("$fakeSupplierKnowMoreWebLink?lng=$fakeLanguage")
        testObserver.dispose()
    }

    companion object {
        private const val fakeLanguage = "fake_language"
        private const val fakeSupplierKnowMoreWebLink = "fake_supplier_learn_more_web_link"

        private val fakeVersion = Version(
            0,
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            fakeSupplierKnowMoreWebLink,
            "",
            "",
        )
    }
}
