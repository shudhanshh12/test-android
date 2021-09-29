package tech.okcredit.home.ui.reminder.bulk

import `in`.okcredit.backend.contract.Customer
import `in`.okcredit.collection.contract.CollectionRepository
import `in`.okcredit.merchant.contract.GetActiveBusinessId
import `in`.okcredit.shared.base.BaseViewModel
import `in`.okcredit.shared.usecase.Result
import `in`.okcredit.shared.usecase.UseCase
import dagger.Lazy
import io.reactivex.Observable
import tech.okcredit.home.R
import tech.okcredit.home.ui.reminder.bulk.BulkReminderContract.*
import tech.okcredit.home.ui.reminder.usecase.GetBulkReminderCustomers
import tech.okcredit.home.ui.reminder.usecase.SendBulkReminder
import javax.inject.Inject

class BulkReminderViewModel @Inject constructor(
    state: State,
    private val getActiveBusinessId: Lazy<GetActiveBusinessId>,
    private val getBulkReminderCustomers: Lazy<GetBulkReminderCustomers>,
    private val sendBulkReminder: Lazy<SendBulkReminder>,
    private val collectionRepository: Lazy<CollectionRepository>,
) : BaseViewModel<State, PartialState, ViewEvent>(state) {

    override fun handle(): Observable<PartialState> {
        return Observable.mergeArray(
            loadActiveMerchant(),
            loadDefaulters(),
            observeSelectCustomer(),
            loadSubmitClicked(),
            collectionAdoptedObservable(),
        )
    }

    private fun loadDefaulters() = intent<Intent.Load>().switchMap {
        UseCase.wrapObservable(getBulkReminderCustomers.get().execute())
    }.map {
        when (it) {
            is Result.Progress -> PartialState.Loading
            is Result.Success -> PartialState.CustomerList(it.value)
            is Result.Failure -> {
                PartialState.NoChange
            }
        }
    }

    private fun observeSelectCustomer() = intent<Intent.SelectCustomer>().map {
        PartialState.SelectCustomer(it.customerId)
    }

    private fun loadActiveMerchant() = intent<Intent.Load>().switchMap {
        wrap(getActiveBusinessId.get().execute())
    }.map {
        if (it is Result.Success) {
            PartialState.SetBusiness(it.value)
        } else {
            PartialState.NoChange
        }
    }

    private fun collectionAdoptedObservable() = intent<Intent.Load>().switchMap {
        UseCase.wrapObservable(collectionRepository.get().isCollectionActivated())
    }.map {
        when (it) {
            is Result.Success -> {
                PartialState.SetCollectionActivated(it.value)
            }
            else -> PartialState.NoChange
        }
    }

    private fun loadSubmitClicked() = intent<Intent.SubmitClicked>().switchMap {
        UseCase.wrapCompletable(sendBulkReminder.get().execute(it.selectedCustomersSet))
    }.map {
        when (it) {
            is Result.Progress -> PartialState.Loading
            is Result.Success -> {
                emitViewEvent(ViewEvent.Success)
                PartialState.NoChange
            }
            is Result.Failure -> {
                when {
                    isInternetIssue(it.error) -> {
                        emitViewEvent(ViewEvent.ShowError(R.string.interent_error))
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

    override fun reduce(currentState: State, partialState: PartialState): State {
        return when (partialState) {
            PartialState.NoChange -> currentState
            PartialState.Loading -> currentState.copy(loading = true)
            is PartialState.CustomerList -> currentState.copy(
                loading = false,
                bulkReminderList = transformCustomerList(partialState.list)
            )
            is PartialState.SelectCustomer -> currentState.copy(
                bulkReminderList = currentState.bulkReminderList.map { item: BulkReminderItem ->
                    return@map if (item.customerId == partialState.customerId) {
                        item.copy(checked = !item.checked)
                    } else {
                        item
                    }
                }
            )
            is PartialState.SetBusiness -> currentState.copy(
                merchantId = partialState.id
            )
            is PartialState.SetCollectionActivated -> currentState.copy(
                collectionAdopted = partialState.adopted
            )
        }
    }

    private fun transformCustomerList(list: List<Customer>): List<BulkReminderItem> {
        val bulkList = mutableListOf<BulkReminderItem>()
        list.forEach {
            val bulkReminderItem = BulkReminderItem(
                customerId = it.id,
                name = it.description,
                profilePic = it.profileImage,
                amountDue = it.balanceV2,
                checked = true
            )
            bulkList.add(bulkReminderItem)
        }

        return bulkList
    }
}
