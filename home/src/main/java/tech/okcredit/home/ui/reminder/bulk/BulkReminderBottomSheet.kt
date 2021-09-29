package tech.okcredit.home.ui.reminder.bulk

import `in`.okcredit.shared.base.BaseFragment
import `in`.okcredit.shared.base.UserIntent
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.NavHostFragment
import androidx.recyclerview.widget.GridLayoutManager
import dagger.Lazy
import io.reactivex.Observable
import tech.okcredit.android.base.extensions.disable
import tech.okcredit.android.base.extensions.longToast
import tech.okcredit.android.base.extensions.viewLifecycleScoped
import tech.okcredit.home.R
import tech.okcredit.home.databinding.FragmentBulkReminderBinding
import tech.okcredit.home.ui.reminder.bulk.BulkReminderContract.*
import javax.inject.Inject
import android.content.Intent as StarterIntent

class BulkReminderBottomSheet :
    BaseFragment<State, ViewEvent, Intent>(
        "BulkReminderScreen"
    ) {

    private val binding by viewLifecycleScoped(FragmentBulkReminderBinding::bind)

    private lateinit var controller: BulkReminderController

    @Inject
    lateinit var bulkReminderEventsTracker: Lazy<BulkReminderEventsTracker>

    companion object {
        const val TAG = "BulkReminderBottomSheet"

        @JvmStatic
        fun start(context: Context) {
            val starter = StarterIntent(context, BulkReminderBottomSheet::class.java)
            context.startActivity(starter)
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return FragmentBulkReminderBinding.inflate(inflater, container, false).root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setUpCustomerList()

        binding.buttonCancel.setOnClickListener {
            NavHostFragment.findNavController(this).popBackStack()
            bulkReminderEventsTracker.get()
                .trackBulkReminderCancelled(merchant_id = if (isStateInitialized()) getCurrentState().merchantId else "")
        }
        binding.imageClose.setOnClickListener {
            bulkReminderEventsTracker.get()
                .trackBulkReminderDismissed(merchant_id = if (isStateInitialized()) getCurrentState().merchantId else "")
            NavHostFragment.findNavController(this).popBackStack()
        }
        binding.buttonSubmit.setOnClickListener {
            val selectedCustomers = getCurrentState().bulkReminderList.filter { it.checked }.map { it.customerId }
            if (selectedCustomers.isNotEmpty()) {
                bulkReminderEventsTracker.get().trackSendBulkReminder(
                    merchant_id = getCurrentState().merchantId,
                    accountIds = selectedCustomers.joinToString(),
                    collectionAdopted = getCurrentState().collectionAdopted,
                    numberOfAccountsSelected = selectedCustomers.size
                )
                pushIntent(Intent.SubmitClicked(selectedCustomers))
                it.disable()
            } else {
                longToast(R.string.msg_select_customer_for_reminder)
            }
        }
    }

    private fun setUpCustomerList() {
        binding.recyclerView.apply {
            layoutManager = GridLayoutManager(requireContext(), 4)
            controller = BulkReminderController()
            controller.spanCount = 4
            controller.setListener(::customerClicked)
            adapter = controller.adapter
        }
    }

    private fun customerClicked(id: String) {
        val customer = getCurrentState().bulkReminderList.firstOrNull { it.customerId == id }
        bulkReminderEventsTracker.get().trackSelectCustomer(
            merchant_id = getCurrentState().merchantId,
            accountId = id,
            dueAmount = customer?.amountDue ?: 0,
            selected = (customer?.checked ?: true).not()
        )
        pushIntent(Intent.SelectCustomer(id))
    }

    override fun userIntents(): Observable<UserIntent> = Observable.empty()

    override fun render(state: State) {
        if (!state.loading) {
            controller.setData(state.bulkReminderList)
        }
    }

    override fun loadIntent(): UserIntent = Intent.Load

    override fun handleViewEvent(event: ViewEvent) {
        when (event) {
            ViewEvent.Success -> onSuccessEvent()
            is ViewEvent.ShowError -> longToast(event.error)
        }
    }

    private fun onSuccessEvent() {
        bulkReminderEventsTracker.get().trackBulkReminderSuccess(merchant_id = getCurrentState().merchantId)
        NavHostFragment.findNavController(this).popBackStack()
    }
}
