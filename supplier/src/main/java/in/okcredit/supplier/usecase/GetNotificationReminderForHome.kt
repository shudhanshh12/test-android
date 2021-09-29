package `in`.okcredit.supplier.usecase

import `in`.okcredit.backend.utils.CurrencyUtil
import `in`.okcredit.merchant.contract.GetActiveBusinessId
import `in`.okcredit.merchant.suppliercredit.SupplierCreditRepository
import `in`.okcredit.merchant.suppliercredit.model.NotificationReminderForUi
import `in`.okcredit.shared.utils.toTimestamp
import dagger.Lazy
import io.reactivex.Single
import merchant.okcredit.supplier.contract.IsNetworkReminderEnabled
import org.joda.time.DateTime
import tech.okcredit.android.base.utils.DateTimeUtils
import tech.okcredit.android.base.utils.ThreadUtils.worker
import timber.log.Timber
import javax.inject.Inject

class GetNotificationReminderForHome @Inject constructor(
    private val isNetworkReminderEnabled: Lazy<IsNetworkReminderEnabled>,
    private val supplierCreditRepository: Lazy<SupplierCreditRepository>,
    private val getActiveBusinessId: Lazy<GetActiveBusinessId>,
) {
    private companion object {
        const val TAG = "GetNotificationReminderForHome"
    }

    fun execute(): Single<NotificationReminderResponse> {
        return isNetworkReminderEnabled.get().execute().subscribeOn(worker())
            .observeOn(worker())
            .flatMap { enabled ->

                if (!enabled) {
                    Timber.i("$TAG network reminder disabled ")
                    return@flatMap Single.just(NotificationReminderResponse(enabled = enabled))
                }

                return@flatMap getActiveBusinessId.get().execute().flatMap { businessId ->
                    supplierCreditRepository.get().getNotificationReminderData(businessId).flatMap { it ->

                        if (it.isNullOrEmpty()) {
                            Timber.i("$TAG network reminder enable but data unavailable ")
                            return@flatMap Single.just(NotificationReminderResponse(enabled = !enabled))
                        }

                        Timber.i("$TAG network reminder enable but data available ")
                        val totalReminders = it.size
                        val notificationReminderData = it.first()
                        Timber.i("$TAG $notificationReminderData")

                        return@flatMap Single.just(
                            NotificationReminderResponse(
                                enabled = enabled,
                                notificationReminderForUi = NotificationReminderForUi(
                                    totalNotificationCount = totalReminders - 1,
                                    name = notificationReminderData.name,
                                    profileImage = notificationReminderData.profileImage,
                                    balance = CurrencyUtil.formatV2(notificationReminderData.balance?.toLong() ?: 0),
                                    lastPayment = notificationReminderData.lastPayment?.let { it1 ->
                                        CurrencyUtil.formatV2(it1.toLong())
                                    },
                                    lastPaymentDate = notificationReminderData.lastPaymentDate?.let {
                                        DateTimeUtils.formatDateOnly(
                                            DateTime(it.toTimestamp().epoch)
                                        )
                                    },
                                    accountId = notificationReminderData.accountId,
                                    notificationId = notificationReminderData.notificationId,
                                    balanceInPaisa = notificationReminderData.balance.toString(),
                                    lastPaymentInPaisa = notificationReminderData.lastPayment?.toString(),
                                )
                            )
                        )
                    }
                }
            }
    }

    data class NotificationReminderResponse(
        val enabled: Boolean,
        val notificationReminderForUi: NotificationReminderForUi? = null,
    )
}
