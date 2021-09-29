package tech.okcredit.home.utils

import `in`.okcredit.backend.contract.Customer
import `in`.okcredit.merchant.suppliercredit.Supplier
import org.joda.time.DateTime
import tech.okcredit.home.ui.homesearch.HomeConstants

object SearchAndSortUtils {

    fun sortSuppliers(sortType: Int, suppliersList: MutableList<Supplier>) {
        when (sortType) {
            HomeConstants.SORT_TYPE_ABS_BALANCE -> {
                suppliersList.sortBy {
                    it.balance
                }
            }
            HomeConstants.SORT_TYPE_RECENT_ACTIVITY -> {
                suppliersList.sortedWith(nullsLast(compareBy { it.lastActivityTime })).reversed()
            }
            HomeConstants.SORT_TYPE_LAST_PAYMENT -> {
                suppliersList.sortedWith(
                    compareBy(
                        {
                            when {
                                it.balance > 0 -> return@compareBy 1
                                it.balance < 0 -> return@compareBy -1
                                else -> return@compareBy 0
                            }
                        },
                        {
                            return@compareBy it.lastActivityTime ?: it.createTime
                        }
                    )
                )
            }
            HomeConstants.SORT_TYPE_NAME -> {
                suppliersList.sortBy {
                    it.name.toLowerCase()
                }
            }
            else -> {
                suppliersList.sortBy {
                    it.name.toLowerCase()
                }
            }
        }
    }

    fun sortCustomer(
        sortFilter: List<String>,
        sortBy: String,
        originalCustomerList: List<Customer>
    ): List<Customer> {
        if (sortFilter.isEmpty() && sortBy == HomeConstants.SORT_BY_LATEST) {
            return getDefaultList(originalCustomerList)
        }

        val sortFilterList: List<Customer>

        if (sortFilter.isNotEmpty()) {
            sortFilterList = ArrayList<Customer>()
            for (i in sortFilter) {
                when (i) {
                    HomeConstants.SORT_FILTER_DUE_TODAY -> {
                        sortFilterList.addAll(
                            originalCustomerList.filter {
                                it.dueInfo_activeDate != null && it.dueInfo_activeDate!!.withTimeAtStartOfDay()
                                    .isEqual(DateTime.now().withTimeAtStartOfDay())
                            }.toCollection(ArrayList())
                        )
                    }
                    HomeConstants.SORT_FILTER_DUE_CROSSED -> {
                        sortFilterList.addAll(
                            originalCustomerList.filter {
                                it.dueInfo_activeDate != null && it.dueInfo_activeDate!!.withTimeAtStartOfDay()
                                    .isBefore(DateTime.now().withTimeAtStartOfDay())
                            }.toCollection(ArrayList())
                        )
                    }
                    HomeConstants.SORT_FILTER_UPCOMING_DUE -> {
                        sortFilterList.addAll(
                            originalCustomerList.filter {
                                it.dueInfo_activeDate != null && it.dueInfo_activeDate!!.withTimeAtStartOfDay()
                                    .isAfter(DateTime.now().withTimeAtStartOfDay())
                            }.toCollection(ArrayList())
                        )
                    }
                }
            }
        } else {
            sortFilterList = ArrayList(originalCustomerList)
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

    private fun getDefaultList(originalCustomerList: List<Customer>): MutableList<Customer> {
        val finalList = mutableListOf<Customer>()

        val recentList = originalCustomerList.filter {
            it.newActivityCount > 0
        }.sortedBy {
            it.lastActivity
        }.reversed()

        finalList.addAll(recentList)

        val duetoday = originalCustomerList
            .minus(recentList)
            .filter { it.dueActive }
            .filter { it.dueInfo_activeDate != null }
            .filter { it.dueInfo_activeDate!!.withTimeAtStartOfDay().isEqual(DateTime.now().withTimeAtStartOfDay()) }
            .sortedBy { it.lastActivity }
            .reversed()

        finalList.addAll(duetoday)

        val dueCrossed = originalCustomerList
            .minus(recentList)
            .filter { it.dueActive }
            .filter { it.dueInfo_activeDate != null }
            .filter {
                it.dueInfo_activeDate!!.withTimeAtStartOfDay().isEqual(DateTime.now().withTimeAtStartOfDay().minusDays(1))
            }
            .sortedBy { it.lastActivity }
            .reversed()

        finalList.addAll(dueCrossed)

        val rest = originalCustomerList
            .minus(finalList)
            .sortedBy {
                it.lastActivity
            }.reversed()

        finalList.addAll(rest)

        return finalList
    }
}

fun String.containNameOrMobile(
    name: String?,
    mobile: String?
): Boolean {
    return when {
        this.isEmpty() -> {
            true
        }
        else -> {
            (
                ((name?.toLowerCase()?.contains(this.toLowerCase())) ?: false) ||
                    (mobile?.contains(this.toLowerCase().trim()) ?: false)
                )
        }
    }
}
