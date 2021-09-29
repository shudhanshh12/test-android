package `in`.okcredit.merchant.customer_ui.ui.customerreports

import `in`.okcredit.analytics.PropertyValue
import `in`.okcredit.backend.contract.Customer
import `in`.okcredit.backend.contract.RxSharedPrefValues
import `in`.okcredit.backend.contract.RxSharedPrefValues.IS_REPORT_SHARE_EDUCATION_SHOWN
import `in`.okcredit.backend.utils.CurrencyUtil
import `in`.okcredit.communication_inappnotification.contract.LocalInAppNotificationHandler
import `in`.okcredit.communication_inappnotification.contract.ui.local.TapTargetLocal
import `in`.okcredit.merchant.customer_ui.R
import `in`.okcredit.merchant.customer_ui.analytics.CustomerEventTracker
import `in`.okcredit.merchant.customer_ui.databinding.CustomerReportsFragmentBinding
import `in`.okcredit.merchant.customer_ui.ui.add_customer_dialog.AddNumberDialogScreen
import `in`.okcredit.merchant.customer_ui.ui.customerreports.CustomerReportsContract.SelectedDateMode.*
import `in`.okcredit.merchant.customer_ui.utils.CustomerUtils
import `in`.okcredit.shared.base.BaseFragment
import `in`.okcredit.shared.base.UserIntent
import `in`.okcredit.shared.utils.CommonUtils
import android.Manifest
import android.annotation.SuppressLint
import android.app.ProgressDialog
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.UiThread
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.airbnb.epoxy.EpoxyVisibilityTracker
import com.google.android.material.snackbar.Snackbar
import com.jakewharton.rxbinding3.view.clicks
import dagger.Lazy
import io.reactivex.Observable
import io.reactivex.subjects.PublishSubject
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.joda.time.DateTime
import tech.okcredit.android.base.extensions.setBooleanVisibility
import tech.okcredit.android.base.extensions.shortToast
import tech.okcredit.android.base.extensions.snackbar
import tech.okcredit.android.base.extensions.viewLifecycleScoped
import tech.okcredit.android.base.language.LocaleManager
import tech.okcredit.android.base.preferences.Scope
import tech.okcredit.android.base.utils.DateTimeUtils
import tech.okcredit.app_contract.LegacyNavigator
import tech.okcredit.base.permission.IPermissionListener
import tech.okcredit.base.permission.Permission.requestStoragePermission
import uk.co.samuelwall.materialtaptargetprompt.MaterialTapTargetPrompt
import java.lang.ref.WeakReference
import java.util.concurrent.TimeUnit
import javax.inject.Inject

