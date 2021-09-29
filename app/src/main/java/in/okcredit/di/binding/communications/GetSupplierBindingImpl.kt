package `in`.okcredit.di.binding.communications

import `in`.okcredit.merchant.suppliercredit.Supplier
import `in`.okcredit.supplier.usecase.GetSupplier
import dagger.Lazy
import io.reactivex.Single
import tech.okcredit.android.communication.GetSupplierBinding
import tech.okcredit.android.communication.NotificationPersonProfile
import javax.inject.Inject

class GetSupplierBindingImpl @Inject constructor(
    private val getSupplier: Lazy<GetSupplier>,
) :
    GetSupplierBinding {
    override fun getSupplierNameAndImage(supplierId: String): Single<NotificationPersonProfile> {
        return getSupplier.get().executeObservable(supplierId)
            .firstOrError()
            .flatMap { supplier: Supplier ->
                Single.just<NotificationPersonProfile?>(
                    NotificationPersonProfile(supplier.id, supplier.name, supplier.profileImage)
                )
            }
    }
}
