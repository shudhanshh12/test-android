package `in`.okcredit.frontend.usecase

import `in`.okcredit.shared.service.keyval.KeyValService
import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.eq
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.whenever
import io.reactivex.Observable
import io.reactivex.Single
import org.junit.Test
import tech.okcredit.android.base.preferences.DefaultPreferences.Keys.PREF_INDIVIDUAL_KEY_SERVER_VERSION

class GetKnowMoreVideoLinksTest {

    private val keyValService: KeyValService = mock()

    private val getKnowMoreVideoLinks = GetKnowMoreVideoLinks(keyValService)

    @Test
    fun `should return vidoes if present and not null`() {
        whenever(keyValService.contains(eq(PREF_INDIVIDUAL_KEY_SERVER_VERSION), any())).thenReturn(Single.just(true))

        whenever(keyValService.get(eq(PREF_INDIVIDUAL_KEY_SERVER_VERSION), any())).thenReturn(Observable.just(getDummyResponse()))
        val testObserver = getKnowMoreVideoLinks.execute(Unit).test()

        testObserver.assertValues(
            `in`.okcredit.shared.usecase.Result.Progress(),
            `in`.okcredit.shared.usecase.Result.Success(
                GetKnowMoreVideoLinks.Response(
                    "rtYGwt8HR7Y",
                    "pdnSrr6Z1yk"
                )
            )
        )

        testObserver.dispose()
    }

    @Test
    fun `should return null if videos not present `() {
        whenever(keyValService.contains(eq(PREF_INDIVIDUAL_KEY_SERVER_VERSION), any())).thenReturn(Single.just(true))

        whenever(keyValService.get(eq(PREF_INDIVIDUAL_KEY_SERVER_VERSION), any())).thenReturn(
            Observable.just(
                getNullDummyResponse()
            )
        )
        val testObserver = getKnowMoreVideoLinks.execute(Unit).test()

        testObserver.assertValues(
            `in`.okcredit.shared.usecase.Result.Progress(),
            `in`.okcredit.shared.usecase.Result.Success(
                GetKnowMoreVideoLinks.Response(
                    null,
                    null
                )
            )
        )
        testObserver.dispose()
    }

    @Test
    fun `should return progress only if keyValService do not conatin  KEY_SERVER_VERSION`() {
        whenever(keyValService.contains(eq(PREF_INDIVIDUAL_KEY_SERVER_VERSION), any())).thenReturn(Single.just(false))

        whenever(keyValService.get(eq(PREF_INDIVIDUAL_KEY_SERVER_VERSION), any())).thenReturn(Observable.just(getDummyResponse()))
        val testObserver = getKnowMoreVideoLinks.execute(Unit).test()

        testObserver.assertValues(
            `in`.okcredit.shared.usecase.Result.Progress()
        )

        testObserver.dispose()
    }

    private fun getNullDummyResponse(): String? {
        return "{\"ca_education_video_1\":\"7OQL18w-M34\",\"has_new_features\":true,\"help_number\":\"8296508123\",\"intro_video\":\"K-SRk1rB31A\",\"lp_education_video_1\":\"Q0gTf0pKiCQ\",\"otp_number\":\"9513133390\",\"sc_education_video_1\":\"wJ11kmdxs8M\",\"sc_education_video_2\":\"vGr6CMC9xcc\",\"sc_intro_video\":\"19srF4nRX3w\",\"setup_collection_video\":\"Idp3cuzJ0zM\",\"supplier_learn_more_web_link\":\"https://account.okcredit.in/static/faq/supplier\",\"version\":151}"
    }

    private fun getDummyResponse(): String? {
        return "{\"ca_education_video_1\":\"7OQL18w-M34\",\"common_ledger_buyer_video\":\"rtYGwt8HR7Y\",\"common_ledger_seller_video\":\"pdnSrr6Z1yk\",\"has_new_features\":true,\"help_number\":\"8296508123\",\"intro_video\":\"K-SRk1rB31A\",\"lp_education_video_1\":\"Q0gTf0pKiCQ\",\"otp_number\":\"9513133390\",\"sc_education_video_1\":\"wJ11kmdxs8M\",\"sc_education_video_2\":\"vGr6CMC9xcc\",\"sc_intro_video\":\"19srF4nRX3w\",\"setup_collection_video\":\"Idp3cuzJ0zM\",\"supplier_learn_more_web_link\":\"https://account.okcredit.in/static/faq/supplier\",\"version\":151}"
    }
}
