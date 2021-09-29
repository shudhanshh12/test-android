package `in`.okcredit.merchant.customer_ui.ui.staff_link

import `in`.okcredit.merchant.customer_ui.R
import `in`.okcredit.merchant.customer_ui.databinding.StaffLinkActivityBinding
import `in`.okcredit.merchant.customer_ui.ui.staff_link.add.StaffLinkAddCustomerContract
import `in`.okcredit.merchant.customer_ui.ui.staff_link.add.StaffLinkAddCustomerFragment
import `in`.okcredit.merchant.customer_ui.ui.staff_link.edit.StaffLinkEditDetailsContract
import `in`.okcredit.merchant.customer_ui.ui.staff_link.edit.StaffLinkEditDetailsFragment
import `in`.okcredit.merchant.customer_ui.ui.staff_link.education.StaffLinkEducationFragment
import `in`.okcredit.merchant.customer_ui.usecase.GetCollectionStaffLinkScreen
import `in`.okcredit.shared.base.BaseActivity
import `in`.okcredit.shared.base.UserIntent
import android.os.Bundle
import androidx.core.view.isVisible
import io.reactivex.Observable
import tech.okcredit.android.base.extensions.replaceFragment
import tech.okcredit.android.base.extensions.toArrayList
import tech.okcredit.android.base.extensions.viewLifecycleScoped

class StaffLinkActivity :
    BaseActivity<StaffLinkContract.State, StaffLinkContract.ViewEvent, StaffLinkContract.Intent>("StaffLink"),
    NavigationListener {

    private val binding: StaffLinkActivityBinding by viewLifecycleScoped(StaffLinkActivityBinding::inflate)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
    }

    override fun userIntents(): Observable<UserIntent> {
        return Observable.ambArray()
    }

    override fun render(state: StaffLinkContract.State) {
        binding.progressBar.isVisible = state.loading
    }

    override fun handleViewEvent(event: StaffLinkContract.ViewEvent) {
        when (event) {
            is StaffLinkContract.ViewEvent.MoveToScreen -> navigateToScreen(event.staffLinkScreen)
        }
    }

    private fun navigateToScreen(staffLinkScreen: GetCollectionStaffLinkScreen.StaffLinkScreen) {
        when (staffLinkScreen) {
            is GetCollectionStaffLinkScreen.StaffLinkScreen.ActiveStaffLink -> navigateToAddEditDetails(
                linkId = staffLinkScreen.linkId,
                link = staffLinkScreen.link,
                selectedCustomers = staffLinkScreen.customerIds,
                linkCreateTime = staffLinkScreen.createTime,
            )
            GetCollectionStaffLinkScreen.StaffLinkScreen.SelectCustomer -> navigateToSelectCustomer()
            GetCollectionStaffLinkScreen.StaffLinkScreen.Education -> navigateToEducationScreen()
        }
    }

    override fun loadIntent(): UserIntent {
        return StaffLinkContract.Intent.Load
    }

    override fun navigateToEducationScreen() {
        supportFragmentManager.replaceFragment(
            fragment = StaffLinkEducationFragment(),
            holder = R.id.staffLinkFragmentHolder,
            addToBackStack = false,
        )
    }

    override fun navigateToSelectCustomer(
        linkId: String?,
        selectedCustomers: List<String>?,
        link: String?,
        linkCreateTime: Long?,
    ) {
        val fragment = StaffLinkAddCustomerFragment().apply {
            if (!linkId.isNullOrEmpty() || !selectedCustomers.isNullOrEmpty() || !link.isNullOrEmpty()) {
                val bundle = Bundle()
                linkId?.let { bundle.putString(StaffLinkAddCustomerContract.ARG_LINK_ID, it) }
                link?.let { bundle.putString(StaffLinkAddCustomerContract.ARG_LINK, link) }
                linkCreateTime?.let { bundle.putLong(StaffLinkAddCustomerContract.ARG_LINK_CREATE_TIME, linkCreateTime) }
                selectedCustomers?.let {
                    bundle.putStringArrayList(
                        StaffLinkAddCustomerContract.ARG_SELECTED_CUSTOMERS,
                        it.toArrayList()
                    )
                }
                arguments = bundle
            }
        }
        supportFragmentManager.replaceFragment(
            fragment = fragment,
            holder = R.id.staffLinkFragmentHolder,
            addToBackStack = false,
        )
    }

    override fun navigateToAddEditDetails(
        linkId: String?,
        selectedCustomers: List<String>,
        link: String?,
        linkCreateTime: Long?,
    ) {
        val fragment = StaffLinkEditDetailsFragment().apply {
            val bundle = Bundle()
            linkId?.let { bundle.putString(StaffLinkEditDetailsContract.ARG_LINK_ID, it) }
            link?.let { bundle.putString(StaffLinkEditDetailsContract.ARG_LINK, link) }
            linkCreateTime?.let { bundle.putLong(StaffLinkEditDetailsContract.ARG_LINK_CREATE_TIME, linkCreateTime) }
            bundle.putStringArrayList(StaffLinkEditDetailsContract.ARG_SELECTED_CUSTOMERS, selectedCustomers.toArrayList())
            arguments = bundle
        }
        supportFragmentManager.replaceFragment(
            fragment = fragment,
            holder = R.id.staffLinkFragmentHolder,
            addToBackStack = false,
        )
    }
}

interface NavigationListener {
    fun navigateToEducationScreen()

    fun navigateToSelectCustomer(
        linkId: String? = null,
        selectedCustomers: List<String>? = null,
        link: String? = null,
        linkCreateTime: Long? = null,
    )

    fun navigateToAddEditDetails(
        linkId: String?,
        selectedCustomers: List<String>,
        link: String?,
        linkCreateTime: Long?,
    )
}
