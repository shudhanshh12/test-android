package `in`.okcredit.merchant.customer_ui.ui.payment

import `in`.okcredit.backend.contract.GetCustomer
import `in`.okcredit.collection.contract.*
import `in`.okcredit.collection.contract.Collection
import `in`.okcredit.merchant.customer_ui.R
import `in`.okcredit.merchant.customer_ui.ui.payment.AddCustomerPaymentContract.*
import `in`.okcredit.merchant.customer_ui.usecase.GetCollectionsOfCustomerOrSupplier
import `in`.okcredit.shared.base.BaseViewModel
import `in`.okcredit.shared.usecase.Result
import dagger.Lazy
import io.reactivex.Observable
import org.joda.time.DateTime
import tech.okcredit.android.base.utils.DateTimeUtils
import tech.okcredit.base.dagger.di.qualifier.ViewModelParam
import javax.inject.Inject

class AddCustomerPaymentViewModel @Inject constructor(
    initialState: State,
    @ViewModelParam("expanded_qr") val expandedQr: Boolean,
    private val getCustomer: Lazy<GetCustomer>,
    private val collectionRepository: Lazy<CollectionRepository>,
    private val getCustomerCollectionProfile: Lazy<GetCustomerCollectionProfile>,
    private val showExpandedQrInAddPayment: Lazy<ShowExpandedQrInAddPayment>,
    private val getKycRiskCategory: Lazy<GetKycRiskCategory>,
    private val getKycStatus: Lazy<GetKycStatus>,
    private val getCollectionsOfCustomerOrSupplier: Lazy<GetCollectionsOfCustomerOrSupplier>,
    private val checkForNewCustomerCollections: Lazy<CheckForNewCustomerCollections>,
    private val addCustomerPaymentEventsTracker: Lazy<AddCustomerPaymentEventsTracker>,
) : BaseViewModel<State, PartialState, ViewEvent>(initialState) {

    private var loadTimeStamp: DateTime = DateTime()

    override fun handle(): Observable<PartialState> = Observable.mergeArray(
        observeCollectionActivated(),
        observeExpandedQr(),
        loadCustomerDetails(),
        loadCustomerCollectionProfile(),
        getKycRiskCategory(),
        observeOnlinePaymentReceived(),
        observeCustomerCollectionsReceived(),
        observeShowCustomerQrTapped(),
        observeMinimizeCustomerQr(),
    )

    private fun observeShowCustomerQrTapped() = intent<Intent.ShowQrTapped>().map {
        addCustomerPaymentEventsTracker.get().trackShowCustomerQr(getCurrentState().customerId)
        PartialState.NoChange
    }

    private fun observeMinimizeCustomerQr() = intent<Intent.MinimizeQr>().map {
        addCustomerPaymentEventsTracker.get().trackCustomerQrMinimized(getCurrentState().customerId)
        PartialState.NoChange
    }

    private fun observeCollectionActivated() = intent<Intent.Load>().switchMap {
        collectionRepository.get().isCollectionActivated()
    }.map {
        PartialState.SetCollectionActivated(it)
    }

    private fun getKycRiskCategory() = intent<Intent.Load>()
        .switchMap { getKycDetails() }
        .map {
            PartialState.SetKycRiskCategory(
                it.first,
                it.second.kycRiskCategory,
                it.second.isLimitReached
            )
        }

    private fun getKycDetails() = Observable.zip(
        getKycStatus.get().execute(),
        getKycRiskCategory.get().execute(),
        { kycStatus, kycRisk ->
            Pair(kycStatus, kycRisk)
        }
    )

    private fun observeExpandedQr() = intent<Intent.Load>().switchMap {
        wrap {
            if (expandedQr) {
                addCustomerPaymentEventsTracker.get().trackViewQr(getCurrentState().source)
                true
            } else {
                showExpandedQrInAddPayment.get().execute()
            }
        }
    }.map {
        if (it is Result.Success && it.value) {
            addCustomerPaymentEventsTracker.get().trackShowCustomerQr(getCurrentState().customerId)
            emitViewEvent(ViewEvent.ExpandQr)
        }
        PartialState.NoChange
    }

    private fun loadCustomerDetails() = intent<Intent.Load>().switchMap {
        wrap(getCustomer.get().execute(getCurrentState().customerId).firstOrError())
    }.map {
        when (it) {
            is Result.Success -> {
                PartialState.CustomerData(it.value)
            }
            is Result.Failure -> {
                when {
                    isInternetIssue(it.error) -> {
                        PartialState.NoChange
                    }
                    else -> {
                        emitViewEvent(ViewEvent.ShowError(R.string.err_default))
                        PartialState.NoChange
                    }
                }
            }
            is Result.Progress -> PartialState.ShowProgress
        }
    }

    private fun loadCustomerCollectionProfile() = intent<Intent.Load>()
        .switchMap {
            wrap(getCustomerCollectionProfile.get().execute(getCurrentState().customerId))
        }
        .map {
            return@map when (it) {
                is Result.Progress -> PartialState.NoChange
                is Result.Success -> {
                    PartialState.SetCustomerCollectionProfile(it.value)
                }
                is Result.Failure -> {
                    when {
                        isInternetIssue(it.error) -> {
                            PartialState.NoChange
                        }
                        else -> {
                            emitViewEvent(ViewEvent.ShowError(R.string.err_default))
                            PartialState.NoChange
                        }
                    }
                }
            }
        }

    private fun observeCustomerCollectionsReceived() = intent<Intent.Load>()
        .switchMap {
            loadTimeStamp = DateTimeUtils.currentDateTime()
            wrap(getCollectionsOfCustomerOrSupplier.get().execute(customerId = getCurrentState().customerId))
        }
        .map {
            if (it is Result.Success) {
                checkForNewOnlinePayment(it.value)
                return@map PartialState.SetCustomerCollections(it.value)
            }
            PartialState.NoChange
        }

    private fun observeOnlinePaymentReceived() = intent<Intent.Load>()
        .switchMap {
            wrap { checkForNewCustomerCollections.get().execute() }
        }.map {
            PartialState.NoChange
        }

    private fun checkForNewOnlinePayment(newList: List<Collection>) {
        // list being loaded for first time return
        if (newList.isEmpty()) return

        // no new txn have been added, probably change in existing transactions
        if (newList.size == getCurrentState().customerCollections.size) {
            return
        }

        // check difference between new list and current list
        val diff = newList.subtract(getCurrentState().customerCollections)
        if (diff.size == 1) {
            val newCollection = diff.first()
            if (newCollection.status == CollectionStatus.PAID && newCollection.create_time.isAfter(loadTimeStamp.millis)) {
                addCustomerPaymentEventsTracker.get().trackPaymentSuccessScreenShown(
                    accountId = getCurrentState().customerId,
                    amount = newCollection.amount_collected ?: 0L,
                    transactionId = newCollection.id,
                )
                emitViewEvent(
                    ViewEvent.ShowPaymentReceived(
                        newCollection.id,
                        newCollection.amount_collected ?: 0L,
                        newCollection.create_time.millis
                    )
                )
            }
        }
    }

    override fun reduce(
        currentState: State,
        partialState: PartialState,
    ): State {
        return when (partialState) {
            PartialState.NoChange -> currentState
            is PartialState.CustomerData -> currentState.copy(
                customerName = partialState.customer.description,
                customerProfile = partialState.customer.profileImage,
                balanceDue = partialState.customer.balanceV2,
                loading = false
            )
            is PartialState.SetCustomerCollectionProfile -> currentState.copy(
                qrIntent = partialState.customerCollectionProfile.qr_intent
            )
            PartialState.ShowProgress -> currentState.copy(loading = true)
            is PartialState.SetKycRiskCategory -> currentState.copy(
                kycLimitReached = partialState.limitReached,
                kycRiskCategory = partialState.kycRiskCategory,
                kycStatus = partialState.kycStatus,
            )
            is PartialState.SetCollectionActivated -> currentState.copy(showQrLocked = !partialState.activated)
            is PartialState.SetCustomerCollections -> currentState.copy(customerCollections = partialState.value)
        }
    }
}
