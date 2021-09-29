package tech.okcredit.home.usecase

import `in`.okcredit.backend._offline.database.CustomerRepo
import `in`.okcredit.backend.contract.Customer
import `in`.okcredit.merchant.contract.GetActiveBusinessId
import `in`.okcredit.shared.usecase.Result
import `in`.okcredit.shared.usecase.UseCase
import dagger.Lazy
import dagger.Reusable
import io.reactivex.Observable
import io.reactivex.functions.Function3
import org.joda.time.DateTime
import tech.okcredit.account_chat_contract.IGetChatUnreadMessageCount
import tech.okcredit.account_chat_contract.STRING_CONSTANTS
import tech.okcredit.android.base.crashlytics.RecordException
import tech.okcredit.home.ui.homesearch.HomeConstants
import tech.okcredit.home.ui.homesearch.Sort
import tech.okcredit.home.utils.LifeCycle
import tech.okcredit.sdk.usecase.GetUnreadBillCounts
import timber.log.Timber
import javax.inject.Inject

/**
 * Here app db is single source of truth
 * every data is populated from app db (which is timely synced with server)
 *
 *  UI - customer list is populated
 *
 * when merchant do Sorting or Searching -> we once again fetch data from db and
 * populate list and then we do Sorting or Filtering on the newly fetched list
 * reason : there might be some update in the db (we periodically sync db with server), so we read once again from it
 *
 */

