package tech.okcredit.home.ui.homesearch

import `in`.okcredit.analytics.Tracker
import `in`.okcredit.shared.performance.PerformanceTracker
import com.airbnb.epoxy.EpoxyAsyncUtil
import com.airbnb.epoxy.TypedEpoxyController
import tech.okcredit.home.R
import tech.okcredit.home.ui.home.views.HomeSupplierView
import tech.okcredit.home.ui.home.views.homeSupplierView
import tech.okcredit.home.ui.homesearch.views.addContactView
import tech.okcredit.home.ui.homesearch.views.filterView
import tech.okcredit.home.ui.homesearch.views.global.headerView
import tech.okcredit.home.ui.homesearch.views.homeSearchCustomerView
import tech.okcredit.home.ui.homesearch.views.importCustomerContactView
import tech.okcredit.home.ui.homesearch.views.noUserFoundView
import tech.okcredit.home.ui.homesearch.views.shimmerListLoadingView

class HomeSearchControllerV2(
    private val fragment: HomeSearchFragment,
    private val tracker: Tracker,
    private val performanceTracker: PerformanceTracker,
) : TypedEpoxyController<List<HomeSearchItem>>(
    EpoxyAsyncUtil.getAsyncBackgroundHandler(),
    EpoxyAsyncUtil.getAsyncBackgroundHandler()
) {
    override fun buildModels(data: List<HomeSearchItem>?) {
        data?.forEach {
            when (it) {
                is HomeSearchItem.ContactsItem -> renderContactView(it)
                is HomeSearchItem.CustomerItem -> renderCustomerView(it)
                HomeSearchItem.FilterItem -> renderFilterView()
                is HomeSearchItem.HeaderItem -> renderHeaderView(it)
                is HomeSearchItem.ImportCustomerContactItem -> renderImportCustomerContact(it)
                is HomeSearchItem.NoUserFoundItem -> renderNoUserFoundItem(it)
                HomeSearchItem.ShimmerListLoadingItem -> renderShimmerListLoading()
                is HomeSearchItem.SupplierItem -> renderSupplierView(it)
            }
        }
    }

    private fun renderSupplierView(supplierItem: HomeSearchItem.SupplierItem) {
        homeSupplierView {
            id("supplier${supplierItem.id}")
            supplierDetails(
                HomeSupplierView.HomeSupplierData(
                    id = supplierItem.id,
                    supplier = supplierItem.supplier,
                    syncType = supplierItem.syncType,
                )
            )
            tracker(tracker)
            listener(fragment)
            performanceTracker(performanceTracker)
        }
    }

    private fun renderCustomerView(customerItem: HomeSearchItem.CustomerItem) {
        homeSearchCustomerView {
            id("customer${customerItem.customerId}")
            homeCustomerItem(customerItem)
            listener(fragment)
        }
    }

    private fun renderContactView(contactsItem: HomeSearchItem.ContactsItem) {
        addContactView {
            id(contactsItem.contact.phonebookId)
            contacts(contactsItem.contact)
            listener(fragment)
        }
    }

    private fun renderFilterView() {
        filterView {
            id("filter_view")
            sortProp(Sort.sortfilter.size)
            listener(fragment)
        }
    }

    private fun renderHeaderView(headerItem: HomeSearchItem.HeaderItem) {
        headerView {
            id(headerItem.title)
            title(fragment.getString(headerItem.title))
        }
    }

    private fun renderImportCustomerContact(importCustomerContactItem: HomeSearchItem.ImportCustomerContactItem) {
        importCustomerContactView {
            id("import_customer_contact_view")
            source(importCustomerContactItem.source)
            listener(fragment)
        }
    }

    private fun renderNoUserFoundItem(noUserFoundItem: HomeSearchItem.NoUserFoundItem) {
        noUserFoundView {
            id("no_customer_view_1")
            noUserFoundMessage(fragment.getString(R.string.contact_not_found, noUserFoundItem.searchQuery))
            showLoader(noUserFoundItem.addRelationshipLoading)
            listener(fragment)
        }
    }

    private fun renderShimmerListLoading() {
        shimmerListLoadingView {
            id("loading_contact")
        }
    }
}
