package `in`.okcredit.collection_ui.usecase

import `in`.okcredit.collection.contract.CollectionRepository
import `in`.okcredit.merchant.contract.GetActiveBusinessId
import dagger.Lazy
import io.reactivex.Single
import javax.inject.Inject

class ValidatePaymentAddress @Inject constructor(
    private val collectionRepository: CollectionRepository,
    private val getActiveBusinessId: Lazy<GetActiveBusinessId>,
) {

    fun execute(paymentAddress: String, paymentAddressType: String, isUpdate: Boolean): Single<Response> {
        return getActiveBusinessId.get().execute().flatMap { businessId ->
            collectionRepository.validatePaymentAddress(paymentAddressType, paymentAddress, businessId)
                .map {
                    return@map Response(it.first, it.second, paymentAddressType, isUpdate)
                }
        }
    }

    data class Response(
        val isValidPaymentAddress: Boolean,
        val paymentAccountName: String,
        val paymentAddressType: String,
        val isUpdate: Boolean,
    )
}
