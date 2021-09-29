package `in`.okcredit.frontend.ui.supplier_transaction_details

import `in`.okcredit.backend._offline.usecase.GetMerchantPreferenceImpl
import `in`.okcredit.frontend.usecase.GetSupplierCollection
import `in`.okcredit.frontend.usecase.supplier.GetSupplierTransaction
import `in`.okcredit.frontend.usecase.supplier.SyncSupplierTransaction
import `in`.okcredit.frontend.utils.PhoneBookUtils
import `in`.okcredit.individual.contract.PreferenceKey
import `in`.okcredit.merchant.contract.Business
import `in`.okcredit.merchant.contract.GetActiveBusiness
import `in`.okcredit.merchant.suppliercredit.Supplier
import `in`.okcredit.merchant.suppliercredit.Transaction
import `in`.okcredit.referral.contract.usecase.GetReferralLink
import `in`.okcredit.shared.base.BaseViewModel
import `in`.okcredit.shared.base.UiState
import `in`.okcredit.shared.usecase.CheckNetworkHealth
import `in`.okcredit.shared.usecase.Result
import `in`.okcredit.shared.usecase.UseCase
import `in`.okcredit.supplier.usecase.GetSupplier
import android.content.Context
import dagger.Lazy
import io.reactivex.Observable
import io.reactivex.Observable.mergeArray
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.subjects.PublishSubject
import merchant.okcredit.accounting.contract.usecases.GetCustomerSupportType
import tech.okcredit.base.dagger.di.qualifier.ViewModelParam
import tech.okcredit.feature_help.contract.GetSupportNumber
import timber.log.Timber
import java.util.concurrent.TimeUnit
import javax.inject.Inject

