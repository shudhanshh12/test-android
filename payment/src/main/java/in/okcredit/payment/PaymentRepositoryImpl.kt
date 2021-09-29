package `in`.okcredit.payment

import `in`.okcredit.collection.contract.CollectionSyncer
import `in`.okcredit.payment.contract.model.PaymentModel
import `in`.okcredit.payment.server.PaymentServerImpl
import `in`.okcredit.payment.server.internal.PaymentApiMessages
import android.content.Context
import dagger.Lazy
import io.reactivex.Observable
import io.reactivex.Single
import tech.okcredit.android.base.error.Error.parse
import tech.okcredit.android.base.utils.ThreadUtils
import javax.inject.Inject

class PaymentRepositoryImpl @Inject constructor(
    private val context: Lazy<Context>,
    private val server: Lazy<PaymentServerImpl>,
    private val collectionSyncer: Lazy<CollectionSyncer>,
) : PaymentRepository {

    override fun getJuspayAttributes(
        juspayAttributeRequestBody: PaymentApiMessages.JuspayAttributeRequestBody,
        businessId: String,
    ): Single<PaymentApiMessages.GetJuspayAttributesResponse> {
        return server.get().getJuspayAttributes(juspayAttributeRequestBody, businessId)
    }

    override fun getPaymentAttributes(
        client: String,
        linkId: String,
        businessId: String,
    ): Single<PaymentApiMessages.GetPaymentAttributesResponse> {
        return server.get()
            .getPaymentAttributes(PaymentApiMessages.PaymentAttributeRequestBody(client), linkId, businessId)
    }

    override fun getJuspayPaymentPolling(
        pid: String,
        polling: Boolean,
        type: String,
        businessId: String
    ): Observable<PaymentModel.JuspayPaymentPollingModel> {
        return server.get().getJuspayPaymentPolling(pid, polling, type, businessId).toObservable()
    }

    override fun createPaymentDestination(
        paymentDestinationRequest: PaymentApiMessages.PaymentDestinationRequest,
        businessId: String
    ): Single<PaymentApiMessages.PaymentDestinationResponse> {
        return server.get().createPaymentDestination(paymentDestinationRequest, businessId)
            .subscribeOn(ThreadUtils.api())
            .observeOn(ThreadUtils.worker())
            .map {
                if (it.isSuccessful && it.body() != null) {
                    // to sync merchant profile in DB which currently resides in collection
                    collectionSyncer.get().scheduleCollectionProfile("payment_repo", businessId)
                    it.body()
                } else {
                    val errorBody = parse(it)
                    when (errorBody.code) {
                        500 -> {
                            throw Error(context.get().getString(R.string.payment_error_not_able_to_Add_details))
                        }
                        else -> {
                            throw Error(errorBody.error)
                        }
                    }
                }
            }
    }
}
