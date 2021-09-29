package `in`.okcredit.merchant.customer_ui.ui.staff_link.add

import `in`.okcredit.backend.contract.Customer
import `in`.okcredit.backend.utils.CurrencyUtil
import `in`.okcredit.merchant.customer_ui.R
import `in`.okcredit.merchant.customer_ui.ui.staff_link.add.StaffLinkAddCustomerContract.*
import `in`.okcredit.shared.base.Reducer
import android.content.Context
import android.text.Spanned
import androidx.core.text.buildSpannedString
import androidx.core.text.color
import dagger.Lazy
import org.joda.time.Days
import tech.okcredit.android.base.extensions.getColorCompat
import tech.okcredit.android.base.utils.DateTimeUtils
import tech.okcredit.base.dagger.di.qualifier.ViewModelParam
import javax.inject.Inject
import kotlin.math.abs

class StaffLinkAddCustomerReducer @Inject constructor(
    private val context: Lazy<Context>,
    @ViewModelParam("selected_customers") private val preSelectedCustomerIds: Set<String>?,
) : Reducer<State, PartialState> {

    private val currentDate by lazy { DateTimeUtils.currentDateTime() }

    override fun reduce(
        current: State,
        partial: PartialState,
    ): State {
        return when (partial) {
            PartialState.NoChange -> current
            is PartialState.SetCustomerList -> handleSearchCustomer(
                current,
                partial
            )
            is PartialState.SetLoading -> current.copy(loading = partial.loading)
            PartialState.SearchClicked -> current.copy(
                showTopSummaryCard = false,
                showEditableSearch = true
            )
            PartialState.DismissSearch -> current.copy(
                showTopSummaryCard = true,
                showSelectAllHeader = true,
                showEditableSearch = false,
            )
            is PartialState.SelectCustomers -> current.copy(
                selectedCustomerIds = partial.selectedCustomers,
                filteredCustomerList = current.filteredCustomerList.map {
                    it.copy(selected = partial.selectedCustomers.contains(it.id))
                },
                showBottomActions = partial.selectedCustomers.isNotEmpty(),
            )
            is PartialState.SetPreSelectedCustomer -> handlePreselectedCustomers(
                current,
                partial
            )
        }
    }

    private fun handlePreselectedCustomers(
        current: State,
        partial: PartialState.SetPreSelectedCustomer,
    ): State {
        val list = mutableListOf<CustomerItem>()
        var totalDue = 0L
        val selectedCustomerSet = mutableSetOf<String>()
        selectedCustomerSet.addAll(current.selectedCustomerIds)
        partial.list.forEach {
            totalDue += abs(it.balanceV2)
            // if coming for the first time then add all customers in selected list
            if (current.originalCustomerList.isEmpty()) {
                selectedCustomerSet.add(it.id)
            }
            list.add(
                CustomerItem(
                    id = it.id,
                    profilePic = it.profileImage,
                    name = it.description,
                    subTitle = createSubtitleForCustomer(it),
                    selected = selectedCustomerSet.contains(it.id),
                )
            )
        }
        return current.copy(
            filteredCustomerList = list,
            totalDue = totalDue,
            showSelectAllHeader = true,
            selectedCustomerIds = selectedCustomerSet.toSet(),
            showBottomActions = selectedCustomerSet.size != 0,
            showEmptySearchResult = false,
            originalCustomerList = partial.list,
            showNoCustomerMessage = partial.list.isEmpty(),
            loading = false
        )
    }

    private fun handleSearchCustomer(
        current: State,
        partial: PartialState.SetCustomerList,
    ): State {
        val list = mutableListOf<CustomerItem>()
        var totalDue = 0L
        val selectedCustomerSet = mutableSetOf<String>()
        selectedCustomerSet.addAll(current.selectedCustomerIds)
        partial.customerSearchWrapper.filteredCustomerList.forEachIndexed { index, customer ->
            // if coming for the first time and we do not have any preselected customers then add all customers in
            // selected list
            if (current.originalCustomerList.isEmpty() && preSelectedCustomerIds.isNullOrEmpty()) {
                selectedCustomerSet.add(customer.id)
            }
            list.add(
                CustomerItem(
                    id = customer.id,
                    profilePic = customer.profileImage,
                    name = customer.description,
                    subTitle = createSubtitleForCustomer(customer),
                    selected = selectedCustomerSet.contains(customer.id),
                )
            )
        }
        partial.customerSearchWrapper.originalCustomerList.forEach {
            totalDue += abs(it.balanceV2)
        }

        return current.copy(
            filteredCustomerList = list,
            totalDue = totalDue,
            searchQuery = partial.customerSearchWrapper.searchQuery,
            showSelectAllHeader = partial.customerSearchWrapper.searchQuery.isBlank(),
            selectedCustomerIds = selectedCustomerSet.toSet(),
            showBottomActions = selectedCustomerSet.size != 0,
            showEmptySearchResult = partial.customerSearchWrapper.searchQuery.isNotBlank() && list.isEmpty(),
            originalCustomerList = partial.customerSearchWrapper.originalCustomerList,
            showNoCustomerMessage = partial.customerSearchWrapper.originalCustomerList.isEmpty(),
            loading = false
        )
    }

    private fun createSubtitleForCustomer(customer: Customer): Spanned {
        return buildSpannedString {
            color(context.get().getColorCompat(R.color.tx_credit)) {
                append(String.format("₹%s", CurrencyUtil.formatV2(customer.balanceV2)))
            }
            if (customer.dueActive && customer.dueInfo_activeDate != null) {
                val dayCount = Days.daysBetween(currentDate, customer.dueInfo_activeDate).days
                val duePrefix = if (dayCount > 0) {
                    context.get().getString(R.string.after_due_on)
                } else {
                    context.get().getString(R.string.since_due_on)
                }
                append(" ")
                append(
                    context.get().getString(
                        R.string.t_003_staff_collection_amount_due_date,
                        duePrefix,
                        abs(dayCount).toString()
                    )
                )
            } else {
                append(" ")
                append(
                    context.get().getString(R.string.due)
                )
            }
        }
    }
}
