package `in`.okcredit.collection_ui.ui.onlinepayments

import `in`.okcredit.collection.contract.CollectionOnlinePayment
import `in`.okcredit.collection_ui.CollectionUiTestData.COLLECTION_ONLINE_ITEM
import `in`.okcredit.collection_ui.CollectionUiTestData.COLLECTION_ONLINE_ITEM_2
import `in`.okcredit.collection_ui.ui.TestViewModel
import `in`.okcredit.collection_ui.ui.passbook.payments.DateFilterListDialog
import `in`.okcredit.collection_ui.ui.passbook.payments.OnlinePaymentsContract
import `in`.okcredit.collection_ui.ui.passbook.payments.OnlinePaymentsViewModel
import `in`.okcredit.collection_ui.usecase.GetNewOnlinePayments
import `in`.okcredit.collection_ui.usecase.GetOnlinePayments
import `in`.okcredit.collection_ui.usecase.GetTransactionIdFromCollection
import `in`.okcredit.merchant.collection.usecase.SetPaymentTagViewed
import `in`.okcredit.shared.base.BaseViewModel
import `in`.okcredit.shared.usecase.Result
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.whenever
import io.reactivex.Completable
import io.reactivex.Observable
import io.reactivex.Single
import org.joda.time.DateTime
import org.junit.Test

class OnlinePaymentsViewModelTest :
    TestViewModel<OnlinePaymentsContract.State, OnlinePaymentsContract.PartialState, OnlinePaymentsContract.ViewEvent>() {

    private val initialState: OnlinePaymentsContract.State = OnlinePaymentsContract.State()
    private val getOnlinePayments: GetOnlinePayments = mock()
    private val setPaymentTagViewed: SetPaymentTagViewed = mock()
    private val getNewOnlinePayments: GetNewOnlinePayments = mock()
    private val getTransactionIdFromCollection: GetTransactionIdFromCollection = mock()

    override fun createViewModel(): BaseViewModel<OnlinePaymentsContract.State, OnlinePaymentsContract.PartialState, OnlinePaymentsContract.ViewEvent> {
        return OnlinePaymentsViewModel(
            initialState = { initialState },
            getOnlinePayments = { getOnlinePayments },
            getTransactionIdFromCollection = { getTransactionIdFromCollection },
            setPaymentTagViewed = { setPaymentTagViewed },
            getNewOnlinePayments = { getNewOnlinePayments }
        )
    }

    @Test
    fun `getOnlinePayments() when result success`() {
        val startTime: DateTime = mock()
        val endTime: DateTime = mock()
        val request: GetOnlinePayments.Request = GetOnlinePayments.Request(startTime, endTime)

        val list: List<GetOnlinePayments.OnlineCollectionData> = listOf(mock(), mock())

        whenever(getOnlinePayments.execute(request)).thenReturn(Observable.just(Result.Success(list)))

        pushIntent(
            OnlinePaymentsContract.Intent.GetOnlinePayments(
                startTime,
                endTime
            )
        )

        assertLastState(initialState.copy(filteredList = list))
        assertLastViewEvent(OnlinePaymentsContract.ViewEvent.OnLoadDataSuccessEvent(true))
        verify(getOnlinePayments).execute(request)
    }

    @Test
    fun `getOnlinePayments() when result failure`() {
        val startTime: DateTime = mock()
        val endTime: DateTime = mock()
        val request: GetOnlinePayments.Request = GetOnlinePayments.Request(startTime, endTime)
        val mockError: Exception = mock()

        whenever(getOnlinePayments.execute(request)).thenReturn(Observable.just(Result.Failure(mockError)))

        pushIntent(
            OnlinePaymentsContract.Intent.GetOnlinePayments(
                startTime,
                endTime
            )
        )
        assertLastViewEvent(OnlinePaymentsContract.ViewEvent.OnError)
        verify(getOnlinePayments).execute(request)
    }

    @Test
    fun `setFilterRange()`() {
        val filter = DateFilterListDialog.Filters.Overall
        pushIntent(OnlinePaymentsContract.Intent.ChangeFilterRange(filter))
        assertLastState(initialState.copy(filter = filter))
    }

    @Test
    fun `setPaymentTagViewed()`() {
        whenever(setPaymentTagViewed.execute()).thenReturn(Completable.complete())
        pushIntent(OnlinePaymentsContract.Intent.SetPaymentTag)
        assertLastState(initialState.copy())
        verify(setPaymentTagViewed).execute()
    }

    @Test
    fun `getNewOnlinePaymentsCount()`() {
        val response = listOf<CollectionOnlinePayment>()
        whenever(getNewOnlinePayments.execute(Unit)).thenReturn(
            Observable.create {
                it.onNext(Result.Success(response))
            }
        )
        pushIntent(OnlinePaymentsContract.Intent.Load)
        assertLastState(initialState.copy())
        verify(getNewOnlinePayments).execute(Unit)
    }

    @Test
    fun `observeItemClicked() when status is 5`() {
        val collectionItem = COLLECTION_ONLINE_ITEM
        whenever(getTransactionIdFromCollection.execute(collectionItem.id)).thenReturn(
            Single.create {
                it.onSuccess(Pair("id", "1"))
            }
        )
        pushIntent(OnlinePaymentsContract.Intent.ItemClicked(collectionItem))
        assertLastViewEvent(OnlinePaymentsContract.ViewEvent.MoveToTransactionDetails("1"))
        verify(getTransactionIdFromCollection).execute(collectionItem.id)
    }

    @Test
    fun `observeItemClicked() when status is other 5 , 6 , 7 `() {
        val collectionItem = COLLECTION_ONLINE_ITEM_2
        pushIntent(OnlinePaymentsContract.Intent.ItemClicked(collectionItem))
        assertLastViewEvent(OnlinePaymentsContract.ViewEvent.MoveToPaymentDetails("id_2"))
    }
}
