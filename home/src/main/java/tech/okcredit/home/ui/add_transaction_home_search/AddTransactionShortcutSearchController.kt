package tech.okcredit.home.ui.add_transaction_home_search

import `in`.okcredit.analytics.Tracker
import `in`.okcredit.shared.performance.PerformanceTracker
import androidx.fragment.app.FragmentActivity
import com.airbnb.epoxy.AsyncEpoxyController
import dagger.Lazy
import tech.okcredit.home.R
import tech.okcredit.home.ui.home.views.HomeCustomerView
import tech.okcredit.home.ui.home.views.HomeSupplierView
import tech.okcredit.home.ui.home.views.homeCustomerView
import tech.okcredit.home.ui.home.views.homeSupplierView
import tech.okcredit.home.ui.homesearch.HomeSearchContract
import tech.okcredit.home.ui.homesearch.views.addContactView
import tech.okcredit.home.ui.homesearch.views.global.headerView
import tech.okcredit.home.ui.homesearch.views.importCustomerContactView
import tech.okcredit.home.ui.homesearch.views.noUserFoundView
import tech.okcredit.home.ui.homesearch.views.shimmerListLoadingView

class AddTransactionShortcutSearchController constructor(
    private val context: AddTransactionShortcutSearchFragment,
    private val activity: FragmentActivity?,
    private val performanceTracker: Lazy<PerformanceTracker>,
    private val tracker: Tracker,
) : AsyncEpoxyController() {

    private lateinit var state: AddTransactionShortcutSearchContract.State

    fun setState(
        state: AddTransactionShortcutSearchContract.State,
    ) {
        this.state = state
        requestModelBuild()
    }

    override fun buildModels() {
        if (state.showSuggestedCustomers && state.suggestedCustomers.isNotEmpty() && state.isSuggestedCustomersLoading.not()) {
            renderSuggestedCustomers(true)
        }

        if (state.customers.isEmpty() && state.suppliers.isEmpty() && state.contacts.isEmpty() && state.isLoading.not()) {
            noUserFoundView {
                id("no_customer_view_1")
                noUserFoundMessage(
                    context.resources.getString(R.string.contact_not_found, state.searchQuery)
                )
                showLoader(state.addRelationLoading)
                listener(context)
            }
        } else if (state.isLoading) {
            shimmerListLoadingView {
                id("loading_customers")
            }
        }

        renderCustomers(true)
        renderSuppliers(true)

        if (state.contacts.isNotEmpty() || !state.isContactsPermissionGranted || state.isContactsLoading) {
            headerView {
                id("contact_header")
                title(activity?.resources?.getString(R.string.contacts) ?: "")
            }

            if (state.contacts.isNotEmpty()) {
                state.contacts.forEach {
                    addContactView {
                        id(it.phonebookId)
                        contacts(it)
                        listener(context)
                    }
                }
            } else if (state.isContactsLoading) {
                shimmerListLoadingView {
                    id("loading_contact")
                }
            } else if (!state.isContactsPermissionGranted) {
                importCustomerContactView {
                    id("import_customer_contact_view")
                    source(HomeSearchContract.SOURCE.HOME_CUSTOMER_TAB)
                    listener(context)
                }
            }
        }
    }

    private fun renderSuggestedCustomers(showHeader: Boolean) {
        if (state.suggestedCustomers.isNullOrEmpty().not()) {
            if (showHeader) {
                headerView {
                    id("suggested_customer_header")
                    title(activity?.resources?.getString(R.string.suggested) ?: "")
                }
            }

            state.suggestedCustomers.mapIndexed { index, customer ->
                homeCustomerView {
                    id("search_suggested_customer_${index}${customer.id}")
                    customerViewData(
                        HomeCustomerView.CustomerAndEducationObject(
                            customer = customer,
                            index = index,
                            totalCustomers = state.customers.size,
                            showEducation = false,
                            isUnsyncTransaction = state.unSyncCustomerIds.contains(customer.id),
                            isSupplierRegistered = state.supplierCreditEnabledCustomerIds.contains(customer.id),
                            isProfilePicClickable = state.isProfilePicClickable,
                            // Todo(Mohitesh: HomeCustomerView is also use HomeSearchController)
                            isSingleListEnabled = false,
                            singleListAddTxnRestrictedCustomers = false,
                        )
                    )

                    tracker(tracker)
                    performanceTracker(performanceTracker.get())
                    listener(context)
                }
            }
        }
    }

    private fun renderCustomers(showHeader: Boolean) {
        if (state.customers.isNullOrEmpty().not()) {
            if (showHeader) {
                headerView {
                    id("customer_header")
                    title(activity?.resources?.getString(R.string.customer) ?: "")
                }
            }

            state.customers.mapIndexed { index, customer ->
                if (state.showSuggestedCustomers) {
                    if (state.suggestedCustomers.map { it.id }.contains(customer.id))
                        return@mapIndexed
                }
                homeCustomerView {
                    id("search_customer_${index}${customer.id}")
                    customerViewData(
                        HomeCustomerView.CustomerAndEducationObject(
                            customer = customer,
                            index = index,
                            totalCustomers = state.customers.size,
                            showEducation = false,
                            isUnsyncTransaction = state.unSyncCustomerIds.contains(customer.id),
                            isSupplierRegistered = state.supplierCreditEnabledCustomerIds.contains(customer.id),
                            isProfilePicClickable = state.isProfilePicClickable,
                            // Todo(Mohitesh: HomeCustomerView is also use HomeSearchController)
                            isSingleListEnabled = false,
                            singleListAddTxnRestrictedCustomers = false,
                        )
                    )
                    tracker(tracker)
                    performanceTracker(performanceTracker.get())
                    listener(context)
                }
            }
        }
    }

    private fun renderSuppliers(showHeader: Boolean) {
        if (state.suppliers.isNullOrEmpty().not()) {
            if (showHeader) {
                headerView {
                    id("supplier_header")
                    title(activity?.resources?.getString(R.string.suppliers) ?: "")
                }
            }

            state.suppliers.forEach {
                homeSupplierView {
                    id("supplier${it.id}")
                    supplierDetails(
                        HomeSupplierView.HomeSupplierData(
                            it.id,
                            it,
                            if (it.lastActivityTime != null) if (state.unSyncSupplierIds.contains(it.id)) HomeSupplierView.SYNC_PENDING else HomeSupplierView.SYNC_COMPLETED else HomeSupplierView.SYNC_NO_TXN,
                        )
                    )
                    tracker(tracker)
                    performanceTracker(performanceTracker.get())
                    listener(context)
                }
            }
        }
    }
}
