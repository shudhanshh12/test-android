package `in`.okcredit.merchant.server

import `in`.okcredit.merchant.contract.Business
import `in`.okcredit.merchant.contract.BusinessErrors
import `in`.okcredit.merchant.contract.GetActiveBusinessId
import `in`.okcredit.merchant.contract.NumberCheckResponse
import `in`.okcredit.merchant.server.internal.ApiEntityMapper
import `in`.okcredit.merchant.server.internal.ApiMessages
import `in`.okcredit.merchant.server.internal.IdentityApiClient
import `in`.okcredit.merchant.server.internal.MerchantApiClient
import `in`.okcredit.merchant.server.internal.MerchantAuiClient
import `in`.okcredit.merchant.store.database.BusinessCategory
import `in`.okcredit.merchant.store.database.DbEntityMapper.toBusiness
import `in`.okcredit.merchant.utils.Utils
import dagger.Lazy
import io.reactivex.Completable
import io.reactivex.Single
import kotlinx.coroutines.rx2.await
import org.jetbrains.annotations.NonNls
import tech.okcredit.android.base.crashlytics.RecordException
import tech.okcredit.android.base.utils.ThreadUtils
import tech.okcredit.base.network.asError
import timber.log.Timber
import javax.inject.Inject
import `in`.okcredit.merchant.store.database.Business as DbBusiness
import `in`.okcredit.merchant.store.database.BusinessType as DbBusinessType

class BusinessRemoteServerImpl @Inject constructor(
    private val merchantApiClient: Lazy<MerchantApiClient>,
    private val identityApiClient: Lazy<IdentityApiClient>,
    private val merchantAuiClient: Lazy<MerchantAuiClient>,
    // Remote source should not depend on usecase
    private val getActiveBusinessId: Lazy<GetActiveBusinessId>,
) : BusinessRemoteServer {

    companion object {

        @NonNls
        const val EMAIL_EXISTS = "email_exists"

        @NonNls
        const val INVALID_MOBILE = "invalid_mobile"

        @NonNls
        const val BUSINESS_LIMIT_REACHED = "business_limit_reached"
    }

    override fun checkNewNumber(mobile: String, businessId: String): Single<NumberCheckResponse> {
        return identityApiClient.get().checkNewNumber(mobile, businessId)
            .subscribeOn(ThreadUtils.api())
            .observeOn(ThreadUtils.worker())
            .flatMap {
                if (it.isSuccessful)
                    return@flatMap Single.just(it.body())
                else throw it.asError()
            }
    }

    override fun getBusiness(businessId: String): Single<DbBusiness> {
        return identityApiClient.get().getBusiness(ApiMessages.GetBusinessRequest(businessId), businessId)
            .subscribeOn(ThreadUtils.api())
            .observeOn(ThreadUtils.worker())
            .map {
                if (it.isSuccessful) {
                    return@map it.body()
                } else {
                    throw it.asError()
                }
            }
            .doOnError {
                RecordException.recordException(it)
                Timber.i(it)
            }
            .map { ApiEntityMapper.BUSINESS_RESPONSE.convert(it) }
    }

    override fun getCategories(businessId: String): Single<List<BusinessCategory>> {
        return merchantApiClient.get().getCategory(businessId)
            .subscribeOn(ThreadUtils.api())
            .observeOn(ThreadUtils.worker())
            .map {
                if (it.isSuccessful) {
                    return@map it.body()?.categories
                } else {
                    throw it.asError()
                }
            }
            .map { Utils.mapList(it, ApiEntityMapper.CATEGORY) }
            .doOnError {
                RecordException.recordException(it)
                Timber.i(it)
            }
    }

    override fun getBusinessTypes(businessId: String): Single<List<DbBusinessType>> {
        return merchantApiClient.get().getBusinessTypes(businessId)
            .subscribeOn(ThreadUtils.api())
            .observeOn(ThreadUtils.worker())
            .map {
                if (it.isSuccessful) {
                    return@map it.body()?.business_type
                } else {
                    throw it.asError()
                }
            }
            .map { Utils.mapList(it, ApiEntityMapper.BUSINESS) }
            .doOnError {
                RecordException.recordException(it)
                Timber.i(it)
            }
    }

    override fun updateBusiness(request: ApiMessages.UpdateBusinessRequest, businessId: String): Completable {
        return identityApiClient.get().updateBusiness(request, businessId)
            .subscribeOn(ThreadUtils.api())
            .observeOn(ThreadUtils.worker())
            .flatMapCompletable { response ->
                if (response.isSuccessful) {
                    return@flatMapCompletable Completable.complete()
                } else {
                    val error = response.asError()
                    when (error.code) {
                        400 -> {

                            if (error.message == INVALID_MOBILE) {
                                Completable.error(BusinessErrors.MerchantExists())
                            } else {
                                Completable.error(BusinessErrors.InvalidName())
                            }
                        }
                        403 -> Completable.error(BusinessErrors.NameChangeLimitExceeded())
                        409 -> {
                            if (error.error == EMAIL_EXISTS) {
                                Completable.error(BusinessErrors.EmailAlreadyExist())
                            } else {
                                Completable.error(BusinessErrors.MerchantExists())
                            }
                        }
                        else ->
                            Completable.error(error)
                    }
                }
            }
    }

    override suspend fun isMerchantActivated(): ApiMessages.IsMerchantActivated =
        getActiveBusinessId.get().execute().await().let {
            merchantAuiClient.get().isMerchantActivated(ApiMessages.IsMerchantActivatedApiRequest(it), it)
        }

    override suspend fun createBusiness(name: String, businessId: String): Business {
        val response = identityApiClient.get().createBusiness(ApiMessages.CreateBusinessRequest(name), businessId)
        return if (response.isSuccessful) {
            ApiEntityMapper.BUSINESS_RESPONSE.convert(response.body())!!.toBusiness()
        } else {
            throw response.asError()
        }
    }
}
