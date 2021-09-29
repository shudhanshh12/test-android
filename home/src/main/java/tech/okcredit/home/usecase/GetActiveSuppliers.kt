package tech.okcredit.home.usecase

import `in`.okcredit.merchant.contract.GetActiveBusinessId
import `in`.okcredit.merchant.suppliercredit.Supplier
import `in`.okcredit.merchant.suppliercredit.SupplierCreditRepository
import dagger.Lazy
import io.reactivex.Observable
import merchant.okcredit.accounting.contract.HomeSortType
import tech.okcredit.account_chat_contract.IGetChatUnreadMessageCount
import tech.okcredit.account_chat_contract.STRING_CONSTANTS
import tech.okcredit.android.base.crashlytics.RecordException
import tech.okcredit.home.ui.supplier_tab.SupplierComparator
import tech.okcredit.sdk.usecase.GetUnreadBillCounts
import java.util.*
import javax.inject.Inject
import kotlin.collections.HashMap

/**
 * Here app db is single source of truth
 * every data is populated from app db (which is timely synced with server)
 *
 *  UI - Supplier list is populated
 *
 * when merchant do Sorting or Searching -> we once again fetch data from db and populate list and then we do Sorting
 * or Filtering on the newly fetched list
 * reason : there might be some update in the db (we periodically sync db with server), so we read once again from it
 *
 */

class GetActiveSuppliers @Inject constructor(
    private val supplierCreditRepository: Lazy<SupplierCreditRepository>,
    private val getChatUnreadMessageCount: Lazy<IGetChatUnreadMessageCount>,
    private val getUnreadBillCounts: Lazy<GetUnreadBillCounts>,
    private val getActiveBusinessId: Lazy<GetActiveBusinessId>,
) {

    // Stop building on top of this. This is highly inefficient. Refactor this usecase in next product task
    fun execute(searchQuery: String = ""): Observable<Response> {

        return getActiveBusinessId.get().execute().flatMapObservable { businessId ->
            Observable.combineLatest(
                supplierCreditRepository.get().listActiveSuppliers(businessId),
                getChatUnreadMessageCount.get().getUnreadCountForRelation(STRING_CONSTANTS.BUYER)
                    .onErrorReturn {
                        RecordException.recordException(it)
                        HashMap()
                    }.startWith(HashMap()),
                supplierCreditRepository.get().getSortType(businessId),
                getUnreadBillCounts.get().execute().onErrorReturn { HashMap() },
                { supplierList: List<Supplier>, result: HashMap<String, Long>, sortType: HomeSortType, unreadBillCounts: Map<String, Int> ->

                    val filteredList: MutableList<Supplier>

                    val originalSupplierList = supplierList as MutableList<Supplier>

                    /*********** Sorting  *************/
                    sortSuppliers(sortType, originalSupplierList)

                    /*********** Filtering Suppliers by Name Or Mobile number  *************/
                    filteredList = filterSuppliers(
                        searchQuery,
                        originalSupplierList
                    )

                    /*********** Getting tabCount count for tab header(in HomeFragment)  *************/
                    var tabCount = 0
                    val newCountList = mutableListOf<Supplier>()
                    filteredList.forEach {
                        val count = unreadBillCounts[it.id] ?: 0
                        val newSupplier = it.copy(
                            newActivityCount = it.newActivityCount + result.getOrElse(
                                it.id,
                                {
                                    0L
                                }
                            ) + count
                        )

                        newCountList.add(newSupplier)

                        tabCount += if (newSupplier.newActivityCount > 0) 1 else 0
                    }

                    Response(
                        suppliers = newCountList,
                        tabCount = tabCount
                    )
                }
            )
        }
    }

    private fun filterSuppliers(
        searchQuery: String?,
        originalSupplierList: MutableList<Supplier>,
    ): MutableList<Supplier> {
        val filteredSupplierList: MutableList<Supplier>
        val finalSupplierList: MutableList<Supplier>
        if (searchQuery != null && searchQuery != "") { // if searchQuery is available , then we start filtering
            filteredSupplierList = originalSupplierList.filter {

                (it.name.toLowerCase().contains(searchQuery)) ||
                    it.mobile != null && it.mobile!!.contains(searchQuery.trim())
            }.toMutableList()

            finalSupplierList = filteredSupplierList
        } else {
            finalSupplierList = originalSupplierList
        }
        return finalSupplierList
    }

    private fun sortSuppliers(sortType: HomeSortType, suppliersList: MutableList<Supplier>) {
        when (sortType) {
            HomeSortType.AMOUNT -> Collections.sort(
                suppliersList,
                SupplierComparator.AbsoluteBalance()
            )
            HomeSortType.ACTIVITY -> Collections.sort(
                suppliersList,
                SupplierComparator.RecentActivity()
            )
            HomeSortType.NAME, HomeSortType.NONE -> Collections.sort(suppliersList, SupplierComparator.Name())
        }
    }

    data class Response(
        val suppliers: List<Supplier>,
        val tabCount: Int = 0,
    )
}
