package `in`.okcredit.collection_ui.ui.payment_detail

import `in`.okcredit.collection.contract.CollectionOnlinePayment
import `in`.okcredit.collection.contract.CollectionSyncer
import `in`.okcredit.collection.contract.GetCollectionMerchantProfile
import `in`.okcredit.collection.contract.SetOnlinePaymentStatusLocally
import `in`.okcredit.collection.contract.TriggerMerchantPayout
import `in`.okcredit.collection_ui.analytics.CollectionTracker
import `in`.okcredit.collection_ui.analytics.OnlineCollectionTracker
import `in`.okcredit.collection_ui.ui.passbook.detail.PaymentDetailContract
import `in`.okcredit.collection_ui.ui.passbook.detail.PaymentDetailViewModel
import `in`.okcredit.collection_ui.usecase.GetCollectionOnlinePayment
import `in`.okcredit.shared.usecase.Result
import android.content.Context
import android.content.Intent
import com.google.common.truth.Truth
import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.doNothing
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.whenever
import io.mockk.every
import io.mockk.mockkStatic
import io.reactivex.Observable
import io.reactivex.Single
import io.reactivex.observers.TestObserver
import io.reactivex.schedulers.Schedulers
import org.junit.Before
import org.junit.Test
import tech.okcredit.android.communication.CommunicationRepository
import tech.okcredit.android.communication.ShareIntentBuilder
import tech.okcredit.feature_help.contract.GetSupportNumber

class PaymentDetailViewModelTest {

    lateinit var testObserver: TestObserver<PaymentDetailContract.State>
    lateinit var paymentDetailViewModel: PaymentDetailViewModel

    private val initialState: PaymentDetailContract.State = PaymentDetailContract.State()
    private val getCollectionOnlinePayment: GetCollectionOnlinePayment = mock()
    private val communicationRepository: CommunicationRepository = mock()
    private val context: Context = mock()
    private val getSupportNumber: GetSupportNumber = mock()
    private val triggerMerchantPayout: TriggerMerchantPayout = mock()
    private val collectionSyncer: CollectionSyncer = mock()
    private val getCollectionMerchantProfile: GetCollectionMerchantProfile = mock()
    private val communicationApi: CommunicationRepository = mock()
    private val onlineCollectionTracker: OnlineCollectionTracker = mock()
    private val setOnlinePaymentStatusLocally: SetOnlinePaymentStatusLocally = mock()
    private val collectionTracker: CollectionTracker = mock()

    fun createViewModel(initialState: PaymentDetailContract.State) {
        paymentDetailViewModel = PaymentDetailViewModel(
            initialState = initialState,
            getCollectionOnlinePayment = { getCollectionOnlinePayment },
            communicationRepository = { communicationRepository },
            context = { context },
            getHelpNumber = { getSupportNumber },
            triggerMerchantPayout = { triggerMerchantPayout },
            getCollectionMerchantProfile = { getCollectionMerchantProfile },
            communicationApi = { communicationApi },
            onlineCollectionTracker = { onlineCollectionTracker },
        )
    }

    @Before
    fun setup() {
        createViewModel(initialState)

        testObserver = TestObserver()
        paymentDetailViewModel.state().subscribe(testObserver)

        mockkStatic(Schedulers::class)
        every { Schedulers.newThread() } returns Schedulers.trampoline()

        val firebaseCrashlytics: FirebaseCrashlytics = mock()
        doNothing().whenever(firebaseCrashlytics).recordException(any())

        mockkStatic(FirebaseCrashlytics::class)
        every { FirebaseCrashlytics.getInstance() } returns firebaseCrashlytics
    }

    @Test
    fun `getOnlinePayment() should fetch data`() {
        val collectionOnlinePayment: CollectionOnlinePayment = mock()
        whenever(getCollectionOnlinePayment.execute("id")).thenReturn(
            Observable.just(
                Result.Success(
                    collectionOnlinePayment
                )
            )
        )

        paymentDetailViewModel.attachIntents(Observable.just(PaymentDetailContract.Intent.Load))

        Truth.assertThat(testObserver.values().contains(initialState))
        Truth.assertThat(
            testObserver.values().last() == initialState.copy(collectionOnlinePayment = collectionOnlinePayment)
        )
        testObserver.dispose()
    }

    @Test
    fun `sendWhatsApp() should not change state`() {
        val shareIntentBuilder: ShareIntentBuilder = mock()
        whenever(communicationRepository.goToWhatsApp(shareIntentBuilder)).thenReturn(Single.just(Intent()))

        paymentDetailViewModel.attachIntents(Observable.just(PaymentDetailContract.Intent.SendWhatsApp(mock())))

        Truth.assertThat(testObserver.values().contains(initialState))
        Truth.assertThat(
            testObserver.values().last() == initialState
        )
        testObserver.dispose()
    }
}
