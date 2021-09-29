package `in`.okcredit.backend._offline.usecase

import `in`.okcredit.backend._offline.database.CustomerRepo
import `in`.okcredit.collection.contract.CollectionSyncer
import `in`.okcredit.merchant.contract.GetActiveBusiness
import `in`.okcredit.merchant.suppliercredit.Supplier
import `in`.okcredit.merchant.suppliercredit.SupplierCreditRepository
import `in`.okcredit.merchant.suppliercredit.server.internal.common.SupplierCreditServerErrors
import androidx.room.EmptyResultSetException
import com.google.common.base.Strings
import io.reactivex.Single
import java.util.*
import javax.inject.Inject

// create supplier
class AddSupplier @Inject constructor(
    private val supplierCreditRepository: dagger.Lazy<SupplierCreditRepository>,
    private val customerRepo: dagger.Lazy<CustomerRepo>,
    private val getActiveBusiness: dagger.Lazy<GetActiveBusiness>,
    private val collectionSyncer: dagger.Lazy<CollectionSyncer>,
) {

    fun execute(req: Request): Single<Supplier> {
        return getBusiness().flatMap { business ->
            Single.zip(
                validateName(req.name),
                validateMobile(req.mobile, business.id),
                validateCyclicAccount(req.mobile, business.id),
                { name, mobile, _ -> Response(name, mobile, business.id) }
            ).flatMap {
                supplierCreditRepository.get().addSupplier(it.name, it.mobile, req.profileImage, it.businessId)
            }.flatMap {
                collectionSyncer.get().scheduleCollectionProfile(CollectionSyncer.Source.ADD_SUPPLIER, business.id)
                Single.just(it)
            }
        }
    }

    data class Response(
        val name: String,
        val mobile: String,
        val businessId: String
    )

    data class Request(
        val name: String,
        val mobile: String?,
        val profileImage: String?,
    )

    private fun getBusiness() = getActiveBusiness.get().execute().firstOrError()

    private fun validateName(desc: String?): Single<String> {
        return Single.just(desc)
    }

    // if this mobile number is already added as supplier , we throw MobileConflict error
    private fun validateMobile(mobile: String?, businessId: String): Single<String> {
        return if (Strings.isNullOrEmpty(mobile) || mobile?.length != 10) {
            Single.just("")
        } else {
            supplierCreditRepository.get().getSupplierByMobile(mobile, businessId)
                .flatMap {
                    if (!it.deleted) { // active
                        return@flatMap Single.error<String>(SupplierCreditServerErrors.MobileConflict(it))
                    } else {
                        return@flatMap Single.just(mobile)
                    }
                }
                .onErrorResumeNext {
                    when (it) {
                        is EmptyResultSetException -> Single.just(mobile)
                        else -> Single.error(it)
                    }
                }
        }
    }

    // if , merchant trying to added this mobile number as supplier , but this mobile number is already added as
    // customer , then we throw Cyclic Account error
    private fun validateCyclicAccount(mobile: String?, businessId: String): Single<String> {
        return if (Strings.isNullOrEmpty(mobile) || mobile?.length != 10) {
            Single.just("")
        } else {
            customerRepo.get().findCustomerByMobile(mobile, businessId).flatMap { customer ->
                return@flatMap getActiveBusiness.get().execute().firstOrError()
                    .flatMap { business ->

                        if (business.mobile == mobile) { // cyclic account is not considered for merchant
                            Single.just(mobile)
                        } else {
                            if (customer.status == 1) { // customer is active(not deleted)
                                return@flatMap Single.error<String>(
                                    SupplierCreditServerErrors.ActiveCyclicAccount(
                                        SupplierCreditServerErrors.Error(
                                            customer.id,
                                            customer.description,
                                            customer.mobile,
                                            customer.profileImage
                                        )
                                    )
                                )
                            } else {
                                return@flatMap Single.error<String>(
                                    SupplierCreditServerErrors.DeletedCyclicAccount(
                                        SupplierCreditServerErrors.Error(
                                            customer.id,
                                            customer.description,
                                            customer.mobile,
                                            customer.profileImage
                                        )
                                    )
                                )
                            }
                        }
                    }
            }.onErrorResumeNext {
                when (it) {
                    is NoSuchElementException -> Single.just(mobile)
                    else -> Single.error(it)
                }
            }
        }
    }
}
