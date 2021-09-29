package `in`.okcredit.merchant.customer_ui.usecase

import `in`.okcredit.accounting_core.contract.QuickAddCustomerModel
import `in`.okcredit.backend._offline.database.CustomerRepo
import `in`.okcredit.merchant.contract.GetActiveBusinessId
import `in`.okcredit.merchant.suppliercredit.SupplierCreditRepository
import androidx.room.EmptyResultSetException
import dagger.Lazy
import io.reactivex.Single
import tech.okcredit.android.base.crashlytics.RecordException
import javax.inject.Inject

class GetQuickAddCustomerModel @Inject constructor(
    private val customerRepo: Lazy<CustomerRepo>,
    private val supplierCreditRepository: Lazy<SupplierCreditRepository>,
    private val getActiveBusinessId: Lazy<GetActiveBusinessId>,
) {

    fun execute(name: String, mobile: String, profileImage: String? = null): Single<QuickAddCustomerModel> {
        return getActiveBusinessId.get().execute().flatMap { businessId ->
            if (mobile.isBlank()) {
                Single.just(
                    QuickAddCustomerModel(
                        name,
                        mobile,
                        profileImage
                    )
                )
            } else {
                resolveExistingCustomer(name, mobile, profileImage, businessId)
            }
        }
    }

    private fun resolveExistingCustomer(
        name: String,
        mobile: String,
        profileImage: String? = null,
        businessId: String
    ): Single<QuickAddCustomerModel> {
        return customerRepo.get().findCustomerByMobile(mobile, businessId)
            .map {
                if (it.isActive()) {
                    QuickAddCustomerModel(
                        name = it.description,
                        mobile = it.mobile,
                        customerId = it.id
                    )
                } else {
                    QuickAddCustomerModel(
                        // Use the new name
                        name = name,
                        mobile = it.mobile,
                        customerId = it.id,
                        shouldReactivate = true
                    )
                }
            }
            .onErrorResumeNext {
                RecordException.recordException(it)
                if (it is NoSuchElementException) {
                    resolveExistingSupplier(name, mobile, profileImage, businessId)
                } else {
                    throw it
                }
            }
    }

    private fun resolveExistingSupplier(
        name: String,
        mobile: String,
        profileImage: String? = null,
        businessId: String
    ): Single<QuickAddCustomerModel> {
        return supplierCreditRepository.get().getSupplierByMobile(mobile, businessId)
            .flatMap { supplier ->
                if (supplier.deleted) {
                    Single.just(
                        // Use the new name
                        QuickAddCustomerModel(
                            name,
                            supplier.mobile,
                            supplierId = supplier.id,
                            shouldReactivate = true
                        )
                    )
                } else {
                    Single.just(
                        QuickAddCustomerModel(
                            supplier.name,
                            supplier.mobile,
                            supplierId = supplier.id
                        )
                    )
                }
            }.onErrorResumeNext {
                RecordException.recordException(it)
                if (it is EmptyResultSetException) {
                    Single.just(
                        QuickAddCustomerModel(
                            name,
                            mobile,
                            profileImage
                        )
                    )
                } else {
                    Single.error(it)
                }
            }
    }
}
