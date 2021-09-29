package `in`.okcredit.di.binding.communications

import `in`.okcredit.backend.contract.Customer
import `in`.okcredit.backend.contract.GetCustomer
import dagger.Lazy
import io.reactivex.Single
import tech.okcredit.android.communication.GetCustomerBinding
import tech.okcredit.android.communication.NotificationPersonProfile
import javax.inject.Inject

class GetCustomerBindingImpl @Inject constructor(private val getCustomer: Lazy<GetCustomer>) : GetCustomerBinding {
    override fun getCustomerNameAndImage(customerId: String): Single<NotificationPersonProfile> {
        return getCustomer.get().execute(customerId)
            .firstOrError()
            .flatMap { cus: Customer ->
                Single.just<NotificationPersonProfile?>(
                    NotificationPersonProfile(cus.id, cus.description, cus.profileImage)
                )
            }
    }
}
