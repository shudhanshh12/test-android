package `in`.okcredit.merchant.contract

import io.reactivex.Completable

interface UpdateBusiness {
    fun execute(req: Request): Completable
}

data class Request(
    val inputType: Int,
    val updatedValue: String? = null,
    val address: Triple<String, Double, Double>? = null,
    val category: Pair<String?, String?>? = null,
    val businessType: BusinessType? = null
)