class SupplierTransactionViewModel @Inject constructor(
    private val getActiveBusiness: GetActiveBusiness,
    private val GetSupplierTransaction: GetSupplierTransaction,
    private val getSupplier: GetSupplier,
    private val syncSupplierTransaction: SyncSupplierTransaction,
    private val checkNetworkHealth: Lazy<CheckNetworkHealth>,
    @ViewModelParam("transaction_id") val transactionId: String?,
    val context: Context,
    private val getReferralLink: GetReferralLink,
    private val getMerchantPreference: GetMerchantPreferenceImpl,
    private val getSupplierCollection: GetSupplierCollection,
    private val getSupportNumber: Lazy<GetSupportNumber>,
    private val getCustomerSupportType: Lazy<GetCustomerSupportType>,
) : BaseViewModel<SupplierTransactionContract.State, SupplierTransactionContract.PartialState, SupplierTransactionContract.ViewEvent>(
    SupplierTransactionContract.State()
) {

    private var contactPermissionAvailable = false
    lateinit var supplier: Supplier
    lateinit var transaction: Transaction
    lateinit var business: Business
    private val getCollectionPublishSubject: PublishSubject<String> = PublishSubject.create()
    private val getSupplierPublishSubject: PublishSubject<String> = PublishSubject.create()
    private val getTransactionSubject: PublishSubject<String> = PublishSubject.create()

    override fun handle(): Observable<UiState.Partial<SupplierTransactionContract.State>> {
        return mergeArray(

            // hide network error when network becomes available
            intent<SupplierTransactionContract.Intent.Load>()
                .switchMap { checkNetworkHealth.get().execute(Unit) }
                .map {
                    if (it is Result.Success) {
                        // network connected
                        SupplierTransactionContract.PartialState.SetNetworkError(false)
                    } else {
                        SupplierTransactionContract.PartialState.NoChange
                    }
                },

            // handle `show alert` intent
            intent<SupplierTransactionContract.Intent.ShowAlert>()
                .switchMap {
                    Observable.timer(2, TimeUnit.SECONDS)
                        .map<SupplierTransactionContract.PartialState> { SupplierTransactionContract.PartialState.HideAlert }
                        .startWith(SupplierTransactionContract.PartialState.ShowAlert(it.message))
                },

            intent<SupplierTransactionContract.Intent.Load>()
                .map {
                    getTransactionSubject.onNext(transactionId!!)
                    SupplierTransactionContract.PartialState.NoChange
                },

            intent<SupplierTransactionContract.Intent.Load>()
                .switchMap { UseCase.wrapSingle(getReferralLink.execute()) }
                .map {
                    when (it) {
                        is Result.Progress -> SupplierTransactionContract.PartialState.NoChange
                        is Result.Success -> {
                            SupplierTransactionContract.PartialState.SetReferralId(it.value)
                        }
                        is Result.Failure -> {
                            when {
                                isInternetIssue(it.error) -> SupplierTransactionContract.PartialState.NoChange
                                else -> SupplierTransactionContract.PartialState.NoChange
                            }
                        }
                    }
                },

            // load page
            getCollectionPublishSubject
                .switchMap { getSupplierCollection.execute(it) }
                .map {
                    when (it) {
                        is Result.Progress -> SupplierTransactionContract.PartialState.NoChange
                        is Result.Success -> {
                            SupplierTransactionContract.PartialState.SetCollection(it.value)
                        }
                        is Result.Failure -> {
                            when {
                                isInternetIssue(it.error) -> SupplierTransactionContract.PartialState.NoChange
                                else -> SupplierTransactionContract.PartialState.ErrorState
                            }
                        }
                    }
                },

            // load page
            getTransactionSubject
                .switchMap { UseCase.wrapObservable(GetSupplierTransaction.execute(it)) }
                .map {
                    when (it) {
                        is Result.Progress -> SupplierTransactionContract.PartialState.NoChange
                        is Result.Success -> {
                            this.transaction = it.value

                            if (!it.value.collectionId.isNullOrBlank()) {
                                getCollectionPublishSubject.onNext(it.value.collectionId!!)
                            }

                            Timber.i(" ˆˆˆ accountId 1 =${it.value.supplierId}")
                            if (it.value.supplierId.isNotEmpty()) {
                                getSupplierPublishSubject.onNext(it.value.supplierId)
                            }
                            SupplierTransactionContract.PartialState.SetTransaction(it.value)
                        }
                        is Result.Failure -> {
                            when {
                                isInternetIssue(it.error) -> SupplierTransactionContract.PartialState.NoChange
                                else -> SupplierTransactionContract.PartialState.ErrorState
                            }
                        }
                    }
                },

            getSupplierPublishSubject
                .switchMap { getSupplier.execute(it) }
                .map {
                    when (it) {
                        is Result.Progress -> SupplierTransactionContract.PartialState.NoChange
                        is Result.Success -> {
                            supplier = it.value
                            Timber.i(" ˆˆˆ accountId 2 =${it.value}")
                            SupplierTransactionContract.PartialState.SetSupplier(it.value)
                        }
                        is Result.Failure -> {
                            when {
                                isInternetIssue(it.error) -> SupplierTransactionContract.PartialState.NoChange
                                else -> SupplierTransactionContract.PartialState.ErrorState
                            }
                        }
                    }
                },

            // load merchant
            intent<SupplierTransactionContract.Intent.Load>()
                .switchMap { getActiveBusiness.execute() }
                .map {
                    business = it
                    SupplierTransactionContract.PartialState.SetBusiness(it)
                },

            // sync tx
            intent<SupplierTransactionContract.Intent.SyncTransaction>()
                .switchMap {
                    syncSupplierTransaction.execute(transaction)
                }
                .map {
                    when (it) {
                        is Result.Progress -> SupplierTransactionContract.PartialState.SyncTransaction(true)
                        is Result.Success -> {
                            // when synced , we once again get transaction details
                            // this time we would read the the transaction (via id returned by server)
                            // because transaction with local id would be deleted when synced with server
                            // The result will change the text 'Sync Now' to 'Sync Successful'
                            getTransactionSubject.onNext(it.value)
                            SupplierTransactionContract.PartialState.SyncTransaction(false)
                        }
                        is Result.Failure -> {
                            when {
                                isInternetIssue(it.error) -> SupplierTransactionContract.PartialState.SetNetworkError(
                                    true
                                )
                                else -> SupplierTransactionContract.PartialState.ErrorState
                            }
                        }
                    }
                },

            // delete tx
            intent<SupplierTransactionContract.Intent.Delete>()
                .map {
                    emitViewEvent(SupplierTransactionContract.ViewEvent.GoToDeletePage(transactionId!!))
                    SupplierTransactionContract.PartialState.NoChange
                },

            // share tx
            intent<SupplierTransactionContract.Intent.ShareOnWhatsApp>()
                .map {
                    emitViewEvent(
                        SupplierTransactionContract.ViewEvent.GoToWhatsAppShare(
                            supplier,
                            business,
                            transaction
                        )
                    )
                    SupplierTransactionContract.PartialState.NoChange
                },
            intent<SupplierTransactionContract.Intent.Load>()
                .switchMap { wrap(GetSupplierTransaction.execute(transactionId!!)) }
                .map {
                    when (it) {
                        is Result.Progress -> SupplierTransactionContract.PartialState.NoChange
                        is Result.Success -> {

                            if (it.value.createdBySupplier) {
                                SupplierTransactionContract.PartialState.SetDeleteStatus(SupplierTransactionContract.DeleteLayoutStatus.InActive)
                            } else {
                                SupplierTransactionContract.PartialState.SetDeleteStatus(SupplierTransactionContract.DeleteLayoutStatus.Active)
                            }
                        }
                        is Result.Failure -> {
                            when {
                                isInternetIssue(it.error) -> SupplierTransactionContract.PartialState.NoChange
                                else -> SupplierTransactionContract.PartialState.ErrorState
                            }
                        }
                    }
                },
            intent<SupplierTransactionContract.Intent.OnKnowMoreClicked>()
                .map {
                    emitViewEvent(SupplierTransactionContract.ViewEvent.GoToKnowMoreScreen(it.id, "supplier"))
                    SupplierTransactionContract.PartialState.NoChange
                },

            /***********************  WhatsApp Us  ***********************/
            intent<SupplierTransactionContract.Intent.WhatsApp>()
                .switchMap {
                    contactPermissionAvailable = it.contactPermissionAvailable
                    UseCase.wrapSingle(
                        getMerchantPreference.execute(PreferenceKey.WHATSAPP)
                            .firstOrError()
                    )
                }
                .map {

                    when (it) {
                        is Result.Progress -> SupplierTransactionContract.PartialState.NoChange
                        is Result.Success -> {

                            val isWhatsAppEnabled = it.value!!.toBoolean()
                            if (isWhatsAppEnabled && contactPermissionAvailable) {

                                PhoneBookUtils.addOkCreditNumberToContact(context, getSupportNumber.get().supportNumber)
                                    .observeOn(AndroidSchedulers.mainThread())
                                    .subscribe {
                                        emitViewEvent(SupplierTransactionContract.ViewEvent.GoToWhatsApp(getSupportNumber.get().supportNumber))
                                    }
                            } else {
                                emitViewEvent(SupplierTransactionContract.ViewEvent.WhatsAppOptIn)
                            }
                            SupplierTransactionContract.PartialState.NoChange
                        }
                        is Result.Failure -> {
                            emitViewEvent(SupplierTransactionContract.ViewEvent.GoToWhatsApp(getSupportNumber.get().supportNumber))
                            SupplierTransactionContract.PartialState.NoChange
                        }
                    }
                },
            getCustomerSupportType()
        )
    }

    private fun getCustomerSupportType(): Observable<SupplierTransactionContract.PartialState> {
        return intent<SupplierTransactionContract.Intent.Load>()
            .switchMap {
                wrap(getCustomerSupportType.get().execute())
            }
            .map {
                if (it is Result.Success) {
                    SupplierTransactionContract.PartialState.SetSupportType(it.value)
                } else
                    SupplierTransactionContract.PartialState.NoChange
            }
    }

    override fun reduce(
        currentState: SupplierTransactionContract.State,
        partialState: SupplierTransactionContract.PartialState
    ): SupplierTransactionContract.State {
        return when (partialState) {
            is SupplierTransactionContract.PartialState.ShowLoading -> currentState.copy(isLoading = true)
            is SupplierTransactionContract.PartialState.SetTransaction -> currentState.copy(
                isLoading = false,
                transaction = partialState.transaction
            )
            is SupplierTransactionContract.PartialState.SetSupplier -> currentState.copy(
                isLoading = false,
                supplier = partialState.supplier
            )
            is SupplierTransactionContract.PartialState.ErrorState -> currentState.copy(
                isLoading = false,
                error = true,
                syncing = false
            )
            is SupplierTransactionContract.PartialState.ShowAlert -> currentState.copy(
                isAlertVisible = true,
                alertMessage = partialState.message
            )
            is SupplierTransactionContract.PartialState.HideAlert -> currentState.copy(isAlertVisible = false)
            is SupplierTransactionContract.PartialState.SetNetworkError -> currentState.copy(
                networkError = partialState.networkError,
                syncing = false
            )
            is SupplierTransactionContract.PartialState.SyncTransaction -> currentState.copy(syncing = partialState.syncing)
            is SupplierTransactionContract.PartialState.NoChange -> currentState
            is SupplierTransactionContract.PartialState.SetDeleteStatus -> currentState.copy(deleteStatus = partialState.deleteStatus)
            is SupplierTransactionContract.PartialState.SetBusiness -> currentState.copy(business = partialState.business)
            is SupplierTransactionContract.PartialState.SetReferralId -> currentState.copy(referralId = partialState.referralId)
            is SupplierTransactionContract.PartialState.SetCollection -> currentState.copy(collection = partialState.collection)
            is SupplierTransactionContract.PartialState.SetSupportType -> currentState.copy(supportType = partialState.type)
        }
    }
}
