package `in`.okcredit.merchant.customer_ui.ui.bulk_reminder_v2

import `in`.okcredit.backend._offline.usecase.GetPaymentReminderIntent
import `in`.okcredit.merchant.customer_ui.ui.bulk_reminder_v2.BulkReminderV2Contract.BulkReminderEpoxyModel.TopBanner
import `in`.okcredit.merchant.customer_ui.ui.bulk_reminder_v2.BulkReminderV2Contract.ReminderProfile.ReminderType
import `in`.okcredit.shared.base.BaseViewEvent
import `in`.okcredit.shared.base.UiState
import `in`.okcredit.shared.base.UserIntent
import android.os.Parcelable
import androidx.annotation.Keep
import androidx.annotation.StringRes
import kotlinx.android.parcel.Parcelize

interface BulkReminderV2Contract {
    data class State(
        val internetAvailable: Boolean = true,
        val bulkReminderEpoxyModels: MutableList<BulkReminderEpoxyModel> = mutableListOf(),
        val selectedReminderCount: Int = 0,
        val isLoadingSendReminder: Boolean = false,
        val enableSendButton: Boolean = false,
        val responseData: ResponseData? = null,
        val isPendingReminderCollapsed: Boolean = false,
        val isTodaysReminderCollapsed: Boolean = false,
        val isPendingReminderSelectedAll: Boolean = false,
        val isTodaysReminderSelectedAll: Boolean = false,
        val showCongratulationBanner: Boolean = false,
    ) : UiState

    data class ResponseData(
        val topBanner: TopBanner? = null,
        val reminderProfiles: List<ReminderProfile> = emptyList(),
    )

    sealed class BulkReminderEpoxyModel {
        data class TopBanner(
            val totalBalanceDue: Long = 0,
            val defaultedSince: Int = 0,
            val totalCustomers: Int = 0,
        ) : BulkReminderEpoxyModel()

        data class ReminderTab(
            val reminderTabType: ReminderType? = null,
            val totalDefaulters: Int = 0,
            val defaulters: List<ReminderProfile> = emptyList(),
            val isCollapsed: Boolean = true,
            val isAllSelected: Boolean = false,
        ) : BulkReminderEpoxyModel()

        object CongratulationBanner : BulkReminderEpoxyModel()
    }

    @Keep
    @Parcelize
    data class ReminderProfile(
        val reminderType: ReminderType? = null,
        val customerId: String? = null,
        val customerName: String? = null,
        val profileUrl: String = "",
        val dueSince: String = "",
        val totalBalanceDue: String = "",
        var isSelected: Boolean = false,
        val lastReminderSend: String = "",
        val lastReminderSendInDays: Int = 0,
        val dueSinceInDays: Int = 0,
        var reminderMode: ReminderMode = ReminderMode.WHATSAPP,
        val reminderStringsObject: GetPaymentReminderIntent.ReminderStringsObject,
    ) : Parcelable {
        enum class ReminderMode(
            val value: String,
        ) {
            SMS("sms"),
            WHATSAPP("whatsapp");

            companion object {
                fun from(value: String?) = when (value) {
                    SMS.value -> SMS
                    WHATSAPP.value -> WHATSAPP
                    else -> WHATSAPP
                }
            }
        }

        enum class ReminderType {
            PENDING_REMINDER,
            TODAYS_REMINDER;
        }
    }

    sealed class PartialState : UiState.Partial<State> {
        object NoChange : PartialState()
        data class SetTodaysReminderCollapsed(val collapsed: Boolean) : PartialState()
        data class SetPendingReminderCollapsed(val collapsed: Boolean) : PartialState()
        data class SetResponseData(
            val responseData: ResponseData?,
        ) : PartialState()

        data class ShowCongratulationBanner(val responseData: ResponseData?) : PartialState()
        data class SetInternetAvailable(val value: Boolean) : PartialState()
    }

    sealed class Intent : UserIntent {
        object Load : Intent()
        object SyncLastReminderSentTime : Intent()
        object ShowCongratulationsBanner : Intent()

        data class ReminderTabClicked(val reminderTabType: ReminderType, val isCollapsed: Boolean) : Intent()

        data class ReminderSelected(
            val responseData: ResponseData,
            val selectedProfile: ReminderProfile,
        ) : Intent()

        data class ReminderDeselected(
            val responseData: ResponseData,
            val deselectedProfile: ReminderProfile,
        ) : Intent()

        data class SetReminderMode(
            val customerId: String,
            val reminderMode: ReminderProfile.ReminderMode,
            val responseData: ResponseData,
        ) : Intent()

        data class SelectAllClicked(
            val responseData: ResponseData,
            val reminderTabType: ReminderType,
            val defaulters: List<ReminderProfile>,
        ) : Intent()

        data class DeselectAllClicked(
            val responseData: ResponseData,
            val reminderTabType: ReminderType,
            val defaulters: List<ReminderProfile>,
        ) : Intent()

        data class SendButtonClicked(val responseData: ResponseData?) : Intent()
        data class TrackSendReminderClicked(val responseData: ResponseData?) : Intent()
    }

    sealed class ViewEvent : BaseViewEvent {
        data class TrackUpdateProfile(
            val accountId: String,
            val reminderMode: ReminderProfile.ReminderMode?,
        ) : ViewEvent()

        data class ShowError(@StringRes val stringRes: Int) : ViewEvent()

        data class GoToSendReminderDialog(val selectedReminders: List<ReminderProfile>?) : ViewEvent()
    }
}
