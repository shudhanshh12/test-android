package `in`.okcredit.payment.server

import `in`.okcredit.payment.contract.model.JuspayPollingStatus
import `in`.okcredit.payment.contract.model.PaymentModel
import `in`.okcredit.payment.server.internal.PaymentApiClient
import `in`.okcredit.payment.server.internal.PaymentApiMessages
import `in`.okcredit.payment.utils.PaymentModuleMapper
import dagger.Lazy
import io.reactivex.Flowable
import io.reactivex.Single
import retrofit2.Response
import tech.okcredit.android.base.error.Error
import tech.okcredit.android.base.utils.ThreadUtils
import tech.okcredit.base.network.asError
import java.util.concurrent.TimeUnit
import javax.inject.Inject

class PaymentServerImpl @Inject constructor(
    private val apiClient: Lazy<PaymentApiClient>
) {
    fun getJuspayAttributes(
        juspayAttributeRequestBody: PaymentApiMessages.JuspayAttributeRequestBody,
        businessId: String
    ): Single<PaymentApiMessages.GetJuspayAttributesResponse> {
        return apiClient.get().getJuspayAttributes(juspayAttributeRequestBody, businessId)
            .subscribeOn(ThreadUtils.api())
            .observeOn(ThreadUtils.worker())
            .map {
                if (it.isSuccessful && it.body() != null) {
                    it.body()
                } else {
                    val error = Error.parse(it)
                    throw error
                }
            }
    }

    fun getPaymentAttributes(
        requestBody: PaymentApiMessages.PaymentAttributeRequestBody,
        linkId: String,
        businessId: String
    ): Single<PaymentApiMessages.GetPaymentAttributesResponse> {
        return apiClient.get().getPaymentAttributes(requestBody, linkId, businessId)
            .subscribeOn(ThreadUtils.api())
            .observeOn(ThreadUtils.worker())
            .map {
                if (it.isSuccessful && it.body() != null) {
                    it.body()
                } else {
                    val error = Error.parse(it)
                    throw error
                }
            }
    }

    fun getJuspayPaymentPolling(
        pid: String,
        polling: Boolean,
        type: String,
        businessId: String
    ): Flowable<PaymentModel.JuspayPaymentPollingModel> {
        var totalTime = 0L
        val maxLimit = 13L
        val delayTime = 2L
        return apiClient.get().juspayPaymentPolling(payment_id = pid, polling = polling, type = type, businessId)
            .repeatWhen {
                it.delay(delayTime, TimeUnit.SECONDS)
            }
            .takeUntil {
                totalTime += delayTime
                it.body() != null && it.body()!!.status != JuspayPollingStatus.PENDING.value || totalTime > maxLimit
            }
            .filter {
                // when totalTime > maxLimit is true  for takeuntil call,  we need to pass last pending response to usecase
                // so totalTime > maxLimit.minus(delayTime) wil use
                it.body() != null && it.body()!!.status != JuspayPollingStatus.PENDING.value || totalTime > maxLimit.minus(
                    delayTime
                )
            }
            .subscribeOn(ThreadUtils.api())
            .observeOn(ThreadUtils.worker())
            .map {
                if (it.isSuccessful && it.body() != null) {
                    it.body()
                } else {
                    val error = it.asError()
                    throw error
                }
            }.map {
                PaymentModuleMapper.toPaymentResponseModel(it)
            }
    }

    fun createPaymentDestination(
        paymentDestinationRequest: PaymentApiMessages.PaymentDestinationRequest,
        businessId: String
    ): Single<Response<PaymentApiMessages.PaymentDestinationResponse>> {
        return apiClient.get()
            .createPaymentDestination(paymentDestinationRequest.destinationId, paymentDestinationRequest, businessId)
    }
}
