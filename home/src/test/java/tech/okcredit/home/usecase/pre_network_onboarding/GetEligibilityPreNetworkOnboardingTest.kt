package tech.okcredit.home.usecase.pre_network_onboarding

import `in`.okcredit.backend._offline.database.CustomerRepo
import `in`.okcredit.merchant.contract.GetActiveBusinessId
import `in`.okcredit.merchant.suppliercredit.SupplierCreditRepository
import `in`.okcredit.onboarding.contract.OnboardingRepo
import com.google.common.truth.Truth
import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import com.nhaarman.mockitokotlin2.anyOrNull
import com.nhaarman.mockitokotlin2.argumentCaptor
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.whenever
import io.reactivex.Observable
import io.reactivex.Single
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.runBlocking
import org.junit.Before
import org.junit.Test
import tech.okcredit.android.ab.AbRepository
import tech.okcredit.home.TestData

@ExperimentalCoroutinesApi
class GetEligibilityPreNetworkOnboardingTest {
    private val mockSupplierCreditRepo: SupplierCreditRepository = mock()
    private val mockCustomerRepo: CustomerRepo = mock()
    private val mockOnboardingRepo: OnboardingRepo = mock()
    private val mockFirebaseRemoteConfig: FirebaseRemoteConfig = mock()
    private val mockAb: AbRepository = mock()
    private val getActiveBusinessId: GetActiveBusinessId = mock()

    private val getEligibilityPreNetworkOnboarding = GetEligibilityPreNetworkOnboarding(
        { mockSupplierCreditRepo },
        { mockCustomerRepo },
        { mockOnboardingRepo },
        { mockFirebaseRemoteConfig },
        { mockAb },
        { getActiveBusinessId }
    )

    @Before
    fun setup() {
        whenever(getActiveBusinessId.execute()).thenReturn(Single.just(TestData.BUSINESS_ID))
    }

    @Test
    fun `getEligibilityPreNetworkOnboarding return hideButtonAndNudges Boolean if user doesn't fall in the experiment or nor fresh Login`() {
        runBlocking {
            // given
            val expectedResponse = GetEligibilityPreNetworkOnboarding.PreNetworkOnboardingState(
                hideBigButtonAndNudge = false
            )
            val argumentCaptor = argumentCaptor<String>()
            val fakeString = "fake_string"

            whenever(mockOnboardingRepo.getIsNewUser()).thenReturn(false)
            whenever(mockAb.isExperimentEnabled(argumentCaptor.capture(), anyOrNull())).thenReturn(
                Observable.create {
                    it.onNext(false)
                }
            )
            whenever(mockAb.getExperimentVariant(argumentCaptor.capture(), anyOrNull())).thenReturn(
                Observable.create {
                    it.onNext(fakeString)
                }
            )

            val result = getEligibilityPreNetworkOnboarding.execute(expectedResponse.hideBigButtonAndNudge)

            Truth.assertThat(result).isEqualTo(expectedResponse)
        }
    }

    @Test
    fun `return eligibleForPreNetworkOnboardingNudges is true when supplier and customer count greater than 0 and saved in pref`() {
        runBlocking {
            // ----- Assumptions -------
            // user is fall in experiment
            // user is fall in test variant
            // firebase remote config 10
            // supplier count and customer count is greater than 0
            // supplier count and customer saved in pref, and fetch from shared pref
            // tooltip is not shown yet
            // has preNetwork relationships ids saved in pref

            // given
            val expectedResponse = GetEligibilityPreNetworkOnboarding.PreNetworkOnboardingState(
                hideBigButtonAndNudge = false,
                eligibleForNudges = true,
                delayInToolTipShown = 10,
                isPreNetworkUser = true
            )
            val expectedVariant = "prenetwork"
            val argumentCaptor = argumentCaptor<String>()
            val fakeRelationShipsIds = listOf("id_1", "id_2")
            val fakeCustomerCount = "2"
            val fakeSupplierCount = "3"

            whenever(mockOnboardingRepo.getIsNewUser()).thenReturn(true)
            whenever(mockAb.isExperimentEnabled(argumentCaptor.capture(), anyOrNull())).thenReturn(
                Observable.create {
                    it.onNext(true)
                }
            )
            whenever(mockAb.getExperimentVariant(argumentCaptor.capture(), anyOrNull())).thenReturn(
                Observable.create {
                    it.onNext(expectedVariant)
                }
            )

            whenever(mockFirebaseRemoteConfig.getLong(argumentCaptor.capture())).thenReturn(expectedResponse.delayInToolTipShown)

            whenever(mockOnboardingRepo.getPreNetworkCustomerCount()).thenReturn(fakeCustomerCount)
            whenever(mockOnboardingRepo.getPreNetworkSupplierCount()).thenReturn(fakeSupplierCount)

            whenever(mockOnboardingRepo.getIsRelationshipAddedAfterOnboarding()).thenReturn(
                Observable.create {
                    it.onNext(expectedResponse.hideBigButtonAndNudge)
                }
            )
            whenever(mockOnboardingRepo.getPreNetworkRelationships()).thenReturn(fakeRelationShipsIds)
            whenever(mockOnboardingRepo.getVisibilityPreNetworkOnboardingNudge()).thenReturn(false)

            val result = getEligibilityPreNetworkOnboarding.execute(expectedResponse.hideBigButtonAndNudge)

            Truth.assertThat(result).isEqualTo(
                expectedResponse.copy(
                    isPreNetworkUser = expectedResponse.isPreNetworkUser,
                    delayInToolTipShown = expectedResponse.delayInToolTipShown * 1000
                )
            )
        }
    }