class CustomerReportsFragment :
    BaseFragment<CustomerReportsContract.State, CustomerReportsContract.ViewEvent, CustomerReportsContract.Intent>(
        label = "CustomerReportsScreen",
        contentLayoutId = R.layout.customer_reports_fragment
    ) {

    internal val binding: CustomerReportsFragmentBinding by viewLifecycleScoped(CustomerReportsFragmentBinding::bind)

    private var alert: Snackbar? = null

    private lateinit var customerReportsController: CustomerReportsController

    internal val onChangeDate: PublishSubject<CustomerReportsContract.SelectedDate> = PublishSubject.create()
    internal val getAllTransactionPublishSubject: PublishSubject<CustomerReportsContract.SelectedDateMode> =
        PublishSubject.create()

    private var isFirstTimeLoad = false
    internal var job: Job? = null
    private var isDateFilterEducationShown = false
    private var isReportShareEducationShown = false
    private var triggerEventOnload = false
    private var progressDialog: ProgressDialog? = null

    companion object {
        const val SPACE = " "
        const val DATE_RANGE = "daterange"
    }

    @Inject
    internal lateinit var legacyNavigator: LegacyNavigator

    @Inject
    internal lateinit var customerEventTracker: Lazy<CustomerEventTracker>

    @Inject
    lateinit var localInAppNotificationHandler: Lazy<LocalInAppNotificationHandler>

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return CustomerReportsFragmentBinding.inflate(inflater).root
    }

    private val dataObserver by lazy {
        object : RecyclerView.AdapterDataObserver() {
            override fun onItemRangeInserted(positionStart: Int, itemCount: Int) {
                binding.recyclerView.scrollToPosition(0)
            }
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        customerReportsController = CustomerReportsController()
        val linearLayoutManager = LinearLayoutManager(context)
        binding.apply {
            recyclerView.layoutManager = linearLayoutManager
            recyclerView.adapter = customerReportsController.adapter

            customerReportsController.adapter.registerAdapterDataObserver(dataObserver)

            dateRange.setOnClickListener {

                val state = getCurrentState()
                customerEventTracker.get().trackDateRangeClick(
                    screen = CustomerEventTracker.CUSTOMER_REPORTS_SCREEN,
                    relation = PropertyValue.CUSTOMER,
                    accountId = state.customerId
                )

                merchant.okcredit.accounting.views.DateRangePickerDialog.show(
                    requireContext(),
                    state.startDate,
                    state.endDate,
                    object : merchant.okcredit.accounting.views.DateRangePickerDialog.Listener {
                        override fun onDone(startDate: DateTime, endDate: DateTime) {
                            onChangeDate.onNext(
                                CustomerReportsContract.SelectedDate(
                                    startDate,
                                    endDate,
                                    CUSTOM_DATE
                                )
                            )
                        }
                    }
                ).show()
            }

            thisMonth.setOnClickListener {
                showTransactionForThisMonth()
            }

            lastSevenDays.setOnClickListener {
                showTransactionForLastWeek()
            }

            lastMonth.setOnClickListener {
                showTransactionForLastMonth()
            }

            lastThreeMonth.setOnClickListener {
                showTransactionForLastThreeMonth()
            }

            lastSixMonth.setOnClickListener {
                showTransactionForLastSixMonth()
            }

            overall.setOnClickListener {
                showTransactionForOverall()
            }

            toolbar.setNavigationOnClickListener {
                requireActivity().onBackPressed()
            }

            downloadReport.setOnClickListener {
                if (getCurrentState().isReportDownloading.not())
                    requestStoragePermissionAndDownloadReport(true)
                else shortToast(R.string.downloading_please_wait)
            }

            shareReport.setOnClickListener {
                val state = getCurrentState()
                customerEventTracker.get().trackSendReport(
                    screen = CustomerEventTracker.CUSTOMER_REPORTS_SCREEN,
                    relation = PropertyValue.CUSTOMER,
                    accountId = state.customerId,
                    value = getValueFromSelectedMode(state.selectedMode),
                    dueAmount = state.balanceForSelectedDuration,
                    collectionAdopted = state.isCollectionAdopted,
                    dateRange = mutableListOf(
                        DateTimeUtils.formatDateOnly(state.startDate),
                        DateTimeUtils.formatDateOnly(state.endDate)
                    )
                )
                if (state.customer?.mobile.isNullOrEmpty()) {
                    showAddMobileNumberDialog(state.customer!!)
                } else {
                    pushIntent(CustomerReportsContract.Intent.ShareReportClicked)
                }
            }
        }

        binding.rootView.setTracker(performanceTracker)
    }

    private fun showAddMobileNumberDialog(customer: Customer) {
        val dialog = AddNumberDialogScreen.newInstance(
            customerId = customer.id,
            description = getString(R.string.please_add_customer_number),
            isSkipAndSend = true,
            screen = CustomerEventTracker.CUSTOMER_REPORTS_SCREEN
        )
        dialog.setListener(object : AddNumberDialogScreen.Listener {
            override fun onSkip() {
                pushIntent(CustomerReportsContract.Intent.ShareReportClicked)
            }

            override fun onDone() {
                pushIntent(CustomerReportsContract.Intent.ShareReportClicked)
            }
        })
        dialog.show(childFragmentManager, AddNumberDialogScreen.TAG)
    }

    override fun loadIntent(): UserIntent {
        return CustomerReportsContract.Intent.Load
    }

    override fun userIntents(): Observable<UserIntent> {
        return Observable.mergeArray(

            onChangeDate
                .throttleFirst(300, TimeUnit.MILLISECONDS)
                .map {

                    trackUpdateEvent(it)

                    CustomerReportsContract.Intent.ChangeDateRange(
                        it.startDate,
                        it.endDate,
                        it.selectedMode
                    )
                },

            binding.lastZeroBalance.clicks()
                .throttleFirst(300, TimeUnit.MILLISECONDS)
                .map {
                    CustomerReportsContract.Intent.GetMiniStatementDateRange
                },

            getAllTransactionPublishSubject
                .throttleFirst(300, TimeUnit.MILLISECONDS)
                .map {
                    CustomerReportsContract.Intent.GetAllTransactions(it)
                },

            Observable.just(
                CustomerReportsContract.Intent
                    .ObserveDownloadReportWorkerStatus(weakLifecycleOwner = WeakReference(viewLifecycleOwner))
            )
        )
    }

    private fun trackUpdateEvent(it: CustomerReportsContract.SelectedDate) {
        if (triggerEventOnload) {
            val state = getCurrentState()
            customerEventTracker.get().trackDateRangeUpdate(
                screen = CustomerEventTracker.CUSTOMER_REPORTS_SCREEN,
                relation = PropertyValue.CUSTOMER,
                accountId = state.customerId,
                value = getValueFromSelectedMode(it.selectedMode),
                dateRange = mutableListOf(
                    DateTimeUtils.formatDateOnly(state.startDate),
                    DateTimeUtils.formatDateOnly(state.endDate)
                )
            )
        }

        triggerEventOnload = true
    }

    @SuppressLint("RestrictedApi")
    override fun render(state: CustomerReportsContract.State) {
        val epoxyVisibilityTracker = EpoxyVisibilityTracker()
        epoxyVisibilityTracker.attach(binding.recyclerView)

        customerReportsController.setState(state)

        if (state.showDownloadLoader) {
            if (progressDialog == null) {
                progressDialog = ProgressDialog.show(context, "", getString(R.string.account_report_loading))
            } else {
                progressDialog?.show()
            }
        } else {
            progressDialog?.dismiss()
        }

        if (isFirstTimeLoad.not()) {
            isFirstTimeLoad = true
            showTransactionForThisMonth()
        }

        if (state.isDateFilterEducationShown == false && isDateFilterEducationShown.not()) {
            isDateFilterEducationShown = true
            showDateRangeEducation()
        }

        if (state.isReportShareEducationShown == false && isReportShareEducationShown.not()) {
            isReportShareEducationShown = true
            binding.lottieCollectionHighlighter.enableMergePathsForKitKatAndAbove(true)
            binding.lottieCollectionHighlighter.setBooleanVisibility(true)
            pushIntent(
                CustomerReportsContract.Intent.RxPreferenceBoolean(
                    IS_REPORT_SHARE_EDUCATION_SHOWN,
                    true,
                    Scope.Individual
                )
            )
        }

        binding.apply {

            handleSelectedDateModeUI(state)

            val totalAmount: Long = if (state.customer?.balanceV2 == null) 0L else state.customer.balanceV2
            CurrencyUtil.renderV2(totalAmount, totalBalance, totalAmount >= 0)

            balance.text = StringBuilder(getString(R.string.balance))

            val paymentTransactionCount = state.paymentTransactionCount
            val creditTransactionCount = state.creditTransactionCount

            take.text = StringBuilder(getString(R.string.customer_report_take))
                .append(SPACE)
                .append("(")
                .append(paymentTransactionCount)
                .append(")")

            give.text = StringBuilder(getString(R.string.customer_report_give))
                .append(SPACE)
                .append("(")
                .append(creditTransactionCount)
                .append(")")

            CurrencyUtil.renderV2(state.totalPayment, selectedDurationTotalPayment, true)
            CurrencyUtil.renderV2(state.totalCredit, selectedDurationTotalCredit, false)
            CurrencyUtil.renderV2(state.balanceForSelectedDuration, balanceAmount, state.balanceForSelectedDuration > 0)

            selectedDateRange.text = showSelectedDate(state)
        }

        // show/hide alert
        showAlertErrors(state)
        setDownloadUi(state.isReportDownloading)
    }

    private fun setDownloadUi(isReportDownloading: Boolean) {
        binding.downloadReport.text = if (isReportDownloading) {
            getString(R.string.downloading)
        } else {
            getString(R.string.download)
        }
    }

    private fun CustomerReportsFragmentBinding.handleSelectedDateModeUI(state: CustomerReportsContract.State) {
        when (state.selectedMode) {
            CUSTOM_DATE -> {
                dateRange.isSelected = true
                thisMonth.isSelected = false
                lastMonth.isSelected = false
                lastSevenDays.isSelected = false
                lastThreeMonth.isSelected = false
                lastSixMonth.isSelected = false
                overall.isSelected = false
                lastZeroBalance.isSelected = false

                dateRange.text = showSelectedDate(state)
            }

            THIS_MONTH -> {
                thisMonth.isSelected = true
                dateRange.isSelected = false
                lastMonth.isSelected = false
                lastSevenDays.isSelected = false
                lastThreeMonth.isSelected = false
                lastSixMonth.isSelected = false
                overall.isSelected = false
                lastZeroBalance.isSelected = false

                dateRange.text = getString(R.string.date_range)
            }

            LAST_WEEK -> {
                dateRange.isSelected = false
                thisMonth.isSelected = false
                lastMonth.isSelected = false
                lastSevenDays.isSelected = true
                lastThreeMonth.isSelected = false
                lastSixMonth.isSelected = false
                overall.isSelected = false
                lastZeroBalance.isSelected = false

                dateRange.text = getString(R.string.date_range)
            }

            LAST_ZERO_BALANCE -> {
                dateRange.isSelected = false
                thisMonth.isSelected = false
                lastMonth.isSelected = false
                lastSevenDays.isSelected = false
                lastThreeMonth.isSelected = false
                lastSixMonth.isSelected = false
                overall.isSelected = false
                lastZeroBalance.isSelected = true

                dateRange.text = getString(R.string.date_range)
            }

            LAST_MONTH -> {
                dateRange.isSelected = false
                thisMonth.isSelected = false
                lastMonth.isSelected = true
                lastSevenDays.isSelected = false
                lastThreeMonth.isSelected = false
                lastSixMonth.isSelected = false
                overall.isSelected = false
                lastZeroBalance.isSelected = false

                dateRange.text = getString(R.string.date_range)
            }

            LAST_THREE_MONTHS -> {
                dateRange.isSelected = false
                thisMonth.isSelected = false
                lastMonth.isSelected = false
                lastSevenDays.isSelected = false
                lastThreeMonth.isSelected = true
                lastSixMonth.isSelected = false
                overall.isSelected = false
                lastZeroBalance.isSelected = false

                dateRange.text = getString(R.string.date_range)
            }

            LAST_SIX_MONTHS -> {
                dateRange.isSelected = false
                thisMonth.isSelected = false
                lastMonth.isSelected = false
                lastSevenDays.isSelected = false
                lastThreeMonth.isSelected = false
                lastSixMonth.isSelected = true
                overall.isSelected = false
                lastZeroBalance.isSelected = false

                dateRange.text = getString(R.string.date_range)
            }

            OVERALL -> {
                dateRange.isSelected = false
                thisMonth.isSelected = false
                lastMonth.isSelected = false
                lastSevenDays.isSelected = false
                lastThreeMonth.isSelected = false
                lastSixMonth.isSelected = false
                overall.isSelected = true
                lastZeroBalance.isSelected = false

                dateRange.text = getString(R.string.date_range)
            }
        }
    }

    private fun isStartAndEndDateAreSame(state: CustomerReportsContract.State): Boolean {
        return state.startDate.dayOfMonth() == state.endDate.dayOfMonth() &&
            state.startDate.monthOfYear() == state.endDate.monthOfYear() &&
            state.startDate.year() == state.endDate.year()
    }

    private fun isDifferentMonthSameYear(state: CustomerReportsContract.State): Boolean {
        return state.startDate.monthOfYear() != state.endDate.monthOfYear() &&
            state.startDate.year() == state.endDate.year()
    }

    private fun isDifferentYear(state: CustomerReportsContract.State): Boolean {
        return state.startDate.year() != state.endDate.year()
    }

    private fun showSelectedDate(state: CustomerReportsContract.State): StringBuilder {

        val language = LocaleManager.getLanguage(requireContext())

        val startDate = state.startDate.dayOfMonth().asText
        val startMonth = if (language == LocaleManager.LANGUAGE_ENGLISH) {
            state.startDate.monthOfYear().asText.substring(0..2)
        } else {
            state.startDate.monthOfYear().asText
        }
        val startYear = state.startDate.year().asText

        val endDate = state.endDate.dayOfMonth().asText
        val endMonth = if (language == LocaleManager.LANGUAGE_ENGLISH) {
            state.endDate.monthOfYear().asText.substring(0..2)
        } else {
            state.endDate.monthOfYear().asText
        }
        val endYear = state.endDate.year().asText

        return when {
            isStartAndEndDateAreSame(state) -> {
                dateRangeUIForSameDate(endDate, endMonth, endYear)
            }
            isDifferentYear(state) -> {
                dateRangeUIForDifferentYear(startDate, startMonth, startYear, endDate, endMonth, endYear)
            }
            isDifferentMonthSameYear(state) -> {
                dateRangeUIForDifferentMonthSameYear(startDate, startMonth, endDate, endMonth, endYear)
            }
            else -> {
                dateRangeUIForSameMonthSameYear(startDate, endDate, endMonth, endYear)
            }
        }
    }

    private fun dateRangeUIForSameMonthSameYear(
        startDate: String,
        endDate: String,
        endMonth: String,
        endYear: String,
    ): StringBuilder {
        return StringBuilder(startDate)
            .append(SPACE)
            .append(" - ")
            .append(endDate)
            .append(SPACE)
            .append(endMonth)
            .append(", $endYear")
    }

    private fun dateRangeUIForDifferentMonthSameYear(
        startDate: String,
        startMonth: String,
        endDate: String,
        endMonth: String,
        endYear: String,
    ): StringBuilder {
        return StringBuilder(startDate)
            .append(SPACE)
            .append(startMonth)
            .append(SPACE)
            .append(" - ")
            .append(endDate)
            .append(SPACE)
            .append(endMonth)
            .append(", $endYear")
    }

    private fun dateRangeUIForDifferentYear(
        startDate: String,
        startMonth: String,
        startYear: String,
        endDate: String,
        endMonth: String,
        endYear: String,
    ): StringBuilder {
        return StringBuilder(startDate).append(SPACE).append(startMonth).append(", $startYear").append(SPACE)
            .append(" - ").append(endDate).append(SPACE).append(endMonth).append(", $endYear")
    }

    private fun dateRangeUIForSameDate(
        endDate: String,
        endMonth: String,
        endYear: String,
    ): StringBuilder {
        return StringBuilder(endDate).append(SPACE).append(endMonth).append(", $endYear")
    }

    private fun showAlertErrors(state: CustomerReportsContract.State) {
        if (state.networkError or state.error or state.isAlertVisible) {
            alert = when {
                state.networkError -> view?.snackbar(
                    getString(R.string.home_no_internet_msg),
                    Snackbar.LENGTH_INDEFINITE
                )
                state.isAlertVisible -> {
                    val resourceId = state.messageId ?: R.string.err_default
                    view?.snackbar(getString(resourceId), Snackbar.LENGTH_INDEFINITE)
                }
                else -> view?.snackbar(getString(R.string.err_default), Snackbar.LENGTH_INDEFINITE)
            }
            alert?.show()
        } else {
            alert?.dismiss()
        }
    }

    private fun showTransactionForThisMonth() {
        val startDate = CustomerUtils.getStartDateOfThisMonth()
        val endDate = CommonUtils.currentDateTime()
        onChangeDate.onNext(
            CustomerReportsContract.SelectedDate(
                startDate,
                endDate,
                THIS_MONTH
            )
        )
    }

    private fun showTransactionForLastMonth() {
        val startDate = CustomerUtils.getStartDateOfLastMonth()
        val endDate = CommonUtils.currentDateTime().withTimeAtStartOfDay()
            .withDayOfMonth(1).minusMillis(1)
        onChangeDate.onNext(
            CustomerReportsContract.SelectedDate(
                startDate,
                endDate,
                LAST_MONTH
            )
        )
    }

    private fun showTransactionForLastThreeMonth() {
        val startDate = CustomerUtils.getStartDateOfThreeMonthBefore()
        val endDate = CommonUtils.currentDateTime()
        onChangeDate.onNext(
            CustomerReportsContract.SelectedDate(
                startDate,
                endDate,
                LAST_THREE_MONTHS
            )
        )
    }

    private fun showTransactionForLastSixMonth() {
        val startDate = CustomerUtils.getStartDateOfSixMonthBefore()
        val endDate = CommonUtils.currentDateTime()
        onChangeDate.onNext(
            CustomerReportsContract.SelectedDate(
                startDate,
                endDate,
                LAST_SIX_MONTHS
            )
        )
    }

    private fun showTransactionForOverall() {
        getAllTransactionPublishSubject.onNext(OVERALL)
    }

    private fun showTransactionForLastWeek() {
        val startDate = CustomerUtils.getStartDateOfWeekBefore()
        val endDate = CommonUtils.currentDateTime()
        onChangeDate.onNext(
            CustomerReportsContract.SelectedDate(
                startDate,
                endDate,
                LAST_WEEK
            )
        )
    }

    private fun requestStoragePermissionAndDownloadReport(downloadReport: Boolean) {
        if (ContextCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.WRITE_EXTERNAL_STORAGE
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            customerEventTracker.get().trackEvents(
                CustomerEventTracker.VIEW_STORAGE_PERMISSION,
                screen = CustomerEventTracker.CUSTOMER_REPORTS_SCREEN,
                type = PropertyValue.STORAGE
            )
        }

        requestStoragePermission(
            activity as AppCompatActivity,
            object : IPermissionListener {
                override fun onPermissionGrantedFirstTime() {
                    customerEventTracker.get().trackRuntimePermission(
                        screen = CustomerEventTracker.CUSTOMER_REPORTS_SCREEN,
                        type = PropertyValue.STORAGE,
                        granted = true
                    )
                }

                override fun onPermissionGranted() {
                    job = viewLifecycleOwner.lifecycleScope.launch {
                        withContext(dispatcherProvider.get().main()) {
                            delay(300)
                            val state = getCurrentState()
                            if (downloadReport) {
                                customerEventTracker.get().trackDateDownLoad(
                                    screen = CustomerEventTracker.CUSTOMER_REPORTS_SCREEN,
                                    relation = PropertyValue.CUSTOMER,
                                    accountId = state.customerId,
                                    value = getValueFromSelectedMode(state.selectedMode),
                                    dueAmount = state.balanceForSelectedDuration,
                                    collectionAdopted = state.isCollectionAdopted,
                                    dateRange = mutableListOf(
                                        DateTimeUtils.formatDateOnly(state.startDate),
                                        DateTimeUtils.formatDateOnly(state.endDate)
                                    )
                                )
                                pushIntent(
                                    CustomerReportsContract.Intent
                                        .DownloadReport(
                                            state.startDate,
                                            state.endDate
                                        )
                                )
                            } else {
                                customerEventTracker.get().trackSendReport(
                                    screen = CustomerEventTracker.CUSTOMER_REPORTS_SCREEN,
                                    relation = PropertyValue.CUSTOMER,
                                    accountId = state.customerId,
                                    value = getValueFromSelectedMode(state.selectedMode),
                                    dueAmount = state.balanceForSelectedDuration,
                                    collectionAdopted = state.isCollectionAdopted,
                                    dateRange = mutableListOf(
                                        DateTimeUtils.formatDateOnly(state.startDate),
                                        DateTimeUtils.formatDateOnly(state.endDate)
                                    )
                                )
                                pushIntent(CustomerReportsContract.Intent.ShareReportClicked)
                            }
                        }
                    }
                }

                override fun onPermissionDenied() {
                    customerEventTracker.get().trackRuntimePermission(
                        screen = CustomerEventTracker.CUSTOMER_REPORTS_SCREEN,
                        type = PropertyValue.STORAGE,
                        granted = false
                    )
                }

                override fun onPermissionPermanentlyDenied() {}
            }
        )
    }

    internal fun getValueFromSelectedMode(selectedMode: CustomerReportsContract.SelectedDateMode): String {
        return when (selectedMode) {
            CUSTOM_DATE -> {
                CustomerEventTracker.DATE_RANGE
            }
            THIS_MONTH -> {
                CustomerEventTracker.THIS_MONTH
            }
            LAST_WEEK -> {
                CustomerEventTracker.LAST_SEVEN_DAYS
            }
            LAST_ZERO_BALANCE -> {
                CustomerEventTracker.LAST_ZERO_BALANCE
            }
            LAST_MONTH -> {
                CustomerEventTracker.LAST_MONTH
            }
            LAST_THREE_MONTHS -> {
                CustomerEventTracker.LAST_THREE_MONTHS
            }
            LAST_SIX_MONTHS -> {
                CustomerEventTracker.LAST_SIX_MONTHS
            }
            OVERALL -> {
                CustomerEventTracker.OVERALL
            }
            else -> {
                CustomerEventTracker.LAST_ZERO_BALANCE
            }
        }
    }

    private fun showDateRangeEducation() {
        try {
            viewLifecycleOwner
        } catch (e: Exception) {
            return
        }

        activity?.runOnUiThread {
            customerEventTracker.get().trackInAppNotificationDisplayed(
                screen = CustomerEventTracker.CUSTOMER_REPORT_SCREEN,
                type = CustomerEventTracker.REPORT_DATE,
                accountId = getCurrentState().customerId
            )

            lifecycleScope.launch {
                localInAppNotificationHandler.get()
                    .generateTapTarget(
                        weakScreen = WeakReference(requireActivity()),
                        tapTarget = TapTargetLocal(
                            screenName = label,
                            targetView = WeakReference(binding.educationContainer),
                            title = getString(R.string.date_filters),
                            titleTextSize = 16f,
                            subtitle = getString(R.string.drag_and_select_dates),
                            listener = { _, state ->
                                when (state) {
                                    MaterialTapTargetPrompt.STATE_FOCAL_PRESSED -> {
                                        customerEventTracker.get().trackInAppNotificationClicked(
                                            screen = CustomerEventTracker.CUSTOMER_REPORT_SCREEN,
                                            type = CustomerEventTracker.REPORT_DATE,
                                            accountId = getCurrentState().customerId,
                                            focalArea = true
                                        )
                                        pushIntent(
                                            CustomerReportsContract.Intent.RxPreferenceBoolean(
                                                RxSharedPrefValues.IS_DATE_RANGE_EDUCATION_SHOWN,
                                                true,
                                                Scope.Individual
                                            )
                                        )
                                    }
                                    MaterialTapTargetPrompt.STATE_NON_FOCAL_PRESSED -> {
                                        customerEventTracker.get().trackInAppNotificationClicked(
                                            screen = CustomerEventTracker.CUSTOMER_REPORT_SCREEN,
                                            type = CustomerEventTracker.REPORT_DATE,
                                            accountId = getCurrentState().customerId,
                                            focalArea = false
                                        )
                                        pushIntent(
                                            CustomerReportsContract.Intent.RxPreferenceBoolean(
                                                RxSharedPrefValues.IS_DATE_RANGE_EDUCATION_SHOWN,
                                                true,
                                                Scope.Individual
                                            )
                                        )
                                    }
                                    MaterialTapTargetPrompt.STATE_BACK_BUTTON_PRESSED -> {
                                        customerEventTracker.get().trackInAppNotificationCleared(
                                            screen = CustomerEventTracker.CUSTOMER_REPORT_SCREEN,
                                            type = CustomerEventTracker.REPORT_DATE,
                                            accountId = getCurrentState().customerId
                                        )
                                    }
                                }
                            }
                        )
                    )
            }
        }
    }

    override fun onDestroyView() {
        alert?.dismiss()
        job?.cancel()
        customerReportsController.adapter.unregisterAdapterDataObserver(dataObserver)
        super.onDestroyView()
    }

    @UiThread
    private fun goToLogin() {
        legacyNavigator.goToLoginScreenForAuthFailure(requireContext())
    }

    @UiThread
    private fun goBack() {
        requireActivity().finish()
    }

    private fun shareReport(intent: Intent) {
        requireActivity().startActivity(intent)
    }

    private fun trackPreviewEvent(noResult: Boolean) {
        val state = getCurrentState()
        customerEventTracker.get().trackDatePreviewLoad(
            screen = CustomerEventTracker.CUSTOMER_REPORTS_SCREEN,
            relation = PropertyValue.CUSTOMER,
            accountId = state.customerId,
            value = getValueFromSelectedMode(state.selectedMode),
            dateRange = mutableListOf(
                DateTimeUtils.formatDateOnly(state.startDate),
                DateTimeUtils.formatDateOnly(state.endDate)
            ),
            noResult = noResult
        )
    }

    override fun handleViewEvent(event: CustomerReportsContract.ViewEvent) {
        when (event) {
            is CustomerReportsContract.ViewEvent.GoToLogin -> goToLogin()
            is CustomerReportsContract.ViewEvent.GoBack -> goBack()
            is CustomerReportsContract.ViewEvent.ShareReport -> shareReport(event.intent)
            is CustomerReportsContract.ViewEvent.TrackPreviewEvent -> trackPreviewEvent(event.noResult)
            is CustomerReportsContract.ViewEvent.ReportGenerationFailed -> showErrorSnackBar(event.isInternetIssue)
            CustomerReportsContract.ViewEvent.DownloadedAlert -> {
                view?.snackbar(getString(R.string.download_complete), Snackbar.LENGTH_SHORT)?.show()
            }
        }
    }

    private fun showErrorSnackBar(internetIssue: Boolean) {
        val stringId = if (internetIssue) R.string.no_internet_msg else R.string.report_generation_failed
        view?.snackbar(getString(stringId), Snackbar.LENGTH_SHORT)?.show()
    }
}
