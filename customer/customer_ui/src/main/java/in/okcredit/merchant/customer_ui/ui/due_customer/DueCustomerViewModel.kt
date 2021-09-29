package `in`.okcredit.merchant.customer_ui.ui.due_customer

import `in`.okcredit.backend.contract.Customer
import `in`.okcredit.collection.contract.CollectionRepository
import `in`.okcredit.merchant.contract.GetActiveBusinessId
import `in`.okcredit.merchant.customer_ui.usecase.GetAllDueCustomers
import `in`.okcredit.shared.base.BasePresenter
import `in`.okcredit.shared.base.UiState
import `in`.okcredit.shared.usecase.Result
import `in`.okcredit.shared.usecase.UseCase
import dagger.Lazy
import io.reactivex.Observable
import io.reactivex.Observable.mergeArray
import io.reactivex.subjects.BehaviorSubject
import io.reactivex.subjects.PublishSubject
import tech.okcredit.base.dagger.di.qualifier.ViewModelParam
import javax.inject.Inject

class DueCustomerViewModel @Inject constructor(
    initialState: DueCustomerContract.State,
    @ViewModelParam(DueCustomerContract.ARG_SOURCE) val sourceScreen: String?,
    @ViewModelParam(DueCustomerContract.ARG_REWARDS_AMOUNT) val rewardsAmount: Long?,
    @ViewModelParam(DueCustomerContract.ARG_REDIRECT_TO_REWARDS_PAGE) val redirectToRewardsPage: Boolean?,
    private val getAllDueCustomers: GetAllDueCustomers,
    private val collectionRepository: CollectionRepository,
    private val getActiveBusinessId: Lazy<GetActiveBusinessId>,
    private val navigator: DueCustomerContract.Navigator
) : BasePresenter<DueCustomerContract.State, DueCustomerContract.PartialState>(initialState) {

    private lateinit var dueCustomers: List<Customer>
    private var isInitialLoadCompleted = false
    private val searchQueryChangeBehaviorSubject: BehaviorSubject<String> = BehaviorSubject.createDefault("")
    private val selectAllPublicSubject: PublishSubject<Boolean> = PublishSubject.create()

    private var merchantId: String = ""

    override fun handle(): Observable<UiState.Partial<DueCustomerContract.State>> {
        return mergeArray(

            intent<DueCustomerContract.Intent.Load>()
                .switchMap { searchQueryChangeBehaviorSubject }
                .switchMap { getAllDueCustomers.execute(GetAllDueCustomers.Request(it)) }
                .map {
                    when (it) {
                        is Result.Progress -> DueCustomerContract.PartialState.NoChange
                        is Result.Success -> {
                            dueCustomers = it.value

                            if (isInitialLoadCompleted.not()) {
                                selectAllPublicSubject.onNext(true)
                                isInitialLoadCompleted = true
                            }

                            DueCustomerContract.PartialState.SetDueCustomerList(it.value)
                        }

                        is Result.Failure -> DueCustomerContract.PartialState.IsError
                    }
                },

            intent<DueCustomerContract.Intent.Load>()
                .switchMap { UseCase.wrapSingle(getActiveBusinessId.get().execute()) }
                .map {
                    when (it) {
                        is Result.Progress -> DueCustomerContract.PartialState.NoChange
                        is Result.Success -> {
                            merchantId = it.value
                            DueCustomerContract.PartialState.NoChange
                        }
                        is Result.Failure -> {
                            DueCustomerContract.PartialState.NoChange
                        }
                    }
                },

            selectAllPublicSubject
                .map {
                    val selectedCustomerIds = mutableListOf<String>()
                    if (it) {
                        for (customer in dueCustomers) {
                            selectedCustomerIds.add(customer.id)
                        }
                    }

                    DueCustomerContract.PartialState.SelectCustomersIds(selectedCustomerIds)
                },

            intent<DueCustomerContract.Intent.SelectAll>()
                .map {
                    selectAllPublicSubject.onNext(it.selectAll)
                    DueCustomerContract.PartialState.NoChange
                },

            intent<DueCustomerContract.Intent.SendReminders>()
                .switchMap {
                    UseCase.wrapCompletable(
                        collectionRepository.createBatchCollection(
                            it.selectedCustomerIds,
                            merchantId
                        )
                    )
                }
                .map {
                    when (it) {
                        is Result.Progress -> DueCustomerContract.PartialState.NoChange
                        is Result.Success -> {
                            navigator.goToMerchantDestinationpage(sourceScreen, rewardsAmount, redirectToRewardsPage)
                            DueCustomerContract.PartialState.NoChange
                        }

                        is Result.Failure -> DueCustomerContract.PartialState.NoChange
                    }
                },

            intent<DueCustomerContract.Intent.SelectItem>()
                .map {
                    var selectedCustomerIds: MutableList<String> = mutableListOf()
                    selectedCustomerIds.addAll(it.selectedCustomerIds)

                    if (selectedCustomerIds.contains(it.customerId)) {
                        selectedCustomerIds.remove(it.customerId)
                    } else {
                        selectedCustomerIds.add(it.customerId)
                    }

                    DueCustomerContract.PartialState.SelectCustomersIds(selectedCustomerIds)
                },

            intent<DueCustomerContract.Intent.NotNow>()
                .map {
                    navigator.goToMerchantDestinationpage(sourceScreen, rewardsAmount, redirectToRewardsPage)
                    DueCustomerContract.PartialState.NoChange
                },

            intent<DueCustomerContract.Intent.SearchQuery>()
                .map {
                    searchQueryChangeBehaviorSubject.onNext(it.searchQuery)
                    DueCustomerContract.PartialState.SearchQuery(it.searchQuery)
                }
        )
    }

    override fun reduce(
        currentState: DueCustomerContract.State,
        partialState: DueCustomerContract.PartialState
    ): DueCustomerContract.State {
        return when (partialState) {
            is DueCustomerContract.PartialState.ShowLoading -> currentState.copy(isLoading = true)
            is DueCustomerContract.PartialState.NoChange -> currentState
            is DueCustomerContract.PartialState.SetDueCustomerList -> currentState.copy(dueCustomers = partialState.dueCustomers)
            is DueCustomerContract.PartialState.IsEmpty -> currentState
            is DueCustomerContract.PartialState.IsError -> currentState.copy(isLoading = false, isError = true)
            is DueCustomerContract.PartialState.IsNetworkError -> currentState.copy(
                isLoading = false,
                isNetworkError = true
            )
            is DueCustomerContract.PartialState.IsButtonEnabled -> currentState.copy(isButtonEnabled = true)
            is DueCustomerContract.PartialState.SelectCustomersIds -> currentState.copy(selectedCustomerIds = partialState.selectedCustomerIds)
            is DueCustomerContract.PartialState.SearchQuery -> currentState.copy(searchQuery = partialState.searchQuery)
        }
    }
}
