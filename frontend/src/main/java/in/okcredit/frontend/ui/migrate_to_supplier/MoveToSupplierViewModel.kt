package `in`.okcredit.frontend.ui.migrate_to_supplier

import `in`.okcredit.analytics.Tracker
import `in`.okcredit.backend.contract.GetCustomer
import `in`.okcredit.frontend.ui.MainActivity
import `in`.okcredit.frontend.usecase.MigrateRelation
import `in`.okcredit.merchant.contract.GetActiveBusinessId
import `in`.okcredit.merchant.customer_ui.usecase.IsSupplierCreditEnabledCustomer
import `in`.okcredit.shared.base.BasePresenter
import `in`.okcredit.shared.base.UiState
import `in`.okcredit.shared.usecase.Result
import `in`.okcredit.shared.usecase.UseCase
import dagger.Lazy
import io.reactivex.Observable
import io.reactivex.subjects.PublishSubject
import tech.okcredit.base.dagger.di.qualifier.ViewModelParam
import timber.log.Timber
import javax.inject.Inject

class MoveToSupplierViewModel @Inject constructor(
    initialState: MoveToSupplierContract.State,
    @ViewModelParam(MainActivity.ARG_CUSTOMER_ID) val customerId: String,
    private val getCustomer: GetCustomer,
    private val getActiveBusinessId: Lazy<GetActiveBusinessId>,
    private val migrateRelation: MigrateRelation,
    private val navigator: MoveToSupplierContract.Navigator,
    private val tracker: Tracker,
    private val isSupplierCreditEnabledCustomer: Lazy<IsSupplierCreditEnabledCustomer>,
) : BasePresenter<MoveToSupplierContract.State, MoveToSupplierContract.PartialState>(initialState) {

    private var customerMobile: String? = null
    private var id: String = ""
    private var progressSubject = PublishSubject.create<Int>()
    private var merchantId: String = ""

    override fun handle(): Observable<out UiState.Partial<MoveToSupplierContract.State>> {
        return Observable.mergeArray(
            intent<MoveToSupplierContract.Intent.Load>()
                .switchMap { UseCase.wrapSingle(getActiveBusinessId.get().execute()) }
                .map {
                    when (it) {
                        is Result.Progress -> MoveToSupplierContract.PartialState.NoChange
                        is Result.Success -> {
                            merchantId = it.value
                            MoveToSupplierContract.PartialState.NoChange
                        }
                        is Result.Failure -> {
                            when {
                                isAuthenticationIssue(it.error) -> {
                                    navigator.gotoLogin()
                                    MoveToSupplierContract.PartialState.NoChange
                                }
                                else -> {
                                    Timber.e(it.error, "ErrorState")
                                    MoveToSupplierContract.PartialState.ErrorState
                                }
                            }
                        }
                    }
                },
            intent<MoveToSupplierContract.Intent.Load>()
                .switchMap { UseCase.wrapObservable(getCustomer.execute(customerId)) }
                .map {
                    when (it) {
                        is Result.Progress -> MoveToSupplierContract.PartialState.NoChange
                        is Result.Success -> {
                            customerMobile = it.value.mobile
                            MoveToSupplierContract.PartialState.SetCustomer(it.value)
                        }
                        is Result.Failure -> {
                            when {
                                isAuthenticationIssue(it.error) -> {
                                    navigator.gotoLogin()
                                    MoveToSupplierContract.PartialState.NoChange
                                }
                                isInternetIssue(it.error) -> {
                                    MoveToSupplierContract.PartialState.SetNetworkError(true)
                                }
                                else -> {
                                    Timber.e(it.error, "ErrorState")
                                    MoveToSupplierContract.PartialState.ErrorState
                                }
                            }
                        }
                    }
                },
            intent<MoveToSupplierContract.Intent.Migrate>()
                .switchMap {
                    migrateRelation.execute(
                        MigrateRelation.Request(
                            merchantId,
                            customerId,
                            2,
                            progressSubject
                        )
                    )
                }
                .map {
                    when (it) {
                        is Result.Progress -> {
                            MoveToSupplierContract.PartialState.SetMigrationStates(
                                canShowLoaderScreen = true,
                                canShowSuccessfulScreen = false,
                                canShowFailureScreen = false,
                                migragtedAccountId = null,
                                errorMessage = null
                            )
                        }
                        is Result.Success -> {
                            id = it.value
                            tracker.trackRelationMigrationSuccess(
                                "Customer",
                                "Supplier",
                                "Relationship Migration Screen",
                                customerId,
                            )

                            MoveToSupplierContract.PartialState.SetMigrationStates(
                                canShowLoaderScreen = false,
                                canShowSuccessfulScreen = true,
                                canShowFailureScreen = false,
                                migragtedAccountId = id,
                                errorMessage = null
                            )
                        }
                        is Result.Failure -> {
                            tracker.trackRelationMigrationFailed(
                                "Customer",
                                "Supplier",
                                "Relationship Migration Screen",
                                customerId,
                                it.error.message
                            )

                            if (it.error.message == "account_migration_not_permitted") {
                                MoveToSupplierContract.PartialState.SetMigrationStates(
                                    canShowLoaderScreen = false,
                                    canShowSuccessfulScreen = false,
                                    canShowFailureScreen = true,
                                    migragtedAccountId = null,
                                    errorMessage = "account_migration_not_permitted"
                                )
                            } else if (it.error.message == "cyclic_account_exists") {
                                MoveToSupplierContract.PartialState.SetMigrationStates(
                                    canShowLoaderScreen = false,
                                    canShowSuccessfulScreen = false,
                                    canShowFailureScreen = true,
                                    migragtedAccountId = null,
                                    errorMessage = "cyclic_account_exists"
                                )
                            } else MoveToSupplierContract.PartialState.SetMigrationStates(
                                canShowLoaderScreen = false,
                                canShowSuccessfulScreen = false,
                                canShowFailureScreen = true,
                                migragtedAccountId = null,
                                errorMessage = null
                            )
                        }
                    }
                },
            intent<MoveToSupplierContract.Intent.ShowConfirmDialog>()
                .map {
                    navigator.showConfirmDialog()
                    MoveToSupplierContract.PartialState.NoChange
                },
            progressSubject.map { value ->
                // some ugly logic not to show 100 percent, we will show 100 percent once the timer is completed
                if (value == 100) {
                    MoveToSupplierContract.PartialState.SetLoaderProgess(90)
                }
                MoveToSupplierContract.PartialState.SetLoaderProgess(value)
            },
            observeSupplierCreditEnabled()
        )
    }

    private fun observeSupplierCreditEnabled() = intent<MoveToSupplierContract.Intent.Load>().switchMap {
        UseCase.wrapObservable(isSupplierCreditEnabledCustomer.get().execute(customerId))
    }.map {
        when (it) {
            is Result.Progress -> MoveToSupplierContract.PartialState.NoChange
            is Result.Success -> {
                MoveToSupplierContract.PartialState.SetSupplierCreditEnabledStatus(it.value)
            }
            is Result.Failure -> {
                MoveToSupplierContract.PartialState.NoChange
            }
        }
    }

    override fun reduce(
        currentState: MoveToSupplierContract.State,
        partialState: MoveToSupplierContract.PartialState,
    ): MoveToSupplierContract.State {
        return when (partialState) {
            MoveToSupplierContract.PartialState.NoChange -> currentState
            MoveToSupplierContract.PartialState.ShowLoading -> currentState.copy(
                isLoading = true,
                networkError = false,
                error = false
            )
            MoveToSupplierContract.PartialState.ErrorState -> currentState.copy(error = true)
            is MoveToSupplierContract.PartialState.SetCustomer -> currentState.copy(
                customer = partialState.customer,
                networkError = false,
                error = false
            )
            is MoveToSupplierContract.PartialState.SetNetworkError -> currentState.copy(networkError = partialState.networkError)
            is MoveToSupplierContract.PartialState.SetMigrationStates -> currentState.copy(
                canShowLoaderScreen = partialState.canShowLoaderScreen,
                canShowSuccessfulScreen = partialState.canShowSuccessfulScreen,
                canShowFailureScreen = partialState.canShowFailureScreen,
                migratedAccountId = partialState.migragtedAccountId,
                errorMessage = partialState.errorMessage
            )
            is MoveToSupplierContract.PartialState.SetLoaderProgess -> currentState.copy(progressValue = partialState.progressValue)
            is MoveToSupplierContract.PartialState.AccountMigrationPermittedError -> currentState.copy(
                errorMessage = partialState.errorMessage,
                canShowAccountMigrationError = partialState.canShowAccountMigrationError
            )
            is MoveToSupplierContract.PartialState.SetSupplierCreditEnabledStatus -> currentState.copy(commonLedger = partialState.commonLedger)
        }
    }
}
