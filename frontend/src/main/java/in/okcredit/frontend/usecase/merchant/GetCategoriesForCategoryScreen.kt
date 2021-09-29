package `in`.okcredit.frontend.usecase.merchant

import `in`.okcredit.frontend.ui.merchant_profile.categoryscreen.CategoryScreenContract.Companion.SPAN_SIZE_CATEGORY
import `in`.okcredit.merchant.contract.Business
import `in`.okcredit.merchant.contract.BusinessRepository
import `in`.okcredit.merchant.contract.Category
import `in`.okcredit.merchant.contract.CategoryTypes
import `in`.okcredit.merchant.contract.GetActiveBusiness
import `in`.okcredit.shared.usecase.Result
import `in`.okcredit.shared.usecase.UseCase
import dagger.Lazy
import io.reactivex.Observable
import java.util.*
import javax.inject.Inject

class GetCategoriesForCategoryScreen @Inject constructor(
    private val businessRepository: Lazy<BusinessRepository>,
    private val getActiveBusiness: Lazy<GetActiveBusiness>
) :
    UseCase<String, GetCategoriesForCategoryScreen.Response> {
    override fun execute(req: String): Observable<Result<Response>> {

        val observables = listOf(
            getActiveBusiness.get().execute(),
            businessRepository.get().getCategories()
        )
        return UseCase.wrapObservable(
            Observable.combineLatest(observables) {
                val merchant = it[0] as Business
                val categories = it[1] as List<Category>

                val modifiedCategoryList = mutableListOf<Category>()
                var otherCategory: Category? = null

                categories.map {
                    if (it.id.equals(merchant.category?.id) && it.type != CategoryTypes.OTHER.value) {
                        modifiedCategoryList.add(it)
                    }

                    if (it.type == CategoryTypes.OTHER.value) {
                        otherCategory = it
                    }
                }

                categories.map {
                    if (it.id.equals(merchant.category?.id).not() && it.type != CategoryTypes.OTHER.value) {
                        modifiedCategoryList.add(it)
                    }
                }

                val chunkedPopularCategories =
                    modifiedCategoryList
                        .filter { it.isPopular }
                        .filter { searchPredicate(it.name, req) }
                        .chunked(SPAN_SIZE_CATEGORY)

                val chunkedNonPopularCategories =
                    modifiedCategoryList
                        .filter { it.isPopular.not() }
                        .filter { searchPredicate(it.name, req) }
                        .chunked(SPAN_SIZE_CATEGORY)

                return@combineLatest Response(
                    categories,
                    otherCategory,
                    chunkedPopularCategories,
                    chunkedNonPopularCategories
                )
            }
        )
    }

    companion object {
        fun searchPredicate(content: String?, searchQuery: String?): Boolean {
            return when {
                content.isNullOrEmpty() -> {
                    true
                }
                searchQuery.isNullOrEmpty() -> {
                    true
                }
                else -> {
                    content.toLowerCase().replace(" ", "")?.contains(
                        searchQuery.toLowerCase().replace(" ", "")
                    )
                }
            }
        }
    }

    data class Response(
        val categories: List<Category>,
        val otherCategory: Category?,
        val chunkedPopularCategories: List<List<Category>>,
        val chunkedNonPopularCategories: List<List<Category>>
    )
}
