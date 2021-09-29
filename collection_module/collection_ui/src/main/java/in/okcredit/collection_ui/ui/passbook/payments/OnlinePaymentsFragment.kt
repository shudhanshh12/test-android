package `in`.okcredit.collection_ui.ui.passbook.payments

import `in`.okcredit.backend.utils.CurrencyUtil
import `in`.okcredit.collection.contract.CollectionOnlinePayment
import `in`.okcredit.collection_ui.R
import `in`.okcredit.collection_ui.analytics.OnlineCollectionTracker
import `in`.okcredit.collection_ui.databinding.OnlinePaymentsFragmentBinding
import `in`.okcredit.collection_ui.ui.passbook.add_to_khata.AddToKhataDialog
import `in`.okcredit.collection_ui.ui.passbook.payments.OnlinePaymentsContract.*
import `in`.okcredit.collection_ui.ui.passbook.payments.TransactionFilter.*
import `in`.okcredit.collection_ui.ui.passbook.payments.views.OnlinePaymentsView
import `in`.okcredit.home.HomeNavigator
import `in`.okcredit.shared.base.BaseFragment
import `in`.okcredit.shared.base.UserIntent
import android.content.Context
import android.os.Bundle
import android.view.View
import androidx.core.os.bundleOf
import androidx.core.view.isVisible
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import io.reactivex.Observable
import org.joda.time.DateTime
import tech.okcredit.android.base.extensions.disableScreanCapture
import tech.okcredit.android.base.extensions.shortToast
import tech.okcredit.android.base.extensions.viewLifecycleScoped
import tech.okcredit.app_contract.LegacyNavigator
import javax.inject.Inject

