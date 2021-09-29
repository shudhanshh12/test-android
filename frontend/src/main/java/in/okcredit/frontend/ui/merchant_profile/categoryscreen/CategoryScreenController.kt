package `in`.okcredit.frontend.ui.merchant_profile.categoryscreen

import `in`.okcredit.frontend.BuildConfig
import `in`.okcredit.frontend.R
import `in`.okcredit.frontend.ui.merchant_profile.categoryscreen.CategoryScreenContract.Companion.SPAN_SIZE_CATEGORY
import `in`.okcredit.frontend.ui.merchant_profile.categoryscreen.views.CategoryFragmentViewModel_
import `in`.okcredit.frontend.ui.merchant_profile.categoryscreen.views.emptyCategoryView
import `in`.okcredit.frontend.ui.merchant_profile.categoryscreen.views.headerView
import com.airbnb.epoxy.AsyncEpoxyController
import com.airbnb.epoxy.carousel
import javax.inject.Inject

class CategoryScreenController @Inject
constructor(private val context: CategoryFragment) : AsyncEpoxyController() {
    private lateinit var state: CategoryScreenContract.State

    init {
        isDebugLoggingEnabled = BuildConfig.DEBUG
    }

    fun setState(state: CategoryScreenContract.State) {
        this.state = state
        requestModelBuild()
    }

    override fun buildModels() {

        if (state.isLoading.not() && state.chunkedPopularCategories.isEmpty() && state.chunkedNonPopularCategories.isEmpty()) {
            emptyCategoryView {
                id("emptyCategoryView")
                title(state.searchQuery ?: "")
                listener(context)
            }
        }

        if (state.chunkedPopularCategories.isNotEmpty()) {
            headerView {
                id("headerView_popular")
                title(context.getString(R.string.popular_text))
            }
        }

        state.chunkedPopularCategories.mapIndexed { index, list ->
            val categoryFragmentViewModels = arrayListOf<CategoryFragmentViewModel_>()
            list.map {
                categoryFragmentViewModels.add(
                    CategoryFragmentViewModel_()
                        .id(it.id)
                        .currentCategoryStatus(state.currentCategory?.id?.equals(it.id) ?: false)
                        .listener(context)
                        .category(it)
                )
            }
            carousel {
                id("carousel_popular$index")
                numViewsToShowOnScreen(SPAN_SIZE_CATEGORY.toFloat())
                models(categoryFragmentViewModels)
            }
        }

        if (state.chunkedPopularCategories.isNotEmpty()) {
            headerView {
                id("headerView_non_popular")
                title(context.getString(R.string.others))
            }
        }

        state.chunkedNonPopularCategories.mapIndexed { index, list ->
            val categoryScreenViewModels = arrayListOf<CategoryFragmentViewModel_>()
            list.map {
                categoryScreenViewModels.add(
                    CategoryFragmentViewModel_()
                        .id(it.id)
                        .currentCategoryStatus(state.currentCategory?.id?.equals(it.id) ?: false)
                        .listener(context)
                        .category(it)
                )
            }
            carousel {
                id("carousel_non_popular$index")
                numViewsToShowOnScreen(SPAN_SIZE_CATEGORY.toFloat())
                models(categoryScreenViewModels)
            }
        }
    }
}
