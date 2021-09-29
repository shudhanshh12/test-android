package `in`.okcredit.merchant.customer_ui.ui.bulk_reminder_v2

import `in`.okcredit.merchant.customer_ui.R
import `in`.okcredit.merchant.customer_ui.ui.bulk_reminder_v2.BulkReminderV2Contract.*
import `in`.okcredit.merchant.customer_ui.ui.bulk_reminder_v2.BulkReminderV2Contract.ReminderProfile.ReminderType.PENDING_REMINDER
import `in`.okcredit.merchant.customer_ui.ui.bulk_reminder_v2.BulkReminderV2Contract.ReminderProfile.ReminderType.TODAYS_REMINDER
import `in`.okcredit.merchant.customer_ui.ui.bulk_reminder_v2.analytics.BulkReminderAnalyticsImpl
import `in`.okcredit.merchant.customer_ui.ui.bulk_reminder_v2.usecase.*
import `in`.okcredit.shared.base.BaseViewModel
import `in`.okcredit.shared.base.UiState
import `in`.okcredit.shared.usecase.GetConnectionStatus
import `in`.okcredit.shared.usecase.IsInternetAvailable
import `in`.okcredit.shared.usecase.Result
import dagger.Lazy
import io.reactivex.Observable
import kotlinx.coroutines.rx2.asObservable
import kotlinx.coroutines.rx2.rxSingle
import javax.inject.Inject