@Reusable
class GetActiveCustomers @Inject constructor(
    private val customerRepo: Lazy<CustomerRepo>,
    private val getChatUnreadMessageCount: Lazy<IGetChatUnreadMessageCount>,
    private val getUnreadBillCounts: Lazy<GetUnreadBillCounts>,
    private val getActiveBusinessId: Lazy<GetActiveBusinessId>,
) : UseCase<GetActiveCustomers.Request, GetActiveCustomers.Response> {

    // Stop building on top of this. This is highly inefficient. Refactor this usecase in next product task
    override fun execute(req: Request): Observable<Result<Response>> {

        return UseCase.wrapObservable(
            getActiveBusinessId.get().execute().flatMapObservable { businessId ->
                Observable.combineLatest(
                    customerRepo.get().listActiveCustomers(businessId),
                    getChatUnreadMessageCount.get().getUnreadCountForRelation(STRING_CONSTANTS.SELLER).onErrorReturn {
                        RecordException.recordException(it)
                        (HashMap())
                    }.startWith(HashMap()),
                    getUnreadBillCounts.get().execute().onErrorReturn { HashMap() }
                        .startWith(HashMap()),
                    handleResult(req)
                )
            }
        )
    }

    private fun handleResult(request: Request): Function3<List<Customer>, HashMap<String, Long>, Map<String, Int>, Response> {
        return Function3 { originalCustomerList, resultHashMap, unreadBillCounts ->

            Timber.d("$TAG GetActiveCustomer.Emiited")

            val finalCustomerList: List<Customer>
            var liveSalesCustomer: Customer? = null

            var sortedCustomerList: ArrayList<Customer>
            request.newSortAndSearchQuery.let {
                val sortFilter = request.newSortAndSearchQuery.first
                val sortBy = request.newSortAndSearchQuery.second
                sortedCustomerList = ArrayList(sortCustomer(sortFilter, sortBy, originalCustomerList, resultHashMap))
            }

            /*********** Separating Live Sales customer from customer list + Filtering customers by Name Or Mobile number  *************/
            val searchQuery = request.newSortAndSearchQuery.third.toLowerCase()
            finalCustomerList = sortedCustomerList.filter { customer ->
                when {
                    customer.isLiveSales -> {
                        liveSalesCustomer = customer
                        false
                    }
                    searchQuery.isEmpty() -> {
                        true
                    }
                    else -> {
                        (customer.description.toLowerCase().contains(searchQuery)) ||
                            customer.mobile != null && customer.mobile!!.contains(searchQuery.trim())
                    }
                }
            }.distinctBy { it.id }

            val lifeCycle = when (finalCustomerList.size) {
                0 -> {
                    if (Sort.sortApplied && Sort.sortfilter.isNotEmpty()) {
                        LifeCycle.SORT
                    } else {
                        LifeCycle.TRIAL_ADD_CUSTOMER
                    }
                }
                else -> LifeCycle.NORMAL_FLOW
            }

            /*********** Getting new activity count for tab header  *************/

            val updatedList = mutableListOf<Customer>()
            var tabCount = 0
            for (customer in finalCustomerList) {
                val count = unreadBillCounts[customer.id] ?: 0
                val combineNewActivity = customer.newActivityCount + resultHashMap.getOrElse(
                    customer.id,
                    {
                        0L
                    }
                ) + count
                updatedList.add(customer.copy(newActivityCount = combineNewActivity))
                tabCount += if (combineNewActivity > 0) 1 else 0
            }

            Response(
                customers = updatedList,
                searchQuery = searchQuery,
                tabCount = tabCount,
                liveSalesCustomer = liveSalesCustomer,
                lifeCycle = lifeCycle
            )
        }
    }

    private fun sortCustomer(
        sortFilter: ArrayList<String>,
        sortBy: String,
        originalCustomerList: List<Customer>,
        hashMap: HashMap<String, Long>,
    ): List<Customer> {

        val copyOriginalList = ArrayList(originalCustomerList)
        if (sortFilter.isEmpty() && sortBy == HomeConstants.SORT_BY_LATEST) {
            return getDefaultList(copyOriginalList, hashMap)
        }

        val sortFilterList: java.util.ArrayList<Customer>

        if (sortFilter.isNotEmpty()) {
            sortFilterList = ArrayList()
            for (i in sortFilter) {
                when (i) {
                    HomeConstants.SORT_FILTER_DUE_TODAY -> {
                        sortFilterList.addAll(
                            copyOriginalList.filter {
                                it.dueActive && it.dueInfo_activeDate?.withTimeAtStartOfDay()
                                    ?.isEqual(DateTime.now().withTimeAtStartOfDay()) ?: false
                            }.toCollection(ArrayList())
                        )
                    }
                    HomeConstants.SORT_FILTER_DUE_CROSSED -> {
                        sortFilterList.addAll(
                            copyOriginalList.filter {
                                it.dueActive && it.dueInfo_activeDate?.withTimeAtStartOfDay()
                                    ?.isBefore(DateTime.now().withTimeAtStartOfDay()) ?: false
                            }.toCollection(ArrayList())
                        )
                    }
                    HomeConstants.SORT_FILTER_UPCOMING_DUE -> {
                        sortFilterList.addAll(
                            copyOriginalList.filter {
                                it.dueActive && it.dueInfo_activeDate?.withTimeAtStartOfDay()
                                    ?.isAfter(DateTime.now().withTimeAtStartOfDay()) ?: false
                            }.toCollection(ArrayList())
                        )
                    }
                }
            }
        } else {
            sortFilterList = ArrayList(copyOriginalList)
        }
        val sortByList = ArrayList<Customer>(sortFilterList)

        when (sortBy) {
            HomeConstants.SORT_BY_NAME -> {
                sortByList.sortBy {
                    it.description.toLowerCase()
                }
            }
            HomeConstants.SORT_BY_AMOUNT -> {
                sortByList.sortBy {
                    it.balanceV2
                }
            }
            HomeConstants.SORT_BY_LATEST -> {
                sortByList.sortBy {
                    it.lastActivity
                }
                sortByList.reverse()
            }
        }
        return sortByList
    }

    private fun getDefaultList(
        originalCustomerList: List<Customer>,
        hashMap: HashMap<String, Long>,
    ): MutableList<Customer> {
        val finalList = mutableListOf<Customer>()
        val recentList = originalCustomerList.filter {
            (it.newActivityCount + hashMap.getOrElse(it.id, { 0L })) > 0
        }

        finalList.addAll(
            recentList.sortedBy {
                it.lastActivity
            }.reversed()
        )
        val dueToday = originalCustomerList
            .minus(recentList)
            .filter {
                it.dueActive &&
                    it.dueInfo_activeDate != null &&
                    it.dueInfo_activeDate!!.withTimeAtStartOfDay().isEqual(DateTime.now().withTimeAtStartOfDay())
            }

        finalList.addAll(
            dueToday.sortedBy { it.lastActivity }
                .reversed()
        )
        val dueCrossed = originalCustomerList
            .minus(recentList)
            .filter {
                it.dueActive &&
                    it.dueInfo_activeDate != null &&
                    it.dueInfo_activeDate!!.withTimeAtStartOfDay().isEqual(
                        DateTime.now().withTimeAtStartOfDay().minusDays(
                            1
                        )
                    )
            }

        finalList.addAll(
            dueCrossed.sortedBy { it.lastActivity }
                .reversed()
        )
        val rest = originalCustomerList.minus(finalList)
        finalList.addAll(
            rest.sortedBy {
                it.lastActivity
            }.reversed()
        )
        return finalList
    }

    data class Response(
        val customers: List<Customer>,
        val searchQuery: String,
        val tabCount: Int,
        val liveSalesCustomer: Customer?,
        val lifeCycle: Int,
    )

    data class Request(
        val sortAndSearchQuery: Pair<Int, String>?,
        val newSortAndSearchQuery: Triple<ArrayList<String>, String, String>,
    )

    companion object {
        const val TAG = "GetActiveCustomer"
    }
}
