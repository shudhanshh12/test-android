package `in`.okcredit.voice_first.ui.bulk_add.search_merchant

import `in`.okcredit.shared.base.BaseActivity
import `in`.okcredit.shared.base.UserIntent
import `in`.okcredit.voice_first.R
import `in`.okcredit.voice_first.data.bulk_add.entities.DraftMerchant
import `in`.okcredit.voice_first.databinding.ActivitySearchMerchantBinding
import `in`.okcredit.voice_first.ui.bulk_add.search_merchant.SearchMerchantContract.*
import `in`.okcredit.voice_first.ui.bulk_add.search_merchant.search_results_list.SearchControllerV2
import `in`.okcredit.voice_first.ui.bulk_add.search_merchant.search_results_list.SearchMerchantListener
import android.content.Context
import android.os.Bundle
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import com.airbnb.epoxy.EpoxyControllerAdapter
import com.airbnb.epoxy.EpoxyVisibilityTracker
import com.jakewharton.rxbinding3.widget.textChanges
import io.reactivex.Observable
import io.reactivex.subjects.PublishSubject
import tech.okcredit.android.base.extensions.gone
import tech.okcredit.android.base.extensions.hideSoftKeyboard
import tech.okcredit.android.base.extensions.longToast
import tech.okcredit.android.base.extensions.showSoftKeyboard
import tech.okcredit.android.base.extensions.viewLifecycleScoped
import tech.okcredit.android.base.extensions.visible
import java.util.concurrent.TimeUnit
import android.content.Intent as AndroidIntent

class SearchMerchantActivity :
    BaseActivity<State, ViewEvent, Intent>("SearchMerchantScreen"),
    SearchMerchantListener {

    private val resetDataPublishSubject: PublishSubject<Unit> = PublishSubject.create()

    private lateinit var searchController: SearchControllerV2

    private val binding by viewLifecycleScoped(ActivitySearchMerchantBinding::inflate)

    override fun onCreate(savedInstanceState: Bundle?) {
        setTheme(R.style.Base_OKCTheme)
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        initUi()
    }

    private fun initUi() {
        searchController = SearchControllerV2(context = this, listener = this)
        binding.searchEpoxy.setController(searchController)
        setListeners()
    }

    private fun setListeners() {
        binding.backButton.setOnClickListener {
            finish()
        }
        binding.btnClose.setOnClickListener {
            binding.searchInput.setText("")
            hideSoftKeyboard()
            pushIntent(Intent.ShowSearchInput(false))
        }
        binding.searchImg.setOnClickListener {
            pushIntent(Intent.ShowSearchInput())
        }

        val epoxyVisibilityTracker = EpoxyVisibilityTracker()
        epoxyVisibilityTracker.attach(binding.searchEpoxy)
    }

    override fun loadIntent(): UserIntent {
        return Intent.Load
    }

    override fun userIntents(): Observable<UserIntent> {
        return Observable.mergeArray(

            binding.searchInput
                .textChanges()
                .debounce(300, TimeUnit.MILLISECONDS)
                .map { Intent.SearchQuery(it.toString()) },

            resetDataPublishSubject
                .map { Intent.ResetData }
        )
    }

    override fun render(state: State) {
        binding.btnClose.isVisible = state.searchQuery.isNotEmpty()

        if (state.hideSearchInput) {
            hideSearchInput()
        } else {
            showSearchInput()
        }

        searchController.setData(state.itemList)
        searchController.adapter.scrollToTopOnItemInsertItem(binding.searchEpoxy)
    }

    private fun showSearchInput() {
        binding.selectCustomerGrp.gone()
        binding.searchInputGrp.visible()
        binding.searchInput.requestFocus()
    }

    private fun hideSearchInput() {
        binding.selectCustomerGrp.visible()
        binding.searchInputGrp.gone()
    }

    override fun onPause() {
        super.onPause()
        hideSoftKeyboard()
    }

    override fun onSelected(merchant: DraftMerchant) {
        setResult(RESULT_OK, SearchMerchantActivityContract.intentFromMerchant(merchant))
        finish()
    }

    private fun showError() {
        longToast(R.string.err_default)
    }

    private fun showInternetError() {
        longToast(R.string.no_internet_msg)
    }

    override fun handleViewEvent(event: ViewEvent) {
        when (event) {
            is ViewEvent.ShowError -> showError()
            is ViewEvent.ShowInternetError -> showInternetError()
            is ViewEvent.ShowKeyboard -> showSoftKeyboard(binding.searchInput)
        }
    }

    private fun EpoxyControllerAdapter.scrollToTopOnItemInsertItem(recyclerView: RecyclerView) {
        this.registerAdapterDataObserver(object : RecyclerView.AdapterDataObserver() {
            override fun onItemRangeInserted(positionStart: Int, itemCount: Int) {
                recyclerView.scrollToPosition(0)
            }
        })
    }

    companion object {
        fun getIntent(context: Context) = AndroidIntent(context, SearchMerchantActivity::class.java)
    }
}
