package `in`.okcredit.backend._offline.common

import `in`.okcredit.backend._offline.model.DueInfo
import `in`.okcredit.merchant.core.model.Customer
import merchant.okcredit.accounting.model.TransactionImage
import org.joda.time.DateTime

object CoreModuleMapper {

    @JvmStatic
    fun toCustomer(customer: Customer): `in`.okcredit.backend.contract.Customer {
        customer.run {
            return `in`.okcredit.backend.contract.Customer(
                id = this.id,
                customerSyncStatus = this.customerSyncStatus,
                status = this.status,
                mobile = this.mobile,
                description = this.description,
                createdAt = DateTime(this.createdAt.epoch),
                txnStartTime = this.txnStartTime?.seconds,
                balanceV2 = this.balance,
                transactionCount = this.transactionCount,
                lastActivity = this.lastActivity?.let { DateTime(it.epoch) },
                lastPayment = this.lastPayment?.let { DateTime(it.epoch) },
                accountUrl = this.accountUrl,
                profileImage = this.profileImage,
                address = this.address,
                email = this.email,
                newActivityCount = this.newActivityCount,
                lastViewTime = this.lastViewTime?.let { DateTime(it.epoch) },
                registered = this.registered,
                lastBillDate = this.lastBillDate?.let { DateTime(it.epoch) },
                txnAlertEnabled = this.txnAlertEnabled,
                lang = this.lang,
                reminderMode = this.reminderMode,
                isLiveSales = this.isLiveSales,
                addTransactionPermissionDenied = this.addTransactionPermissionDenied,
                state = if (this.state.code == `in`.okcredit.backend.contract.Customer.State.BLOCKED.value) `in`.okcredit.backend.contract.Customer.State.BLOCKED else `in`.okcredit.backend.contract.Customer.State.ACTIVE,
                blockedByCustomer = this.blockedByCustomer,
                restrictContactSync = this.restrictContactSync,
                lastReminderSendTime = DateTime(this.lastReminderSendTime.epoch),
            )
        }
    }

    fun Customer.toCustomer(dueInfo: DueInfo): `in`.okcredit.backend.contract.Customer {
        return `in`.okcredit.backend.contract.Customer(
            id = this.id,
            customerSyncStatus = this.customerSyncStatus,
            status = this.status,
            mobile = this.mobile,
            description = this.description,
            createdAt = DateTime(this.createdAt.epoch),
            txnStartTime = this.txnStartTime?.seconds,
            balanceV2 = this.balance,
            transactionCount = this.transactionCount,
            lastActivity = this.lastActivity?.let { DateTime(it.epoch) },
            lastPayment = this.lastPayment?.let { DateTime(it.epoch) },
            accountUrl = this.accountUrl,
            profileImage = this.profileImage,
            address = this.address,
            email = this.email,
            newActivityCount = this.newActivityCount,
            lastViewTime = this.lastViewTime?.let { DateTime(it.epoch) },
            registered = this.registered,
            lastBillDate = this.lastBillDate?.let { DateTime(it.epoch) },
            txnAlertEnabled = this.txnAlertEnabled,
            lang = this.lang,
            reminderMode = this.reminderMode,
            isLiveSales = this.isLiveSales,
            addTransactionPermissionDenied = this.addTransactionPermissionDenied,
            state = if (this.state.code == `in`.okcredit.backend.contract.Customer.State.BLOCKED.value) `in`.okcredit.backend.contract.Customer.State.BLOCKED else `in`.okcredit.backend.contract.Customer.State.ACTIVE,
            blockedByCustomer = this.blockedByCustomer,
            restrictContactSync = this.restrictContactSync,
            dueActive = dueInfo.isDueActive,
            dueInfo_activeDate = dueInfo.activeDate,
            customDateSet = dueInfo.isCustomDateSet,
            lastActivityMetaInfo = this.lastActivityMetaInfo!!,
            lastAmount = this.lastAmount!!,
            lastReminderSendTime = DateTime(this.lastReminderSendTime.epoch),
        )
    }

    fun toTransaction(coreTransaction: `in`.okcredit.merchant.core.model.Transaction): merchant.okcredit.accounting.model.Transaction {
        var images: MutableList<TransactionImage> = mutableListOf()
        if (coreTransaction.images.isNotEmpty())
            images = mutableListOf()
        for (image in coreTransaction.images) {
            images.add(
                TransactionImage(
                    id = image.id,
                    request_id = image.id,
                    transaction_id = image.transactionId,
                    url = image.url,
                    create_time = DateTime(image.createdAt.epoch)
                )
            )
        }
        return merchant.okcredit.accounting.model.Transaction(
            id = coreTransaction.id,
            type = coreTransaction.type.code,
            customerId = coreTransaction.customerId,
            collectionId = coreTransaction.collectionId,
            amountV2 = coreTransaction.amount,
            receiptUrl = images,
            note = coreTransaction.note,
            createdAt = DateTime(coreTransaction.createdAt.epoch),
            isOnboarding = false,
            isDeleted = coreTransaction.isDeleted,
            deleteTime = coreTransaction.deleteTime?.let { DateTime(it.epoch) },
            isDirty = coreTransaction.isDirty,
            billDate = DateTime(coreTransaction.billDate.epoch),
            updatedAt = DateTime(coreTransaction.updatedAt.epoch),
            isSmsSent = coreTransaction.smsSent,
            isCreatedByCustomer = coreTransaction.createdByCustomer,
            isDeletedByCustomer = coreTransaction.deletedByCustomer,
            inputType = coreTransaction.inputType,
            voiceId = coreTransaction.voiceId,
            transactionState = coreTransaction.state.code,
            transactionCategory = coreTransaction.category.code,
            amountUpdated = coreTransaction.amountUpdated,
            amountUpdatedAt = coreTransaction.amountUpdatedAt?.let { DateTime(it.epoch) }
        )
    }
}
