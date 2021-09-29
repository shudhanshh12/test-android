package tech.okcredit.home.ui.dashboard

import `in`.okcredit.dynamicview.DynamicViewKit
import `in`.okcredit.dynamicview.TargetSpec
import `in`.okcredit.shared.base.BaseFragment
import `in`.okcredit.shared.base.UserIntent
import android.os.Bundle
import android.view.View
import dagger.Lazy
import io.reactivex.Observable
import tech.okcredit.android.base.extensions.gone
import tech.okcredit.android.base.extensions.viewLifecycleScoped
import tech.okcredit.android.base.extensions.visible
import tech.okcredit.home.R
import tech.okcredit.home.databinding.DashboardFragmentBinding
import javax.inject.Inject

class DashboardFragment :
    BaseFragment<DashboardContract.State, DashboardContract.ViewEvent, DashboardContract.Intent>(
        "DashboardScreen",
        R.layout.dashboard_fragment
    ) {

    @Inject
    lateinit var dynamicViewKit: Lazy<DynamicViewKit>

    private val binding: DashboardFragmentBinding by viewLifecycleScoped(DashboardFragmentBinding::bind)

    companion object {
        const val TAG = "DashboardFragment"

        @JvmStatic
        fun newInstance() = DashboardFragment()
    }

    override fun loadIntent(): UserIntent {
        return DashboardContract.Intent.Load
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.rootView.setTracker(performanceTracker)
    }

    override fun userIntents(): Observable<UserIntent> {
        return Observable.ambArray()
    }

    override fun render(state: DashboardContract.State) {
        if (state.isLoading) {
            binding.shimmerView.visible()
            binding.dynamicView.gone()
        } else {
            binding.shimmerView.gone()
            binding.dynamicView.visible()

            state.customization?.let {
                val spec = TargetSpec(
                    name = state.customization.target,
                    allowAllComponents = true,
                    trackViewEvents = false
                )
                dynamicViewKit.get().render(binding.dynamicView, state.customization.component, spec)
            }
        }
    }

    override fun handleViewEvent(event: DashboardContract.ViewEvent) {}
}