class BulkReminderV2ViewModel @Inject constructor(
    initialState: State,
    private val getDefaulters: Lazy<GetDefaulters>,
    private val getReminderProfileForCustomers: Lazy<GetReminderProfileForCustomers>,
    private val addSelectedReminder: Lazy<AddSelectedReminder>,
    private val removeDeselectedReminder: Lazy<RemoveDeselectedReminder>,
    private val updateReminderMode: Lazy<UpdateReminderMode>,
    private val addAllSelectedReminder: Lazy<AddAllSelectedReminders>,
    private val removeAllDeselectedReminders: Lazy<RemoveAllDeselectedReminder>,
    private val updateLastReminderSendTime: Lazy<UpdateLastReminderSentTime>,
    private val tracker: Lazy<BulkReminderAnalyticsImpl>,
    private val getConnectionStatus: Lazy<GetConnectionStatus>,
    private val isInternetAvailable: Lazy<IsInternetAvailable>,
) : BaseViewModel<State, PartialState, ViewEvent>(
    initialState
) {

    private var responseData: ResponseData = ResponseData()

    override fun handle(): Observable<UiState.Partial<State>> {
        return Observable.mergeArray(
            loadDefaulters(),
            observeReminderSelectedIntent(),
            observeReminderDeselectedIntent(),
            observeSetReminderModeIntent(),
            observeReminderTabClickedIntent(),
            observeSelectAllClickedIntent(),
            observeDeselectAllClickedIntent(),
            observeSendButtonClicked(),
            observeSyncLastReminderSentTimeIntent(),
            observeShowCongratulationsBannerIntent(),
            observeTrackSendReminderClicked(),
            observeInternetAvailable(),
            observeConnectionStatus(),
        )
    }

    private fun observeInternetAvailable() = intent<Intent.Load>()
        .switchMap { isInternetAvailable.get().execute() }
        .map {
            PartialState.SetInternetAvailable(it)
        }

    private fun observeConnectionStatus() = intent<Intent.Load>()
        .switchMap { getConnectionStatus.get().executeUnwrapped() }
        .map {
            PartialState.SetInternetAvailable(it)
        }

    private fun observeTrackSendReminderClicked() = intent<Intent.TrackSendReminderClicked>()
        .map {
            trackSendButtonClicked(it.responseData)
            PartialState.NoChange
        }

    private fun observeShowCongratulationsBannerIntent() = intent<Intent.ShowCongratulationsBanner>()
        .map {
            responseData = removeAllDeselectedReminders.get().execute(responseData, responseData.reminderProfiles)
            PartialState.ShowCongratulationBanner(responseData)
        }

    private fun observeSyncLastReminderSentTimeIntent() = intent<Intent.SyncLastReminderSentTime>()
        .switchMap { wrap(rxSingle { updateLastReminderSendTime.get().execute() }) }
        .map {
            PartialState.NoChange
        }

    private fun observeSendButtonClicked() = intent<Intent.SendButtonClicked>()
        .map { intent ->
            pushIntent(Intent.TrackSendReminderClicked(intent.responseData))
            val selectedReminders = intent.responseData?.reminderProfiles?.filter { it.isSelected }
            emitViewEvent(ViewEvent.GoToSendReminderDialog(selectedReminders))
            PartialState.NoChange
        }

    private fun observeDeselectAllClickedIntent() = intent<Intent.DeselectAllClicked>()
        .map {
            responseData = removeAllDeselectedReminders.get().execute(it.responseData, it.defaulters)

            PartialState.SetResponseData(
                responseData = responseData
            )
        }

    private fun observeSelectAllClickedIntent() = intent<Intent.SelectAllClicked>()
        .map {
            responseData = addAllSelectedReminder.get().execute(it.responseData, it.defaulters)

            PartialState.SetResponseData(
                responseData = responseData
            )
        }

    private fun observeReminderTabClickedIntent() = intent<Intent.ReminderTabClicked>()
        .map {
            if (it.reminderTabType == PENDING_REMINDER) {
                PartialState.SetPendingReminderCollapsed(it.isCollapsed)
            } else {
                PartialState.SetTodaysReminderCollapsed(it.isCollapsed)
            }
        }

    private fun observeSetReminderModeIntent(): Observable<PartialState> {
        var customerId = ""
        var reminderMode: ReminderProfile.ReminderMode? = null
        return intent<Intent.SetReminderMode>()
            .switchMap {
                customerId = it.customerId
                reminderMode = it.reminderMode
                wrap(
                    rxSingle {
                        updateReminderMode.get().execute(it.customerId, it.reminderMode, it.responseData)
                    }
                )
            }
            .map {
                when (it) {
                    is Result.Success -> {
                        emitViewEvent(ViewEvent.TrackUpdateProfile(customerId, reminderMode))
                        responseData = it.value
                        PartialState.SetResponseData(
                            responseData = it.value
                        )
                    }
                    is Result.Failure -> {
                        if (isInternetIssue(it.error)) {
                            emitViewEvent(ViewEvent.ShowError(R.string.t_001_daily_remind_default_reminder_type_no_internet_err))
                        }
                        PartialState.NoChange
                    }
                    else -> PartialState.NoChange
                }
            }
    }

    private fun observeReminderSelectedIntent() = intent<Intent.ReminderSelected>()
        .map {
            responseData = addSelectedReminder.get().execute(
                it.responseData,
                it.selectedProfile,
            )
            PartialState.SetResponseData(
                responseData,
            )
        }

    private fun observeReminderDeselectedIntent() = intent<Intent.ReminderDeselected>()
        .map {
            responseData = removeDeselectedReminder.get().execute(
                it.responseData,
                it.deselectedProfile,
            )
            PartialState.SetResponseData(
                responseData,
            )
        }

    private fun loadDefaulters() = intent<Intent.Load>()
        .switchMap { getDefaulters.get().execute().asObservable() }
        .switchMap {
            wrap(
                rxSingle {
                    getReminderProfileForCustomers.get()
                        .execute(it, responseData)
                }
            )
        }
        .map {
            when (it) {
                is Result.Progress -> PartialState.NoChange
                is Result.Success -> {
                    PartialState.SetResponseData(
                        responseData = ResponseData(
                            topBanner = it.value.topBanner,
                            reminderProfiles = it.value.reminderProfiles,
                        )
                    )
                }
                is Result.Failure -> PartialState.SetResponseData(responseData = null)
            }
        }

    override fun reduce(currentState: State, partialState: PartialState): State {
        val tempState = when (partialState) {
            is PartialState.NoChange -> currentState
            is PartialState.SetResponseData -> currentState.copy(
                responseData = partialState.responseData
            )
            is PartialState.SetPendingReminderCollapsed -> currentState.copy(
                isPendingReminderCollapsed = partialState.collapsed
            )
            is PartialState.SetTodaysReminderCollapsed -> currentState.copy(
                isTodaysReminderCollapsed = partialState.collapsed
            )
            is PartialState.ShowCongratulationBanner -> currentState.copy(
                isPendingReminderSelectedAll = false,
                isTodaysReminderSelectedAll = false,
                showCongratulationBanner = true,
                responseData = partialState.responseData
            )
            is PartialState.SetInternetAvailable -> currentState.copy(
                internetAvailable = partialState.value
            )
        }

        val selectedReminderProfiles =
            getSelectedReminderCount(tempState.responseData?.reminderProfiles ?: emptyList())
        val isPendingReminderSelectedAll = isPendingReminderAllSelected(tempState.responseData?.reminderProfiles)
        val isTodaysReminderSelectedAll = isTodaysReminderAllSelected(tempState.responseData?.reminderProfiles)

        val modelState = tempState.copy(
            isPendingReminderSelectedAll = isPendingReminderSelectedAll,
            isTodaysReminderSelectedAll = isTodaysReminderSelectedAll,
            enableSendButton = selectedReminderProfiles > 0,
            selectedReminderCount = selectedReminderProfiles,
        )

        return modelState.copy(
            bulkReminderEpoxyModels = getBulkReminderEpoxyModels(modelState),
        )
    }

    private fun isPendingReminderAllSelected(reminderProfiles: List<ReminderProfile>?): Boolean {
        return reminderProfiles
            ?.filter { it.reminderType == PENDING_REMINDER }
            ?.all { it.isSelected }
            ?: false
    }

    private fun isTodaysReminderAllSelected(reminderProfiles: List<ReminderProfile>?): Boolean {
        return reminderProfiles
            ?.filter { it.reminderType == TODAYS_REMINDER }
            ?.all { it.isSelected }
            ?: false
    }

    private fun getSelectedReminderCount(data: List<ReminderProfile>): Int {
        return data.count { it.isSelected }
    }

    private fun getBulkReminderEpoxyModels(
        tempState: State,
    ): MutableList<BulkReminderEpoxyModel> {
        val epoxyModels = mutableListOf<BulkReminderEpoxyModel>()
        addTopBanner(epoxyModels, tempState)
        addCongratulationsBanner(epoxyModels, tempState)
        addPendingReminderTab(epoxyModels, tempState)
        addTodaysReminderTab(epoxyModels, tempState)
        return epoxyModels
    }

    private fun addCongratulationsBanner(
        epoxyModels: MutableList<BulkReminderEpoxyModel>,
        tempState: State,
    ) {
        if (tempState.showCongratulationBanner) {
            epoxyModels.add(BulkReminderEpoxyModel.CongratulationBanner)
        }
    }

    private fun addTopBanner(
        epoxyModels: MutableList<BulkReminderEpoxyModel>,
        tempState: State,
    ) {
        if (tempState.responseData?.topBanner != null &&
            tempState.responseData.topBanner.totalBalanceDue < 0
        ) {
            epoxyModels.add(tempState.responseData.topBanner)
        }
    }

    private fun addPendingReminderTab(
        epoxyModels: MutableList<BulkReminderEpoxyModel>,
        tempState: State,
    ) {
        val pendingRemindersList: List<ReminderProfile>? = tempState.responseData?.reminderProfiles
            ?.filter { it.reminderType == PENDING_REMINDER }
            ?.takeIf { it.isNotEmpty() }

        if (pendingRemindersList != null) {
            epoxyModels.add(
                BulkReminderEpoxyModel.ReminderTab(
                    reminderTabType = PENDING_REMINDER,
                    defaulters = pendingRemindersList,
                    totalDefaulters = pendingRemindersList.size,
                    isCollapsed = tempState.isPendingReminderCollapsed,
                    isAllSelected = tempState.isPendingReminderSelectedAll
                )
            )
        }
    }

    private fun addTodaysReminderTab(
        epoxyModels: MutableList<BulkReminderEpoxyModel>,
        tempState: State,
    ) {

        val todaysRemindersList: List<ReminderProfile>? = tempState.responseData?.reminderProfiles
            ?.filter { it.reminderType == TODAYS_REMINDER }
            ?.takeIf { it.isNotEmpty() }

        if (todaysRemindersList != null) {
            epoxyModels.add(
                BulkReminderEpoxyModel.ReminderTab(
                    reminderTabType = TODAYS_REMINDER,
                    defaulters = todaysRemindersList,
                    totalDefaulters = todaysRemindersList.size,
                    isCollapsed = tempState.isTodaysReminderCollapsed,
                    isAllSelected = tempState.isTodaysReminderSelectedAll
                )
            )
        }
    }

    private fun trackSendButtonClicked(responseData: ResponseData?) {
        val trackerData = responseData?.reminderProfiles.trackerData()

        tracker.get().trackBulkReminderSendStarted(
            totalReminder = trackerData.totalReminder,
            customerReminderListed = trackerData.customerReminderListed,
            sendTodayReminderListed = trackerData.sendTodayReminderListed,
            reminderSelected = trackerData.reminderSelected,
            customerReminderSelected = trackerData.customerReminderSelected,
            sendTodayReminderSelected = trackerData.sendTodayReminderSelected,
            customerSelectAll = trackerData.customerSelectAll,
            sendTodaySelectAll = trackerData.sendTodaySelectAll,
        )
    }
}

