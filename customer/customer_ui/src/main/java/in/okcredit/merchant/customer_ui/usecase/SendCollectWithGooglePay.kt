package `in`.okcredit.merchant.customer_ui.usecase

import `in`.okcredit.collection.contract.CollectionRepository
import `in`.okcredit.merchant.contract.GetActiveBusiness
import `in`.okcredit.merchant.customer_ui.data.CustomerRepositoryImpl
import `in`.okcredit.payment.contract.usecase.GetPaymentAttributeFromServer
import dagger.Lazy
import io.reactivex.Completable
import kotlinx.coroutines.rx2.rxCompletable
import tech.okcredit.android.base.extensions.isNotNullOrBlank
import javax.inject.Inject

class SendCollectWithGooglePay @Inject constructor(
    private val customerRepository: Lazy<CustomerRepositoryImpl>,
    private val collectionRepository: Lazy<CollectionRepository>,
    private val getPaymentAttributeFromServer: Lazy<GetPaymentAttributeFromServer>,
    private val getActiveBusiness: Lazy<GetActiveBusiness>
) {

    fun execute(customerId: String, mobile: String, amount: Long): Completable {
        return getActiveBusiness.get().execute().firstOrError().flatMapCompletable { business ->
            collectionRepository.get().getCollectionCustomerProfile(customerId, business.id).firstOrError()
                .flatMapCompletable { customerProfile ->
                    return@flatMapCompletable if (customerProfile.linkId.isNotNullOrBlank()) {
                        getPaymentAttributeFromServer.get().execute("APP", customerProfile.linkId ?: "")
                            .flatMapCompletable {
                                rxCompletable {
                                    customerRepository.get().initiateGooglePayPayment(
                                        amount = amount,
                                        mobile = mobile,
                                        transactionId = it.paymentId,
                                        linkId = customerProfile.linkId ?: "",
                                        customerId = customerId,
                                        businessName = business.name,
                                        businessId = business.id,
                                    )
                                }.doOnComplete {
                                    collectionRepository.get().updateGooglePayEnabledForCustomer(customerId, false)
                                        .subscribe()
                                }
                            }
                    } else {
                        Completable.error(IllegalArgumentException("link_id not found for the customer"))
                    }
                }
        }
    }
}
