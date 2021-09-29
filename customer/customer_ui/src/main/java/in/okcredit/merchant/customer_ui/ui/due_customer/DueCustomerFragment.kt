package `in`.okcredit.merchant.customer_ui.ui.due_customer

import `in`.okcredit.analytics.Tracker
import `in`.okcredit.backend.contract.Customer
import `in`.okcredit.merchant.customer_ui.R
import `in`.okcredit.merchant.customer_ui.ui.due_customer.views.DueCustomerItemView
import `in`.okcredit.shared.base.BaseScreen
import `in`.okcredit.shared.base.UserIntent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.view.WindowManager
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.perf.metrics.AddTrace
import com.jakewharton.rxbinding3.view.clicks
import io.reactivex.Observable
import io.reactivex.subjects.PublishSubject
import kotlinx.android.synthetic.main.due_customer_fragment.*
import tech.okcredit.app_contract.LegacyNavigator
import tech.okcredit.base.Traces
import java.util.concurrent.TimeUnit
import javax.inject.Inject

class DueCustomerFragment :
    BaseScreen<DueCustomerContract.State>("DueCustomerScreen"),
    DueCustomerContract.Navigator,
    DueCustomerItemView.CustomerSelectionListener {

    @Inject
    internal lateinit var legacyNavigator: LegacyNavigator

    @Inject
    internal lateinit var tracker: Tracker

    private lateinit var dueCustomerController: DueCustomerController
    private val customerSelected: PublishSubject<Customer> = PublishSubject.create()
    private val searchQueryChangePublishSubject: PublishSubject<String> = PublishSubject.create()
    private var initialSoftInputMode: Int? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {

        initialSoftInputMode = activity?.window?.getSoftInputMode()
        activity?.window?.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN)

        return inflater.inflate(R.layout.due_customer_fragment, container, false)
    }

    private fun Window.getSoftInputMode(): Int {
        return attributes.softInputMode
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        dueCustomerController = DueCustomerController(this)
        recycler_view.layoutManager = LinearLayoutManager(context)
        recycler_view.adapter = dueCustomerController.adapter
        root_view.setTracker(performanceTracker)
    }

    override fun loadIntent(): UserIntent {
        return DueCustomerContract.Intent.Load
    }

    override fun userIntents(): Observable<UserIntent> {
        return Observable.mergeArray(

            select_all_customer.clicks()
                .map { DueCustomerContract.Intent.SelectAll(select_all_customer.isChecked) },

            send_reminders.clicks()
                .throttleFirst(300, TimeUnit.MILLISECONDS)
                .map {
                    tracker.trackCollectionBulkRemainder(
                        getCurrentState().selectAll,
                        getSelectedAndVisibleCustomerIds().size,
                        getCurrentState().dueCustomers.size
                    )
                    DueCustomerContract.Intent.SendReminders(getSelectedAndVisibleCustomerIds())
                },

            not_now.clicks()
                .throttleFirst(300, TimeUnit.MILLISECONDS)
                .map {
                    DueCustomerContract.Intent.NotNow
                },

            customerSelected
                .map {
                    DueCustomerContract.Intent.SelectItem(it.id, getCurrentState().selectedCustomerIds)
                },

            searchQueryChangePublishSubject
                .debounce(200, TimeUnit.MILLISECONDS)
                .map {
                    DueCustomerContract.Intent.SearchQuery(it)
                }
        )
    }

    private fun getSelectedAndVisibleCustomerIds(): MutableList<String> {
        val selectedCustomerIds = getCurrentState().selectedCustomerIds
        val dueCustomers = getCurrentState().dueCustomers

        val visibleSelectedCustomerIds = mutableListOf<String>()

        for (customer in dueCustomers) {
            if (selectedCustomerIds.contains(customer.id)) {
                visibleSelectedCustomerIds.add(customer.id)
            }
        }

        return visibleSelectedCustomerIds
    }

    @AddTrace(name = Traces.RENDER_DUE_CUSTOMER)
    override fun render(state: DueCustomerContract.State) {

        dueCustomerController.setState(state)

        if (state.selectedCustomerIds.isNullOrEmpty()) {
            send_reminders.isClickable = false
            send_reminders.setTextColor(ContextCompat.getColor(requireContext(), R.color.grey600))
            send_reminders.setBackgroundResource(R.drawable.circular_border_grey_border)
        } else {
            send_reminders.isClickable = true
            send_reminders.setTextColor(ContextCompat.getColor(requireContext(), R.color.white))
            send_reminders.setBackgroundResource(R.drawable.circular_border_dark_green)
        }

        select_all_customer.isChecked =
            state.dueCustomers.size == state.selectedCustomerIds.size || allVisibleCustomersSelected(state)

        button_loader.visibility = if (state.isLoading) View.VISIBLE else View.GONE

        search_customer.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
                // called before text changed
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                // called on text change
            }

            override fun afterTextChanged(editable: Editable?) {
                searchQueryChangePublishSubject.onNext(editable.toString())
            }
        })
    }

    private fun allVisibleCustomersSelected(state: DueCustomerContract.State): Boolean {
        var visibleCustomersSelected = true
        for (customer in state.dueCustomers) {
            if (!state.selectedCustomerIds.contains(customer.id)) {
                visibleCustomersSelected = false
            }
        }

        return visibleCustomersSelected
    }

    override fun onDestroy() {
        super.onDestroy()
        initialSoftInputMode?.let { activity?.window?.setSoftInputMode(it) }
    }

    override fun onCustomerSelection(customer: Customer) {
        customerSelected.onNext(customer)
    }

    override fun goToMerchantDestinationpage(
        sourceScreen: String?,
        rewardsAmount: Long?,
        redirectToRewardsPage: Boolean?
    ) {
        activity?.runOnUiThread {
            activity?.finishAffinity()
            if (redirectToRewardsPage == true) {
                legacyNavigator.goToRewardsScreenByClearingBackStack(requireActivity())
            } else {
                legacyNavigator.goToMerchantDestinationScreenByClearingBackStack(requireActivity())
            }
        }
    }
}
