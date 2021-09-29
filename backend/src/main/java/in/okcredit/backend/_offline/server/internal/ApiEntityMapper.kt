package `in`.okcredit.backend._offline.server.internal

import `in`.okcredit.backend.contract.Customer
import com.google.common.base.Converter
import org.joda.time.DateTime

object ApiEntityMapper {

    @JvmField
    val CUSTOMER: Converter<`in`.okcredit.backend._offline.server.internal.Customer, Customer> =
        object : Converter<`in`.okcredit.backend._offline.server.internal.Customer, Customer>() {
            override fun doForward(apiEntity: `in`.okcredit.backend._offline.server.internal.Customer): Customer {
                var balanceV2 = apiEntity.balanceV2
                if (apiEntity.balance != 0f && apiEntity.balanceV2 == 0L) {
                    balanceV2 = (apiEntity.balance * 100).toLong()
                }
                return Customer(
                    id = apiEntity.id,
                    status = apiEntity.status,
                    mobile = apiEntity.mobile,
                    description = apiEntity.description,
                    createdAt = apiEntity.createdAt,
                    txnStartTime = apiEntity.txnStartTime,
                    balanceV2 = balanceV2,
                    transactionCount = apiEntity.transactionCount,
                    lastActivity = apiEntity.lastActivity,
                    lastPayment = apiEntity.lastPayment,
                    accountUrl = apiEntity.accountUrl,
                    profileImage = apiEntity.profileImage,
                    address = apiEntity.address,
                    email = apiEntity.email,
                    newActivityCount = 0,
                    lastViewTime = null,
                    registered = apiEntity.registered,
                    lastBillDate = null,
                    txnAlertEnabled = apiEntity.txnAlertEnabled,
                    lang = apiEntity.lang,
                    reminderMode = apiEntity.reminderMode,
                    isLiveSales = apiEntity.isLiveSales,
                    addTransactionPermissionDenied = apiEntity.addTransactionRestricted,
                    state = if (apiEntity.state == Customer.State.BLOCKED.value) Customer.State.BLOCKED else Customer.State.ACTIVE,
                    blockedByCustomer = apiEntity.blockedByCustomer,
                    restrictContactSync = apiEntity.restrictContactSync,
                    lastReminderSendTime = apiEntity.lastReminderSendTime ?: DateTime(0),
                )
            }

            override fun doBackward(customer: Customer): `in`.okcredit.backend._offline.server.internal.Customer {
                throw RuntimeException("illegal operation: cannot convert customer domain entity to api entity")
            }
        }
}
