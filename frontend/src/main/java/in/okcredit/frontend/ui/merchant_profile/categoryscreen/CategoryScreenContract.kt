package `in`.okcredit.frontend.ui.merchant_profile.categoryscreen

import `in`.okcredit.merchant.contract.Category
import `in`.okcredit.shared.base.UiState
import `in`.okcredit.shared.base.UserIntent

// TODO:
// Analytics
// Other category API Testing
// Business Type API Testing
// Remove older code

interface CategoryScreenContract {

    companion object {
        const val SPAN_SIZE_CATEGORY = 4
    }

    data class State(
        val isLoading: Boolean = true,
        val categorySetLoader: Boolean = false,
        val currentCategory: Category? = null,
        val isAlertVisible: Boolean = false,
        val alertMessage: String = "",
        val searchQuery: String? = null,
        val error: Boolean = false,
        val otherCategory: Category? = null,
        val chunkedPopularCategories: List<List<Category>> = arrayListOf(),
        val chunkedNonPopularCategories: List<List<Category>> = arrayListOf(),
        val categories: List<Category> = arrayListOf(),
        val networkError: Boolean = false
    ) : UiState

    sealed class PartialState : UiState.Partial<State> {

        object ShowLoading : PartialState()

        object HideLoading : PartialState()

        data class SetCategoryLoaderStatus(val status: Boolean) : PartialState()

        object ErrorState : PartialState()

        data class ShowAlert(val message: String) : PartialState()

        object HideAlert : PartialState()

        object NoChange : PartialState()

        data class SetNetworkError(val networkError: Boolean) : PartialState()

        data class SetCategoriesData(
            val chunkedPopularCategories: List<List<Category>>,
            val chunkedNonPopularCategories: List<List<Category>>,
            val otherCategory: Category?
        ) : PartialState()

        data class SetCurrentCategory(val category: Category?) : PartialState()

        data class UpdateSearchQuery(val query: String?) : PartialState()

        object ClearNetworkError : PartialState()
    }

    sealed class Intent : UserIntent {
        // load screen
        object Load : Intent()

        data class ShowAlert(val message: String) : Intent()

        data class SearchQuery(val query: String) : Intent()

        data class SetCategory(val category: Category) : Intent()

        data class SetEnteredCategoryName(val query: String, val otherCategoryId: String, val method: String) : Intent()
    }

    interface Navigator {
        fun gotoLogin()

        fun goBack()
    }
}
