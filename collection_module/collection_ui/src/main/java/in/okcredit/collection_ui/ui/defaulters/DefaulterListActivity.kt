package `in`.okcredit.collection_ui.ui.defaulters

import `in`.okcredit.collection_ui.databinding.DefaulterListFragmentBinding
import `in`.okcredit.shared.base.BaseActivity
import `in`.okcredit.shared.base.UserIntent
import android.os.Bundle
import androidx.recyclerview.widget.LinearLayoutManager
import dagger.Lazy
import io.reactivex.Observable
import tech.okcredit.android.base.extensions.gone
import tech.okcredit.android.base.extensions.viewLifecycleScoped
import tech.okcredit.android.base.extensions.visible
import tech.okcredit.app_contract.LegacyNavigator
import javax.inject.Inject

class DefaulterListActivity :
    BaseActivity<DefaulterListContract.State, DefaulterListContract.ViewEvent, DefaulterListContract.Intent>(
        "DefaulterListScreen",
    ),
    DefaulterView.DefaulterClickListener {

    @Inject
    lateinit var defaulterController: Lazy<DefaulterController>

    @Inject
    lateinit var legacyNavigator: Lazy<LegacyNavigator>

    private val binding: DefaulterListFragmentBinding by viewLifecycleScoped(DefaulterListFragmentBinding::inflate)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        initViews()
    }

    private fun initViews() {
        binding.rvDefaulters.layoutManager = LinearLayoutManager(this)
        binding.rvDefaulters.adapter = defaulterController.get().adapter
    }

    override fun loadIntent(): UserIntent {
        return DefaulterListContract.Intent.Load
    }

    override fun userIntents(): Observable<UserIntent> {
        return Observable.ambArray()
    }

    override fun render(state: DefaulterListContract.State) {
        if (state.isLoading) {
            binding.shimmerView.visible()
            binding.rvDefaulters.gone()
        } else {
            binding.shimmerView.gone()
            binding.rvDefaulters.visible()
            defaulterController.get().setState(state)
        }
    }

    override fun handleViewEvent(event: DefaulterListContract.ViewEvent) {}

    override fun onDefaulterClick(customerId: String) {
        legacyNavigator.get().goToCustomerScreen(this, customerId)
    }
}
