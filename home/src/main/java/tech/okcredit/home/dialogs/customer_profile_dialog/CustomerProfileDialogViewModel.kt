package tech.okcredit.home.dialogs.customer_profile_dialog

import `in`.okcredit.analytics.Event
import `in`.okcredit.analytics.PropertiesMap
import `in`.okcredit.analytics.PropertyKey
import `in`.okcredit.analytics.PropertyValue
import `in`.okcredit.analytics.PropertyValue.CUSTOMER_SYNC_STATUS
import `in`.okcredit.analytics.Tracker
import `in`.okcredit.backend._offline.usecase.GetPaymentReminderIntent
import `in`.okcredit.backend.contract.Customer
import `in`.okcredit.collection.contract.GetKycRiskCategory
import `in`.okcredit.collection.contract.GetKycStatus
import `in`.okcredit.merchant.contract.GetActiveBusiness
import `in`.okcredit.merchant.core.model.Customer.CustomerSyncStatus.*
import `in`.okcredit.shared.base.BasePresenter
import `in`.okcredit.shared.base.UiState
import `in`.okcredit.shared.usecase.Result
import `in`.okcredit.shared.usecase.UseCase
import dagger.Lazy
import io.reactivex.Observable
import io.reactivex.exceptions.CompositeException
import tech.okcredit.android.communication.handlers.IntentHelper
import tech.okcredit.base.dagger.di.qualifier.ViewModelParam
import tech.okcredit.home.dialogs.customer_profile_dialog.CustomerProfileDialogContract.*
import tech.okcredit.home.usecase.GetCustomerAndCollectionProfile
import javax.inject.Inject

class CustomerProfileDialogViewModel @Inject constructor(
    initialState: State,
    @ViewModelParam(CustomerProfileDialogContract.ARG_CUSTOMER_ID) val customerId: String,
    private val getActiveBusiness: Lazy<GetActiveBusiness>,
    private val getPaymentReminderIntent: Lazy<GetPaymentReminderIntent>,
    private val navigator: Lazy<Navigator>,
    private val getCollAndCollectionProfile: Lazy<GetCustomerAndCollectionProfile>,
    private val getKycRiskCategory: Lazy<GetKycRiskCategory>,
    private val getKycStatus: Lazy<GetKycStatus>,
    private val tracker: Lazy<Tracker>,
) : BasePresenter<State, PartialState>(
    initialState
) {
    override fun handle(): Observable<out UiState.Partial<State>> {
        return Observable.mergeArray(

            intent<Intent.Load>()
                .switchMap { UseCase.wrapObservable(getActiveBusiness.get().execute()) }
                .map {
                    when (it) {
                        is Result.Progress -> PartialState.NoChange
                        is Result.Success -> PartialState.SetBusiness(it.value)
                        is Result.Failure -> PartialState.NoChange
                    }
                },

            intent<Intent.Load>()
                .switchMap { UseCase.wrapObservable(getCollAndCollectionProfile.get().execute(customerId)) }
                .map {
                    when (it) {
                        is Result.Progress -> PartialState.NoChange
                        is Result.Success -> {
                            PartialState.SetCustomerAndBusiness(
                                it.value.customer,
                                it.value.collectionCustomerProfile,
                                it.value.cleanCompanionDescription,
                            )
                        }
                        is Result.Failure -> {
                            if (!isInternetIssue(it.error)) {
                                navigator.get().goBack()
                            }
                            PartialState.NoChange
                        }
                    }
                },

            intent<Intent.SendWhatsAppReminder>()
                .switchMap {
                    UseCase.wrapSingle(
                        getPaymentReminderIntent.get().execute(customerId, "home", it.reminderMode)
                    )
                }
                .map {
                    when (it) {
                        is Result.Progress -> PartialState.NoChange
                        is Result.Success -> {
                            navigator.get().shareReminder(it.value)
                            PartialState.NoChange
                        }
                        is Result.Failure -> {
                            when {
                                it.error is IntentHelper.NoWhatsAppError ||
                                    (it.error as? CompositeException)
                                    ?.exceptions
                                    ?.find { e -> e is IntentHelper.NoWhatsAppError } != null
                                -> {
                                    navigator.get().showWhatsAppNotInstalled()
                                    PartialState.NoChange
                                }
                                isAuthenticationIssue(it.error) -> {
                                    navigator.get().gotoLogin()
                                    PartialState.NoChange
                                }
                                else -> {
                                    PartialState.NoChange
                                }
                            }
                        }
                    }
                },

            getKycRiskCategory()
        )
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

    override fun reduce(
        currentState: State,
        partialState: PartialState,
    ): State {
        return when (partialState) {
            is PartialState.SetCustomerAndBusiness -> {
                tracker.get().trackEvents(
                    Event.PROFILE_POP_UP_DISPLAYED,
                    screen = PropertyValue.HOME_PAGE_SCREEN,
                    relation = PropertyValue.CUSTOMER,
                    propertiesMap = PropertiesMap.create()
                        .add(PropertyKey.ACCOUNT_ID, customerId)
                        .add(CUSTOMER_SYNC_STATUS, getCustomerSyncStatus(partialState.customer))
                )
                currentState.copy(
                    customer = partialState.customer,
                    collectionCustomerProfile = partialState.collectionCustomerProfile,
                    cleanCompanionDescription = partialState.cleanCompanionDescription
                )
            }
            is PartialState.SetBusiness -> currentState.copy(business = partialState.business)
            is PartialState.NoChange -> currentState
            is PartialState.SetKycRiskCategory -> currentState.copy(
                kycStatus = partialState.kycStatus,
                kycRiskCategory = partialState.kycRiskCategory,
                isKycLimitReached = partialState.isKycLimitReached
            )
        }
    }

    private fun getCustomerSyncStatus(customer: Customer?): String {
        return when (customer?.customerSyncStatus) {
            IMMUTABLE.code -> "Immutable"
            DIRTY.code -> "Dirty"
            CLEAN.code -> "Clean"
            else -> "Unknown"
        }
    }
}
