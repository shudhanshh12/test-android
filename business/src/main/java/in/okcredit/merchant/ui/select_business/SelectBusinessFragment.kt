package `in`.okcredit.merchant.ui.select_business

import `in`.okcredit.merchant.merchant.R
import `in`.okcredit.merchant.merchant.databinding.FragmentSelectBusinessBinding
import `in`.okcredit.merchant.ui.select_business.view.SelectBusinessItemView
import `in`.okcredit.shared.base.BaseFragment
import `in`.okcredit.shared.base.UserIntent
import android.os.Bundle
import android.view.View
import androidx.recyclerview.widget.LinearLayoutManager
import io.reactivex.Observable
import tech.okcredit.android.base.extensions.shortToast
import tech.okcredit.android.base.extensions.viewLifecycleScoped
import java.lang.ref.WeakReference
import javax.inject.Inject

class SelectBusinessFragment :
    BaseFragment<SelectBusinessContract.State, SelectBusinessContract.ViewEvent, SelectBusinessContract.Intent>(
        "SelectBusinessFragment",
        R.layout.fragment_select_business
    ),
    SelectBusinessItemView.Listener {

    private val binding by viewLifecycleScoped(FragmentSelectBusinessBinding::bind)

    private val controller = SelectBusinessController(this)

    @Inject
    internal lateinit var analytics: SelectBusinessAnalytics

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initList()
        analytics.trackPageViewed()
    }

    override fun loadIntent(): UserIntent? {
        return SelectBusinessContract.Intent.Load
    }

    override fun userIntents(): Observable<UserIntent> {
        return Observable.mergeArray(
            Observable.empty()
        )
    }

    override fun render(state: SelectBusinessContract.State) {
        controller.setData(state.businessList)
    }

    override fun handleViewEvent(event: SelectBusinessContract.ViewEvent) {
        when (event) {
            is SelectBusinessContract.ViewEvent.ShowError -> shortToast(event.msg)
        }
    }

    override fun onSelect(businessId: String, businessName: String) {
        analytics.trackBusinessSelected(businessId)
        pushIntent(SelectBusinessContract.Intent.SetActiveBusiness(businessId, businessName, WeakReference(activity)))
    }

    private fun initList() {
        binding.rvBusinessList.adapter = controller.adapter
        binding.rvBusinessList.layoutManager = LinearLayoutManager(requireContext())
    }

    override fun onBackPressed(): Boolean {
        activity?.finish()
        return true
    }
}
