package `in`.okcredit.collection_ui.usecase

import `in`.okcredit.backend._offline.usecase.GetDefaulterCustomerList
import `in`.okcredit.backend._offline.usecase.GetUnSyncedCustomers
import `in`.okcredit.backend.contract.Customer
import `in`.okcredit.collection_ui.ui.defaulters.model.Defaulter
import `in`.okcredit.home.GetSupplierCreditEnabledCustomerIds
import `in`.okcredit.merchant.contract.GetActiveBusinessId
import `in`.okcredit.shared.usecase.Result
import `in`.okcredit.shared.usecase.UseCase
import dagger.Lazy
import io.reactivex.Observable
import javax.inject.Inject

class GetDefaulterList @Inject constructor(
    private val getDefaulterCustomerList: Lazy<GetDefaulterCustomerList>,
    private val getUnSyncedCustomerIds: Lazy<GetUnSyncedCustomers>,
    private val getSupplierCreditEnabledCustomerIds: Lazy<GetSupplierCreditEnabledCustomerIds>,
    private val getActiveBusinessId: Lazy<GetActiveBusinessId>,
) : UseCase<Unit, List<Defaulter>> {

    override fun execute(req: Unit): Observable<Result<List<Defaulter>>> {
        return UseCase.wrapObservable(
            getActiveBusinessId.get().execute().flatMapObservable { businessId ->
                val list = listOf(
                    getDefaulterCustomerList.get().execute(),
                    getUnSyncedCustomerIds.get().execute(),
                    getSupplierCreditEnabledCustomerIds.get().execute(businessId),
                )
                Observable.combineLatest(list) {
                    val customerList = it[0] as? List<Customer>
                    val unSyncedCustomerIds = it[1] as? List<String>
                    val supplierCreditEnabledCustomerIds = it[2] as? String

                    customerList?.map { customer ->
                        Defaulter(
                            customer = customer,
                            hasUnSyncTransactions = unSyncedCustomerIds?.contains(customer.id) == true,
                            isSupplierRegistered = supplierCreditEnabledCustomerIds?.contains(customer.id) == true,
                        )
                    } ?: listOf()
                }
            }
        )
    }
}
