package `in`.okcredit.backend._offline.usecase

import `in`.okcredit.backend._offline.database.CustomerRepo
import `in`.okcredit.merchant.contract.GetActiveBusiness
import `in`.okcredit.merchant.contract.GetActiveBusinessId
import `in`.okcredit.merchant.suppliercredit.Supplier
import `in`.okcredit.merchant.suppliercredit.SupplierCreditRepository
import `in`.okcredit.merchant.suppliercredit.server.internal.common.SupplierCreditServerErrors
import androidx.room.EmptyResultSetException
import com.google.common.base.Strings
import dagger.Lazy
import io.reactivex.Completable
import io.reactivex.Single
import io.reactivex.functions.Function3
import org.joda.time.DateTime
import timber.log.Timber
import java.util.*
import javax.inject.Inject

class UpdateSupplier @Inject constructor(
    private val supplierCreditRepository: SupplierCreditRepository,
    private val customerRepo: CustomerRepo,
    private val getActiveBusiness: GetActiveBusiness,
    private val getActiveBusinessId: Lazy<GetActiveBusinessId>
) {

    // isMobileUpdate -> helps to know whether user is updating mobile number
    // if updating mobile number , then we have to check for cyclic error
    private var isMobileUpdate = false

    fun execute(supplierId: String, state: Int, updateState: Boolean): Completable {
        return getActiveBusinessId.get().execute().flatMapCompletable { businessId ->
            supplierCreditRepository.getSupplier(supplierId, businessId)
                .firstOrError()
                .flatMapCompletable { supplier ->
                    execute(
                        supplierId,
                        supplier.name,
                        supplier.mobile,
                        supplier.address,
                        supplier.profileImage,
                        supplier.txnAlertEnabled,
                        supplier.lang,
                        supplier.registered,
                        supplier.deleted,
                        supplier.createTime,
                        supplier.balance,
                        false,
                        false,
                        state,
                        updateState,
                        supplier.restrictContactSync
                    )
                }
        }
    }

    fun updateMoblie(supplierId: String, updatedMobile: String): Completable {
        return getActiveBusinessId.get().execute().flatMapCompletable { businessId ->
            supplierCreditRepository.getSupplier(supplierId, businessId)
                .firstOrError()
                .flatMapCompletable { supplier ->
                    execute(
                        supplier.id, supplier.name,
                        updatedMobile, supplier.address,
                        null, supplier.txnAlertEnabled,
                        supplier.lang, supplier.registered, supplier.deleted, supplier.createTime,
                        supplier.balance, false, true, Supplier.ACTIVE, false, supplier.restrictContactSync
                    )
                }
        }
    }

    fun execute(
        supplierId: String,
        name: String,
        mobile: String?,
        address: String?,
        profileImage: String?,
        txnAlertEnabled: Boolean,
        lang: String?,
        registered: Boolean,
        deleted: Boolean,
        createdTime: DateTime,
        balance: Long,
        txnAlertChanged: Boolean,
        isMobileUpdate: Boolean,
        state: Int,
        updateState: Boolean,
        restrictContactSync: Boolean
    ): Completable {

        this.isMobileUpdate = isMobileUpdate

        val supplier = Supplier(
            id = supplierId,
            registered = registered,
            deleted = deleted,
            createTime = createdTime,
            txnStartTime = DateTime.now().millis,
            name = name,
            mobile = mobile,
            address = address,
            profileImage = profileImage,
            balance = balance,
            txnAlertEnabled = txnAlertEnabled,
            lang = lang,
            state = state,
            blockedBySupplier = false,
            restrictContactSync = restrictContactSync
        )

        return getActiveBusinessId.get().execute().flatMapCompletable { businessId ->
            Single.zip(
                validateName(supplier.name),
                validateMobile(supplier.mobile, supplier.id, businessId),
                validateCyclicAccount(supplier.mobile, businessId),
                Function3<String, String, String, Pair<String, String>> { name, mobile, mobile1 -> name to mobile }
            )
                .flatMapCompletable {
                    supplierCreditRepository
                        .updateSuppler(supplier, txnAlertChanged, state, updateState, businessId)
                        .doOnComplete { Timber.i(">>Completable.complete 2") }
                }
        }
    }

    private fun validateName(desc: String?): Single<String> {
//        return if (desc == null || desc.isEmpty() || desc.length > 30) {
//            Single.error<String>(SupplierCreditServerErrors.InvalidName())
//        } else Single.just(desc)

        return Single.just(desc)
    }

    // if this mobile number is already added as supplier , we throw MobileConflict error
    private fun validateMobile(mobile: String?, supplierId: String, businessId: String): Single<String> {
        return if (Strings.isNullOrEmpty(mobile) || mobile?.length != 10) {
            Single.just("")
        } else {
            supplierCreditRepository.getSupplierByMobile(mobile, businessId)
                .flatMap {

                    if (it.id == supplierId) {
                        return@flatMap Single.just("")
                    } else {
                        return@flatMap Single.error<String>(SupplierCreditServerErrors.MobileConflict(it))
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

    // if , merchant trying to added this mobile number as supplier , but this mobile number is already added as customer , then
    // 1. if existing customer is deleted :-> we throw DeletedCyclicAccount and take user to customer transaction screen and un-delete this customer
    // 2. if existing customer is not deleted :-> we throw ActiveCyclicAccount and take user to customer transaction screen
    private fun validateCyclicAccount(mobile: String?, businessId: String): Single<String> {
        return if (Strings.isNullOrEmpty(mobile) || mobile?.length != 10) {
            Single.just("")
        } else {

            customerRepo.findCustomerByMobile(mobile, businessId)
                .flatMap { customer ->

                    return@flatMap getActiveBusiness.execute().firstOrError()
                        .flatMap { business ->

                            // 1. cyclic account is not considered for merchant (current device user)
                            // 2. cyclic account is not considered if not updating mobile number
                            if (business.mobile == mobile || !isMobileUpdate) {
                                Single.just(mobile)
                            } else {

                                if (customer.status == 1) {
                                    return@flatMap Single.error<String>(
                                        SupplierCreditServerErrors.ActiveCyclicAccount(
                                            SupplierCreditServerErrors.Error(
                                                customer.id,
                                                customer.description,
                                                customer.mobile,
                                                null
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
                                                null
                                            )
                                        )
                                    )
                                }
                            }
                        }
                }
                .onErrorResumeNext {
                    when (it) {
                        is NoSuchElementException -> Single.just(mobile)
                        else -> Single.error(it)
                    }
                }
        }
    }
}