    @Test
    fun `save preNetworkRelationships when user has preNetworkRelationships`() {
        runBlocking {
            // ----- Assumptions -------
            // user is fall in experiment
            // user is fall in test variant
            // firebase remote config 10
            // supplier count and customer count is greater than 0
            // supplier count and customer saved in pref, and fetch from shared pref
            // tooltip is not shown yet
            // no preNetwork RelationshipIds saved in pref

            // given
            val expectedResponse = GetEligibilityPreNetworkOnboarding.PreNetworkOnboardingState(
                hideBigButtonAndNudge = false,
                eligibleForNudges = true,
                delayInToolTipShown = 10,
                isPreNetworkUser = true
            )
            val expectedVariant = "prenetwork"
            val argumentCaptor = argumentCaptor<String>()
            val fakeSupplierIds = listOf("id_1")
            val fakeCustomerIds = listOf("id_2")
            val expectedPreNetworkRelationshipIds = mutableListOf<String>().apply {
                addAll(fakeSupplierIds)
                addAll(fakeCustomerIds)
            }
            val argumentCaptorForListOfString = argumentCaptor<List<String>>()
            val fakeCustomerCount = "2"
            val fakeSupplierCount = "3"

            whenever(mockOnboardingRepo.getIsNewUser()).thenReturn(true)
            whenever(mockAb.isExperimentEnabled(argumentCaptor.capture(), anyOrNull())).thenReturn(
                Observable.create {
                    it.onNext(true)
                }
            )
            whenever(mockAb.getExperimentVariant(argumentCaptor.capture(), anyOrNull())).thenReturn(
                Observable.create {
                    it.onNext(expectedVariant)
                }
            )

            whenever(mockFirebaseRemoteConfig.getLong(argumentCaptor.capture())).thenReturn(expectedResponse.delayInToolTipShown)

            whenever(mockOnboardingRepo.getPreNetworkCustomerCount()).thenReturn(fakeCustomerCount)
            whenever(mockOnboardingRepo.getPreNetworkSupplierCount()).thenReturn(fakeSupplierCount)

            whenever(mockOnboardingRepo.getIsRelationshipAddedAfterOnboarding()).thenReturn(
                Observable.create {
                    it.onNext(expectedResponse.hideBigButtonAndNudge)
                }
            )

            whenever(mockOnboardingRepo.getPreNetworkRelationships()).thenReturn(emptyList())
            whenever(mockOnboardingRepo.getVisibilityPreNetworkOnboardingNudge()).thenReturn(false)

            whenever(mockSupplierCreditRepo.listActiveSuppliersIds(TestData.BUSINESS_ID)).thenReturn(
                Observable.create {
                    it.onNext(fakeSupplierIds)
                }
            )
            whenever(mockCustomerRepo.listActiveCustomersIds(TestData.BUSINESS_ID)).thenReturn(
                Observable.create {
                    it.onNext(fakeCustomerIds)
                }
            )
            whenever(mockOnboardingRepo.savePreNetworkRelationships(argumentCaptorForListOfString.capture()))
                .thenReturn(Unit)

            // then
            val result = getEligibilityPreNetworkOnboarding.execute(expectedResponse.hideBigButtonAndNudge)

            Truth.assertThat(argumentCaptorForListOfString.firstValue).isEqualTo(expectedPreNetworkRelationshipIds)

            Truth.assertThat(result).isEqualTo(
                expectedResponse.copy(
                    isPreNetworkUser = expectedResponse.isPreNetworkUser,
                    delayInToolTipShown = expectedResponse.delayInToolTipShown * 1000
                )
            )
        }
    }
}