class OnlinePaymentsFragment :
    BaseFragment<State, ViewEvent, Intent>(
        "OnlinePaymentsScreen",
        R.layout.online_payments_fragment
    ),
    OnlinePaymentsView.Listener,
    DateFilterListDialog.Listener,
    TransactionTypeFilterDialog.Listener {

    private val binding: OnlinePaymentsFragmentBinding by viewLifecycleScoped(OnlinePaymentsFragmentBinding::bind)
    private val controller = OnlinePaymentsController(this)
    private var dialog: DateFilterListDialog? = null
    private var selectedPaymentId: String? = null
    private var isInitialDataLoad: Boolean = true

    @Inject
    lateinit var homeNavigator: HomeNavigator

    @Inject
    lateinit var legacyNavigator: LegacyNavigator

    @Inject
    lateinit var onlineCollectionTracker: OnlineCollectionTracker

    private var navigationListener: OnlinePaymentNavigationListener? = null

    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (context is OnlinePaymentNavigationListener) {
            navigationListener = context
        }
    }

    override fun onDetach() {
        super.onDetach()
        navigationListener = null
    }

    fun setListener(navigationListener: OnlinePaymentNavigationListener?) {
        this.navigationListener = navigationListener
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        requireActivity().window.disableScreanCapture()
        super.onViewCreated(view, savedInstanceState)
        initList()
        setListeners()
        binding.rootView.setTracker(performanceTracker)
    }

    private fun initList() {
        binding.recyclerView.adapter = controller.adapter
        binding.recyclerView.layoutManager = LinearLayoutManager(requireContext())
    }

    private fun setListeners() {
        binding.buttonFilterTime.setOnClickListener {
            onlineCollectionTracker.trackDateRangeSelect(getCurrentState().source)
            showFilterDialog()
        }
        binding.buttonFilterTransactionType.setOnClickListener {
            onlineCollectionTracker.trackDateRangeSelect(getCurrentState().source)
            showTransactionFilterDialog()
        }
    }

    private fun showTransactionFilterDialog() {
        val fragment = TransactionTypeFilterDialog.getInstance(getCurrentState().transactionFilter)
        fragment.setListener(this)
        fragment.show(childFragmentManager, TransactionTypeFilterDialog::class.java.simpleName)
    }

    override fun userIntents(): Observable<UserIntent> {
        return Observable.ambArray()
    }

    override fun loadIntent(): UserIntent {
        return Intent.Load
    }

    override fun render(state: State) {
        controller.setState(state)
        displayFilter(state.filter)
        displayTotal(state)
        displayTransactionFilter(state.transactionFilter)
        binding.textNoResult.isVisible = state.filteredList.isEmpty()
    }

    private fun displayTransactionFilter(transactionFilter: TransactionFilter) {
        val filterName = when (transactionFilter) {
            CUSTOMER_COLLECTIONS -> getString(R.string.t_003_transaction_history_payments_received)
            SUPPLIER_COLLECTIONS -> getString(R.string.t_003_transaction_history_payment_given)
            ALL -> getString(R.string.t_003_transaction_history_all_transactions)
        }
        binding.buttonFilterTransactionType.text = filterName
    }

    private fun displayTotal(state: State) {
        binding.textCustomerCollectionsTotal.text =
            getString(R.string.rupees, CurrencyUtil.formatV2(state.totalCustomerPayments))
        binding.textSupplierCollectionsTotal.text =
            getString(R.string.rupees, CurrencyUtil.formatV2(state.totalSupplierPayments))
    }

    private fun displayFilter(filters: DateFilterListDialog.Filters) {
        binding.apply {
            buttonFilterTime.text = when (filters) {
                DateFilterListDialog.Filters.Today -> getString(R.string.today)
                DateFilterListDialog.Filters.Last7Days -> getString(R.string.last_7_days)
                DateFilterListDialog.Filters.Last30Days -> getString(R.string.last_thirty_days)
                DateFilterListDialog.Filters.Last3Months -> getString(R.string.last_3_months)
                DateFilterListDialog.Filters.Overall -> getString(R.string.overall)
            }
        }
        showFilteredData(filters)
    }

    override fun handleViewEvent(event: ViewEvent) {
        when (event) {
            is ViewEvent.OnChangeFilter -> showFilteredData(event.filter)
            is ViewEvent.OnLoadDataSuccessEvent -> {
                if (isInitialDataLoad) {
                    if (event.isNotEmpty || getCurrentState().filter == DateFilterListDialog.Filters.Overall) {
                        onlineCollectionTracker.trackLoadEventOnlineCollection(getCurrentState().source)
                        isInitialDataLoad = false
                    } else {
                        showNextFilter()
                    }
                }
            }
            ViewEvent.OnError -> shortToast(R.string.err_default)
            is ViewEvent.MoveToPaymentDetails -> {
                navigationListener?.moveToPaymentDetail(event.paymentId)
            }
            is ViewEvent.MoveToTransactionDetails -> {
                legacyNavigator.goToTransactionDetailFragment(requireContext(), event.transactionId)
            }
        }
    }

    private fun showNextFilter() {
        isInitialDataLoad = true
        when (getCurrentState().filter) {
            DateFilterListDialog.Filters.Today -> onFilterClicked(DateFilterListDialog.Filters.Last7Days)
            DateFilterListDialog.Filters.Last7Days -> onFilterClicked(DateFilterListDialog.Filters.Last30Days)
            DateFilterListDialog.Filters.Last30Days -> onFilterClicked(DateFilterListDialog.Filters.Last3Months)
            DateFilterListDialog.Filters.Last3Months -> onFilterClicked(DateFilterListDialog.Filters.Overall)
            DateFilterListDialog.Filters.Overall -> {
                // do nothing
            }
        }
    }

    private fun showFilteredData(filters: DateFilterListDialog.Filters) {
        val filterIntent: Intent.GetOnlinePayments = when (filters) {
            DateFilterListDialog.Filters.Today -> showToday()
            DateFilterListDialog.Filters.Last7Days -> showLast7Days()
            DateFilterListDialog.Filters.Last30Days -> showLast30Days()
            DateFilterListDialog.Filters.Last3Months -> showLast3Months()
            DateFilterListDialog.Filters.Overall -> showFromBeginning()
        }
        pushIntent(filterIntent)
        if (isInitialDataLoad.not()) {
            onlineCollectionTracker.trackDateRangeUpdate(
                filters.value,
                filterIntent.startTime,
                filterIntent.endTime,
                getCurrentState().source
            )
        }
    }

    private fun showLast3Months(): Intent.GetOnlinePayments {
        val startTime = DateTime.now().minusMonths(3).withTimeAtStartOfDay()
        val endTime = DateTime.now().plusDays(1).withTimeAtStartOfDay()
        return Intent.GetOnlinePayments(startTime, endTime)
    }

    private fun showFromBeginning(): Intent.GetOnlinePayments {
        val startTime = DateTime(0)
        val endTime = DateTime.now().plusDays(1).withTimeAtStartOfDay()
        return Intent.GetOnlinePayments(startTime, endTime)
    }

    private fun showToday(): Intent.GetOnlinePayments {
        val startTime = DateTime.now().withTimeAtStartOfDay()
        val endTime = DateTime.now().plusDays(1).withTimeAtStartOfDay()
        return Intent.GetOnlinePayments(startTime, endTime)
    }

    private fun showLast7Days(): Intent.GetOnlinePayments {
        val startTime = DateTime.now().minusDays(7).withTimeAtStartOfDay()
        val endTime = DateTime.now().plusDays(1).withTimeAtStartOfDay()
        return Intent.GetOnlinePayments(startTime, endTime)
    }

    private fun showLast30Days(): Intent.GetOnlinePayments {
        val startTime = DateTime.now().minusDays(30).withTimeAtStartOfDay()
        val endTime = DateTime.now().plusDays(1).withTimeAtStartOfDay()
        return Intent.GetOnlinePayments(startTime, endTime)
    }

    override fun clickedOnlinePaymentsView(collectionOnlinePayment: CollectionOnlinePayment) {
        onlineCollectionTracker.trackClickTransactionDetail(collectionOnlinePayment.id, getCurrentState().source)
        pushIntent(Intent.ItemClicked(collectionOnlinePayment))
    }

    override fun addToKhata(paymentId: String) {
        onlineCollectionTracker.trackClickAddToKhata(
            OnlineCollectionTracker.Screen.collectionPaymentsView,
            paymentId,
            getCurrentState().source
        )
        selectedPaymentId = paymentId
        homeNavigator.goToHomeSearchScreenForResult(this, HOME_SEARCH_REQUEST_CODE, true)
    }

    private fun showFilterDialog() {
        if (dialog == null) {
            dialog = DateFilterListDialog()
        }
        dialog?.setFilter(getCurrentState().filter)
        dialog?.setListener(this)
        if (dialog!!.isVisible.not()) {
            dialog?.show(childFragmentManager, DateFilterListDialog.TAG)
        }
    }

    override fun onFilterClicked(filters: DateFilterListDialog.Filters) {
        lifecycleScope.launchWhenResumed {
            pushIntent(Intent.ChangeFilterRange(filters))
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: android.content.Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == HOME_SEARCH_REQUEST_CODE) {
            data?.let {
                val customerId = it.getStringExtra(CUSTOMER_ID)
                goToAddToKhataDialog(customerId)
            }
        }
    }

    private fun goToAddToKhataDialog(customerId: String?) {
        customerId?.let { id ->
            val dialog = AddToKhataDialog.newInstance(getCurrentState().source).apply {
                arguments = bundleOf(PAYMENT_ID to selectedPaymentId, CUSTOMER_ID to id)
            }
            dialog.show(childFragmentManager, AddToKhataDialog::class.java.simpleName)
        }
    }

    companion object {
        const val TAG = "OnlinePaymentsScreen"
        const val HOME_SEARCH_REQUEST_CODE = 9198
        const val CUSTOMER_ID = "customer_id"
        const val PAYMENT_ID = "payment_id"

        const val ARG_SOURCE = "arg_source"
        const val SOURCE_HOME_MENU = "home_menu"
        const val SOURCE_MERCHANT_QR = "merchant_qr"

        fun newInstance(source: String): OnlinePaymentsFragment {
            val args = Bundle()
            args.putString(ARG_SOURCE, source)
            val fragment = OnlinePaymentsFragment()
            fragment.arguments = args
            return fragment
        }
    }

    override fun onTransactionFilterClicked(filter: TransactionFilter) {
        lifecycleScope.launchWhenResumed {
            pushIntent(Intent.ChangeTransactionFilter(filter))
        }
    }
}
