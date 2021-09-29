package `in`.okcredit.backend._offline.common

import `in`.okcredit.merchant.core.store.database.CoreDbReminderProfile
import org.joda.time.DateTime

data class DbReminderProfile(
    var id: String,
    var businessId: String,
    var description: String,
    var profileImage: String? = null,
    var balance: Long,
    var lastPayment: DateTime? = null,
    val lastReminderSendTime: DateTime? = null,
    var reminderMode: String? = null,
    var firstTxnTime: DateTime? = null,
    var dueSinceTime: DateTime? = null,
)

fun List<CoreDbReminderProfile>.toBackendReminderProfileList() = this.map { it.toBackendReminderProfile() }

fun CoreDbReminderProfile.toBackendReminderProfile() = DbReminderProfile(
    id = this.id,
    businessId = this.businessId,
    description = this.description,
    profileImage = this.profileImage,
    balance = this.balance,
    lastPayment = this.lastPayment?.let { DateTime(it.epoch) },
    lastReminderSendTime = this.lastReminderSendTime?.let { DateTime(it.epoch) },
    reminderMode = this.reminderMode,
    firstTxnTime = this.firstTxnTime?.let { DateTime(it.epoch) },
    dueSinceTime = this.dueSinceTime?.let { DateTime(it.epoch) }
)
