package `in`.okcredit.collection_ui.ui.home_menu

import `in`.okcredit.collection.contract.CollectionNavigator
import `in`.okcredit.collection_ui.R
import `in`.okcredit.collection_ui.databinding.FragmentHomePaymentsContainerBinding
import `in`.okcredit.collection_ui.ui.home_menu.HomePaymentsContainerContract.*
import `in`.okcredit.collection_ui.ui.passbook.PassbookActivity
import `in`.okcredit.collection_ui.ui.passbook.payments.OnlinePaymentNavigationListener
import `in`.okcredit.collection_ui.ui.passbook.payments.OnlinePaymentsFragment
import `in`.okcredit.collection_ui.ui.passbook.payments.OnlinePaymentsFragment.Companion.SOURCE_HOME_MENU
import `in`.okcredit.shared.base.BaseFragment
import `in`.okcredit.shared.base.UserIntent
import android.os.Bundle
import android.view.View
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import dagger.Lazy
import io.reactivex.Observable
import tech.okcredit.android.base.extensions.addFragment
import tech.okcredit.android.base.extensions.viewLifecycleScoped
import javax.inject.Inject

class HomePaymentsContainerFragment :
    BaseFragment<State, ViewEvent, Intent>("HomePaymentsContainer", R.layout.fragment_home_payments_container),
    OnlinePaymentNavigationListener {

    @Inject
    lateinit var collectionNavigator: Lazy<CollectionNavigator>

    internal val binding: FragmentHomePaymentsContainerBinding by viewLifecycleScoped(
        FragmentHomePaymentsContainerBinding::bind
    )

    private val lifecycleCallback = object : FragmentManager.FragmentLifecycleCallbacks() {

        override fun onFragmentResumed(fm: FragmentManager, f: Fragment) {
            if (f is OnlinePaymentsFragment) {
                binding.buttonViewQr.extend()
                binding.buttonViewQr.show()
                binding.toolbar.setTitle(R.string.online_payments)
                binding.toolbar.navigationIcon = null
            }
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        childFragmentManager.registerFragmentLifecycleCallbacks(lifecycleCallback, true)

        binding.buttonViewQr.setOnClickListener {
            startActivity(collectionNavigator.get().qrCodeIntent(requireContext()))
        }
    }

    private fun showPaymentsList() {
        if (childFragmentManager.findFragmentById(R.id.fragmentHolder) != null) return

        val onlinePaymentsFragment = OnlinePaymentsFragment.newInstance(SOURCE_HOME_MENU)
        onlinePaymentsFragment.setListener(this)
        childFragmentManager.addFragment(
            fragment = onlinePaymentsFragment,
            holder = R.id.fragmentHolder,
            addToBackStack = false,
        )
    }

    override fun moveToPaymentDetail(paymentId: String, customerId: String?) {
        startActivity(
            PassbookActivity.getPaymentDetailIntent(
                requireContext(),
                paymentId,
                customerId,
                SOURCE_HOME_MENU
            )
        )
    }

    override fun userIntents(): Observable<UserIntent> {
        return Observable.ambArray()
    }

    override fun onResume() {
        super.onResume()
        // refresh merchant payment whenever user returns to this tab
        pushIntent(Intent.RefreshMerchantPayments)
    }

    override fun render(state: State) {
        binding.cardEmptyCollections.isVisible = state.showEmptyCollections
        binding.fragmentHolder.isVisible = !state.showEmptyCollections
    }

    override fun handleViewEvent(event: ViewEvent) {
        when (event) {
            ViewEvent.ShowTransactionHistory -> showPaymentsList()
        }
    }

    override fun loadIntent(): UserIntent {
        return Intent.Load
    }
}
