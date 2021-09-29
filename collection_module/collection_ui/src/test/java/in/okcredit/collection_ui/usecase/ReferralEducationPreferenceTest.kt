package `in`.okcredit.collection_ui.usecase

import `in`.okcredit.collection.contract.ReferralEducationPreference
import `in`.okcredit.merchant.collection.store.preference.CollectionPreference
import `in`.okcredit.merchant.collection.usecase.ReferralEducationPreferenceImpl
import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.anyOrNull
import com.nhaarman.mockitokotlin2.eq
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.whenever
import io.mockk.every
import io.mockk.mockkStatic
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.runBlocking
import org.junit.Before
import org.junit.Test

class ReferralEducationPreferenceTest {
    private val collectionPreference: CollectionPreference = mock()
    private lateinit var referralEducationPreference: ReferralEducationPreference

    @Before
    fun setUp() {
        referralEducationPreference = ReferralEducationPreferenceImpl { collectionPreference }

        mockkStatic(Dispatchers::class)
        every { Dispatchers.Default } returns Dispatchers.Unconfined
    }

    @Test
    fun `increase pref targeted_referral_education_shown and set when setReferralEducationShown is called`() {
        runBlocking {
            referralEducationPreference.setReferralEducationShown().test()

            verify(collectionPreference).increment(eq("targeted_referral_education_shown"), any())
        }
    }

    @Test
    fun `when count is less thn max value shouldShowReferralEducationScreen returns true`() {
        whenever(collectionPreference.getInt(eq("targeted_referral_education_shown"), any(), anyOrNull()))
            .thenReturn(flowOf(1))

        val testObserver = referralEducationPreference.shouldShowReferralEducationScreen().test()

        testObserver.assertValue { it }

        verify(collectionPreference).getInt(eq("targeted_referral_education_shown"), any(), anyOrNull())
    }

    @Test
    fun `when count is more thn max value shouldShowReferralEducationScreen returns false`() {
        whenever(collectionPreference.getInt(eq("targeted_referral_education_shown"), any(), anyOrNull()))
            .thenReturn(flowOf(2))

        val testObserver = referralEducationPreference.shouldShowReferralEducationScreen().test()

        testObserver.assertValue { !it }

        verify(collectionPreference).getInt(eq("targeted_referral_education_shown"), any(), anyOrNull())
    }
}