internal fun List<ReminderProfile>?.trackerData(): TrackerData {
    val totalReminders = this?.count() ?: 0

    val customersReminders = this?.count { it.reminderType == PENDING_REMINDER } ?: 0

    val sendTodaysReminders = this?.count { it.reminderType == TODAYS_REMINDER } ?: 0

    val selectedReminders = this?.count { it.isSelected } ?: 0

    val customerSelectedReminders = this?.count {
        it.isSelected &&
            it.reminderType == PENDING_REMINDER
    } ?: 0

    val sendTodaysSelectedReminders = this?.count {
        it.isSelected &&
            it.reminderType == TODAYS_REMINDER
    } ?: 0

    val customerSelectAll = (customerSelectedReminders == customersReminders) && customersReminders != 0
    val sendTodayReminderSelectAll = (sendTodaysSelectedReminders == sendTodaysReminders) && sendTodaysReminders != 0
    return TrackerData(
        totalReminders,
        customersReminders,
        sendTodaysReminders,
        selectedReminders,
        customerSelectedReminders,
        sendTodaysSelectedReminders,
        customerSelectAll,
        sendTodayReminderSelectAll
    )
}

data class TrackerData(
    val totalReminder: Int,
    val customerReminderListed: Int,
    val sendTodayReminderListed: Int,
    val reminderSelected: Int,
    val customerReminderSelected: Int,
    val sendTodayReminderSelected: Int,
    val customerSelectAll: Boolean,
    val sendTodaySelectAll: Boolean,
)
