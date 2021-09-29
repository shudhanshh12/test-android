package `in`.okcredit.merchant.customer_ui.ui.subscription.list

import `in`.okcredit.merchant.customer_ui.R
import `in`.okcredit.merchant.customer_ui.databinding.SubscriptionListScreenBinding
import `in`.okcredit.merchant.customer_ui.ui.subscription.SubscriptionEventTracker
import `in`.okcredit.merchant.customer_ui.ui.subscription.add.AddSubscriptionContract
import `in`.okcredit.merchant.customer_ui.ui.subscription.list.SubscriptionListContract.*
import `in`.okcredit.merchant.customer_ui.ui.subscription.list.epoxy.SubscriptionController
import `in`.okcredit.shared.base.BaseFragment
import `in`.okcredit.shared.base.UserIntent
import android.os.Bundle
import android.view.View
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.NavHostFragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import dagger.Lazy
import io.reactivex.Observable
import tech.okcredit.android.base.extensions.gone
import tech.okcredit.android.base.extensions.longToast
import tech.okcredit.android.base.extensions.viewLifecycleScoped
import tech.okcredit.android.base.extensions.visible
import javax.inject.Inject

class SubscriptionListFragment :
    BaseFragment<State, ViewEvent, Intent>(
        "SubscriptionListScreen",
        R.layout.subscription_list_screen
    ) {

    private val binding: SubscriptionListScreenBinding by viewLifecycleScoped(SubscriptionListScreenBinding::bind)

    @Inject
    lateinit var controller: Lazy<SubscriptionController>

    @Inject
    lateinit var subscriptionEventTracker: Lazy<SubscriptionEventTracker>

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.toolbar.setNavigationOnClickListener { requireActivity().onBackPressed() }

        binding.rvSubscription.apply {
            layoutManager = LinearLayoutManager(requireActivity())
            adapter = controller.get().adapter
        }

        controller.get().setItemClickListener(::onItemClicked)

        binding.buttonAdd.setOnClickListener { pushIntent(Intent.AddSubscriptionClicked) }
        binding.swipeRefresh.setOnRefreshListener { pushIntent(Intent.RefreshData) }

        val navController = findNavController(this)
        val currentBackStackEntry = navController.currentBackStackEntry!!
        val savedStateHandle = currentBackStackEntry.savedStateHandle
        savedStateHandle.getLiveData<Boolean>(AddSubscriptionContract.SUBSCRIPTION_ADDED)
            .observe(
                currentBackStackEntry,
                {
                    if (it == true) {
                        lifecycleScope.launchWhenResumed {
                            binding.swipeRefresh.isRefreshing = true
                            pushIntentWithDelay(Intent.RefreshData)
                        }
                    }
                }
            )
    }

    private fun onItemClicked(id: String) {
        pushIntent(Intent.ItemClicked(id))
    }

    override fun userIntents(): Observable<UserIntent> {
        return Observable.ambArray()
    }

    override fun render(state: State) {
        if (state.loading) {
            binding.groupEmpty.gone()
            if (state.list.isNullOrEmpty()) binding.progressLoading.visible()
        } else {
            binding.progressLoading.gone()
            binding.swipeRefresh.isRefreshing = false
            if (!state.list.isNullOrEmpty()) {
                controller.get().setData(state.list)
                binding.groupEmpty.gone()
            } else {
                binding.groupEmpty.visible()
            }
        }

        if (state.customerName.isNullOrEmpty().not()) {
            binding.textCustomerName.text = state.customerName
        }
    }

    override fun handleViewEvent(event: ViewEvent) = when (event) {
        ViewEvent.NetworkErrorToast -> longToast(R.string.interent_error)
        ViewEvent.ServerErrorToast -> longToast(R.string.err_default)
        is ViewEvent.GoToAddSubscription -> moveToAddSubscription(event.customerId)
        is ViewEvent.GoToSubscriptionDetail -> {
            val action = SubscriptionListFragmentDirections.actionSubscriptionDetail(
                event.id,
                SubscriptionEventTracker.SUBSCRIPTION_PAGE,
                event.customerId ?: "",
                event.subscription
            )
            findNavController(this).navigate(action)
        }
    }

    private fun moveToAddSubscription(customerId: String?, mobile: String? = null) {
        subscriptionEventTracker.get().trackSubscriptionStarted(
            accountId = customerId ?: "",
            mobile = mobile
        )
        val action = SubscriptionListFragmentDirections.actionAddSubscription()
        findNavController(this).navigate(action)
    }

    override fun loadIntent() = Intent.Load
}
