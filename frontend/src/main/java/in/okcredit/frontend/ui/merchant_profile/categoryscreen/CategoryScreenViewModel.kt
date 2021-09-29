package `in`.okcredit.frontend.ui.merchant_profile.categoryscreen

import `in`.okcredit.analytics.PropertyValue
import `in`.okcredit.analytics.Tracker
import `in`.okcredit.frontend.R
import `in`.okcredit.frontend.usecase.merchant.GetCategoriesForCategoryScreen
import `in`.okcredit.merchant.contract.BusinessConstants
import `in`.okcredit.merchant.contract.Category
import `in`.okcredit.merchant.contract.GetActiveBusiness
import `in`.okcredit.merchant.contract.Request
import `in`.okcredit.merchant.contract.UpdateBusiness
import `in`.okcredit.shared.base.BasePresenter
import `in`.okcredit.shared.base.UiState
import `in`.okcredit.shared.usecase.Result
import `in`.okcredit.shared.usecase.UseCase
import android.content.Context
import io.reactivex.Observable
import io.reactivex.Observable.mergeArray
import io.reactivex.subjects.BehaviorSubject
import io.reactivex.subjects.PublishSubject
import java.util.concurrent.TimeUnit
import javax.inject.Inject

class CategoryScreenViewModel @Inject constructor(
    initialState: CategoryScreenContract.State,
    private val getCategoriesForCategoryScreen: GetCategoriesForCategoryScreen,
    private val updateBusiness: UpdateBusiness,
    private val tracker: Tracker,
    private val getActiveBusiness: GetActiveBusiness,
    private val context: Context,
    private val navigator: CategoryScreenContract.Navigator
) : BasePresenter<CategoryScreenContract.State, CategoryScreenContract.PartialState>(initialState) {

    private val onSearchPublicSubject = BehaviorSubject.createDefault("")
    private val showAlertPublicSubject = PublishSubject.create<String>()
    private var category: Category? = null
    private var searchQuery: String? = null
    private var selectedCategoryMethod: String? = null

    override fun handle(): Observable<UiState.Partial<CategoryScreenContract.State>> {
        return mergeArray(
            intent<CategoryScreenContract.Intent.Load>()
                .switchMap { onSearchPublicSubject }
                .switchMap {
                    searchQuery = it
                    getCategoriesForCategoryScreen.execute(it)
                }
                .map {
                    when (it) {
                        is Result.Progress -> CategoryScreenContract.PartialState.ShowLoading
                        is Result.Success -> {
                            CategoryScreenContract.PartialState.SetCategoriesData(
                                it.value.chunkedPopularCategories,
                                it.value.chunkedNonPopularCategories,
                                it.value.otherCategory
                            )
                        }
                        is Result.Failure -> {
                            when {
                                isAuthenticationIssue(it.error) -> {
                                    navigator.gotoLogin()
                                    CategoryScreenContract.PartialState.HideLoading
                                }
                                isInternetIssue(it.error) -> {
                                    CategoryScreenContract.PartialState.HideLoading
                                }
                                else -> {
                                    CategoryScreenContract.PartialState.ErrorState
                                }
                            }
                        }
                    }
                },

            intent<CategoryScreenContract.Intent.Load>()
                .switchMap {
                    UseCase.wrapObservable(getActiveBusiness.execute())
                }
                .map {
                    when (it) {
                        is Result.Progress -> CategoryScreenContract.PartialState.NoChange
                        is Result.Success -> {
                            CategoryScreenContract.PartialState.SetCurrentCategory(it.value.category)
                        }
                        is Result.Failure -> {
                            CategoryScreenContract.PartialState.NoChange
                        }
                    }
                },

            showAlertPublicSubject
                .switchMap {
                    Observable.timer(3, TimeUnit.SECONDS)
                        .map<CategoryScreenContract.PartialState> { CategoryScreenContract.PartialState.HideAlert }
                        .startWith(CategoryScreenContract.PartialState.ShowAlert(it))
                },

            // handle `show alert` intent
            intent<CategoryScreenContract.Intent.SearchQuery>()
                .map {
                    onSearchPublicSubject.onNext(it.query)
                    CategoryScreenContract.PartialState.UpdateSearchQuery(it.query)
                },

            intent<CategoryScreenContract.Intent.SetCategory>()
                .switchMap {
                    category = it.category
                    UseCase.wrapCompletable(
                        updateBusiness.execute(
                            Request(
                                inputType = BusinessConstants.CATEGORY,
                                category = it.category.id to it.category.name
                            )
                        )
                    )
                }
                .map {
                    when (it) {
                        is Result.Progress -> CategoryScreenContract.PartialState.SetCategoryLoaderStatus(true)
                        is Result.Success -> {
                            tracker.trackUpdateProfileLegacy(
                                relation = PropertyValue.MERCHANT,
                                field = PropertyValue.CATEGORY,
                                setValue = category?.name,
                                removed = false,
                                method = "Button",
                                type = if (category?.isPopular == true) "Popular" else "Others",
                                search = searchQuery.isNullOrBlank().not().toString(),
                                categoryId = category?.id
                            )
                            navigator.goBack()
                            CategoryScreenContract.PartialState.NoChange
                        }
                        is Result.Failure -> {
                            when {
                                isAuthenticationIssue(it.error) -> {
                                    navigator.gotoLogin()
                                }
                                isInternetIssue(it.error) -> {
                                    showAlertPublicSubject.onNext(context.getString(R.string.no_internet_msg))
                                }
                                else -> {
                                    showAlertPublicSubject.onNext(context.getString(R.string.err_default))
                                }
                            }
                            CategoryScreenContract.PartialState.SetCategoryLoaderStatus(false)
                        }
                    }
                },

            intent<CategoryScreenContract.Intent.SetEnteredCategoryName>()
                .switchMap {
                    selectedCategoryMethod = it.method
                    UseCase.wrapCompletable(
                        updateBusiness.execute(
                            Request(
                                inputType = BusinessConstants.CATEGORY,
                                category = it.otherCategoryId to it.query
                            )
                        )
                    )
                }
                .map {
                    when (it) {
                        is Result.Progress -> CategoryScreenContract.PartialState.SetCategoryLoaderStatus(true)
                        is Result.Success -> {
                            tracker.trackUpdateProfileLegacy(
                                relation = PropertyValue.MERCHANT,
                                field = PropertyValue.CATEGORY,
                                setValue = searchQuery,
                                method = selectedCategoryMethod,
                                type = "Typing",
                                search = searchQuery.isNullOrBlank().not().toString()
                            )
                            navigator.goBack()
                            CategoryScreenContract.PartialState.NoChange
                        }
                        is Result.Failure -> {
                            when {
                                isAuthenticationIssue(it.error) -> {
                                    navigator.gotoLogin()
                                }
                                isInternetIssue(it.error) -> {
                                    showAlertPublicSubject.onNext(context.getString(R.string.no_internet_msg))
                                }
                                else -> {
                                    showAlertPublicSubject.onNext(context.getString(R.string.err_default))
                                }
                            }
                            CategoryScreenContract.PartialState.SetCategoryLoaderStatus(false)
                        }
                    }
                }

        )
    }

    override fun reduce(
        currentState: CategoryScreenContract.State,
        partialState: CategoryScreenContract.PartialState
    ): CategoryScreenContract.State {
        return when (partialState) {
            is CategoryScreenContract.PartialState.ShowLoading -> currentState.copy(isLoading = true)
            is CategoryScreenContract.PartialState.HideLoading -> currentState.copy(isLoading = false)
            is CategoryScreenContract.PartialState.SetCategoryLoaderStatus -> currentState.copy(categorySetLoader = partialState.status)
            is CategoryScreenContract.PartialState.SetCurrentCategory -> currentState.copy(currentCategory = partialState.category)
            is CategoryScreenContract.PartialState.HideAlert -> currentState.copy(isAlertVisible = false)
            is CategoryScreenContract.PartialState.UpdateSearchQuery -> currentState.copy(searchQuery = partialState.query)
            is CategoryScreenContract.PartialState.ErrorState -> currentState.copy(
                isLoading = false,
                error = true,
                categorySetLoader = false
            )
            is CategoryScreenContract.PartialState.ShowAlert -> currentState.copy(
                isAlertVisible = true,
                alertMessage = partialState.message
            )
            is CategoryScreenContract.PartialState.SetNetworkError -> currentState.copy(
                networkError = partialState.networkError,
                isLoading = false
            )
            is CategoryScreenContract.PartialState.SetCategoriesData -> currentState.copy(
                chunkedPopularCategories = partialState.chunkedPopularCategories,
                chunkedNonPopularCategories = partialState.chunkedNonPopularCategories,
                otherCategory = partialState.otherCategory,
                isLoading = false
            )
            is CategoryScreenContract.PartialState.ClearNetworkError -> currentState.copy(networkError = false)
            is CategoryScreenContract.PartialState.NoChange -> currentState
        }
    }
}
