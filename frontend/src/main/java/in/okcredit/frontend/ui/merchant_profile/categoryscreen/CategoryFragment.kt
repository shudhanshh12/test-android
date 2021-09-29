package `in`.okcredit.frontend.ui.merchant_profile.categoryscreen

import `in`.okcredit.analytics.PropertyValue
import `in`.okcredit.analytics.Tracker
import `in`.okcredit.frontend.R
import `in`.okcredit.frontend.ui.merchant_profile.categoryscreen.views.CategoryFragmentView
import `in`.okcredit.frontend.ui.merchant_profile.categoryscreen.views.EmptyCategoryView
import `in`.okcredit.frontend.widget.searchview.SimpleSearchView
import `in`.okcredit.merchant.contract.Category
import `in`.okcredit.shared.base.BaseScreen
import `in`.okcredit.shared.base.UserIntent
import android.app.Activity
import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.UiThread
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.airbnb.epoxy.EpoxyVisibilityTracker
import com.google.android.material.snackbar.Snackbar
import com.jakewharton.rxbinding3.view.clicks
import io.reactivex.Observable
import io.reactivex.subjects.PublishSubject
import kotlinx.android.synthetic.main.category_fragment.*
import tech.okcredit.android.base.extensions.snackbar
import tech.okcredit.android.base.utils.keyboardUtils.KeyboardVisibilityEvent
import tech.okcredit.app_contract.LegacyNavigator
import java.util.concurrent.TimeUnit
import javax.inject.Inject

class CategoryFragment :
    BaseScreen<CategoryScreenContract.State>("CategoryScreen"),
    CategoryScreenContract.Navigator,
    CategoryFragmentView.Listener,
    EmptyCategoryView.Listener {

    private var alert: Snackbar? = null

    private val searchQuerySubject: PublishSubject<String> = PublishSubject.create()
    private val onCategoryClickedSubject: PublishSubject<Category> = PublishSubject.create()
    private val onOtherCategoryClickedSubject: PublishSubject<Unit> = PublishSubject.create()

    private lateinit var categoryScreenController: CategoryScreenController

    @Inject
    internal lateinit var legacyNavigator: LegacyNavigator

    @Inject
    internal lateinit var tracker: Tracker

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.category_fragment, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        categoryScreenController = CategoryScreenController(this)
        val linearLayoutManager = LinearLayoutManager(context)
        recycler_view.layoutManager = linearLayoutManager
        recycler_view.adapter = categoryScreenController.adapter
        searchView.showSearch(false)
        categoryScreenController.adapter.registerAdapterDataObserver(dataObserver)
    }

    override fun onDestroyView() {
        categoryScreenController.adapter.unregisterAdapterDataObserver(dataObserver)
        super.onDestroyView()
    }

    override fun loadIntent(): UserIntent {
        return CategoryScreenContract.Intent.Load
    }

    override fun userIntents(): Observable<UserIntent> {
        return Observable.mergeArray(

            searchQuerySubject
                .debounce(300, TimeUnit.MILLISECONDS)
                .map { CategoryScreenContract.Intent.SearchQuery(it) },

            onCategoryClickedSubject
                .throttleFirst(300, TimeUnit.MILLISECONDS)
                .map { CategoryScreenContract.Intent.SetCategory(it) },

            fab_add_category.clicks()
                .throttleFirst(300, TimeUnit.MILLISECONDS)
                .map {
                    getCurrentState().otherCategory?.id?.let {
                        CategoryScreenContract.Intent.SetEnteredCategoryName(
                            searchView.getQuery(),
                            getCurrentState().otherCategory?.id!!,
                            "FAB"
                        )
                    }
                },

            onOtherCategoryClickedSubject
                .throttleFirst(300, TimeUnit.MILLISECONDS)
                .map {
                    getCurrentState().otherCategory?.id?.let {
                        CategoryScreenContract.Intent.SetEnteredCategoryName(
                            searchView.getQuery(),
                            getCurrentState().otherCategory?.id!!,
                            "Add Category"
                        )
                    }
                }
        )
    }

    private val dataObserver by lazy {
        object : RecyclerView.AdapterDataObserver() {
            override fun onItemRangeInserted(positionStart: Int, itemCount: Int) {
                recycler_view.scrollToPosition(0)
            }
        }
    }

    override fun render(state: CategoryScreenContract.State) {
        val epoxyVisibilityTracker = EpoxyVisibilityTracker()
        epoxyVisibilityTracker.attach(recycler_view)

        categoryScreenController.setState(state)

        if (searchView.getQuery()
            .isNotEmpty() && (state.chunkedPopularCategories.isNotEmpty() || state.chunkedNonPopularCategories.isNotEmpty())
        ) {
            fab_add_category.show()
        } else {
            fab_add_category.hide()
        }

        if (state.categorySetLoader) {
            update_loading.visibility = View.VISIBLE
        } else {
            update_loading.visibility = View.GONE
        }

        searchView.setHint(getString(R.string.category_search_text))
        searchView.setIconsColor(Color.BLACK)

        toolbar_icon.setOnClickListener {
            searchView.animationDuration = 400
            searchView.showSearch(true)
            searchView.setQuery("", true)
            tracker.trackSearchCategory(PropertyValue.MERCHANT)
        }

        searchView.setOnQueryTextListener(object : SimpleSearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                return false
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                searchQuerySubject.onNext(newText ?: "")
                return false
            }

            override fun onQueryTextCleared(): Boolean {
                searchView.closeSearch(true)
                return false
            }
        })

        // show/hide alert
        if (state.networkError or state.error or state.isAlertVisible) {
            alert = when {
                state.networkError -> view?.snackbar(
                    getString(R.string.home_no_internet_msg),
                    Snackbar.LENGTH_INDEFINITE
                )
                state.isAlertVisible -> view?.snackbar(state.alertMessage, Snackbar.LENGTH_INDEFINITE)
                else -> view?.snackbar(getString(R.string.err_default), Snackbar.LENGTH_INDEFINITE)
            }
            alert?.show()
        } else {
            alert?.dismiss()
        }
    }

    /****************************************************************
     * Lifecycle methods
     ****************************************************************/

    override fun onBackPressed(): Boolean {
        if (KeyboardVisibilityEvent.isKeyboardVisible(activity)) {
            KeyboardVisibilityEvent.hideKeyboard(activity)
        }
        return false
    }

    /****************************************************************
     * Listeners (for child views)
     ****************************************************************/

    override fun clickedCategoryScreenView(category: Category) {
        onCategoryClickedSubject.onNext(category)
    }

    @UiThread
    override fun gotoLogin() {
        activity?.runOnUiThread {
            legacyNavigator.goToLoginScreenForAuthFailure(requireActivity())
        }
    }

    @UiThread
    override fun goBack() {
        activity?.runOnUiThread {
            if (KeyboardVisibilityEvent.isKeyboardVisible(activity)) {
                KeyboardVisibilityEvent.hideKeyboard(activity)
            }
            if (requireActivity().callingActivity != null) {
                requireActivity().setResult(Activity.RESULT_OK)
            }
            requireActivity().finish()
        }
    }

    override fun clickedAddOtherCategory() {
        onOtherCategoryClickedSubject.onNext(Unit)
    }
}
