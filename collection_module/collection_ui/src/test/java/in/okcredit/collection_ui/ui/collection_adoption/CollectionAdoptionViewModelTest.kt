package `in`.okcredit.collection_ui.ui.collection_adoption

import `in`.okcredit.collection.contract.CollectionMerchantProfile
import `in`.okcredit.collection.contract.GetCollectionMerchantProfile
import `in`.okcredit.collection.contract.SetCollectionDestination
import `in`.okcredit.collection_ui.analytics.CollectionTracker
import `in`.okcredit.collection_ui.ui.home.adoption.CollectionAdoptionContract
import `in`.okcredit.collection_ui.ui.home.adoption.CollectionAdoptionViewModel
import `in`.okcredit.collection_ui.usecase.GetCollectionAdoptionV3Expt
import `in`.okcredit.collection_ui.usecase.GetMerchantQRIntent
import `in`.okcredit.collection_ui.usecase.SaveMerchantQROnDevice
import `in`.okcredit.merchant.contract.Business
import `in`.okcredit.merchant.contract.GetActiveBusiness
import android.content.Intent
import com.google.common.truth.Truth.assertThat
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.whenever
import io.mockk.every
import io.mockk.mockkStatic
import io.reactivex.Observable
import io.reactivex.Single
import io.reactivex.observers.TestObserver
import io.reactivex.schedulers.Schedulers
import org.junit.Before
import org.junit.Test

class CollectionAdoptionViewModelTest {
    lateinit var collectionAdoptionViewModel: CollectionAdoptionViewModel

    private val getCollectionMerchantProfile: GetCollectionMerchantProfile = mock()
    private val getActiveBusiness: GetActiveBusiness = mock()
    private val setCollectionDestination: SetCollectionDestination = mock()
    private val getMerchantQRIntent: GetMerchantQRIntent = mock()
    private val saveMerchantQROnDevice: SaveMerchantQROnDevice = mock()
    private val tracker: CollectionTracker = mock()
    private val business: Business = mock()
    private val intent: Intent = mock()
    private val getCollectionAdoptionV3Expt: GetCollectionAdoptionV3Expt = mock()
    private val viewEffectObserver = TestObserver<CollectionAdoptionContract.ViewEvent>()

    fun createViewModel(initialState: CollectionAdoptionContract.State) {
        collectionAdoptionViewModel = CollectionAdoptionViewModel(
            initialState = initialState,
            sourceScreen = "",
            rewardsAmount = 0,
            redirectToRewardsPage = false,
            getCollectionMerchantProfile = { getCollectionMerchantProfile },
            getActiveBusiness = { getActiveBusiness },
            setCollectionDestination = { setCollectionDestination },
            getMerchantQRIntent = { getMerchantQRIntent },
            saveMerchantQROnDevice = { saveMerchantQROnDevice },
            tracker = { tracker },
            getCollectionAdoptionV3Expt = { getCollectionAdoptionV3Expt }
        )
    }

    @Before
    fun setup() {
        mockkStatic(Schedulers::class)
        every { Schedulers.newThread() } returns Schedulers.trampoline()
        createViewModel(CollectionAdoptionContract.State())
        collectionAdoptionViewModel.viewEvent().subscribe(viewEffectObserver)
    }

    @Test
    fun `on Load set CollectionMerchantProfile`() {
        // Given
        val initialState = CollectionAdoptionContract.State()
        createViewModel(initialState)

        whenever(getCollectionMerchantProfile.execute()).thenReturn(Observable.just(CollectionMerchantProfile.empty()))
        whenever(getActiveBusiness.execute()).thenReturn(Observable.just(business))

        whenever(getCollectionAdoptionV3Expt.execute()).thenReturn(Observable.just(true))
        // when
        collectionAdoptionViewModel.attachIntents(Observable.just(CollectionAdoptionContract.Intent.Load))
        val testObserver = collectionAdoptionViewModel.state().test()

        // then
        assertThat(testObserver.values().get(0).collectionMerchantProfile?.equals(CollectionMerchantProfile.empty()))
        testObserver.values().contains(initialState)
        testObserver.values().contains(
            CollectionAdoptionContract.State(
                collectionMerchantProfile = CollectionMerchantProfile.empty(),
                business = business,
                isCollectionV3Enabled = true
            )
        )

        verify(getCollectionMerchantProfile).execute()
        verify(getActiveBusiness).execute()

        testObserver.dispose()
    }

    @Test
    fun `should go to OTP screen for VerifyOtp Intent`() {
        // Given
        val initialState = CollectionAdoptionContract.State()
        createViewModel(initialState)

        // When
        val testObserver = collectionAdoptionViewModel.viewEvent().test()
        collectionAdoptionViewModel.attachIntents(Observable.just(CollectionAdoptionContract.Intent.VerifyOtp))

        assertThat(
            testObserver.values().last() == CollectionAdoptionContract.ViewEvent.GoToOptVerification
        ).isTrue()
    }

    @Test
    fun `should share Merchant Intent for ShareMerchantQR Intent`() {
        // Given
        val initialState = CollectionAdoptionContract.State()
        createViewModel(initialState)
        whenever(getMerchantQRIntent.execute()).thenReturn(Single.just(intent))

        // When
        val testObserver = collectionAdoptionViewModel.viewEvent().test()
        collectionAdoptionViewModel.attachIntents(Observable.just(CollectionAdoptionContract.Intent.ShareMerchantQR))

        assertThat(
            testObserver.values().last() == CollectionAdoptionContract.ViewEvent.ShareMerchantIntent(intent)
        ).isTrue()
    }
}
