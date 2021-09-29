package `in`.okcredit.collection_ui.ui.insights

import `in`.okcredit.analytics.Event
import `in`.okcredit.analytics.PropertiesMap
import `in`.okcredit.analytics.PropertyValue
import `in`.okcredit.analytics.Screen
import `in`.okcredit.analytics.Tracker
import `in`.okcredit.backend._offline.usecase.GetPaymentReminderIntent
import `in`.okcredit.backend.contract.Customer
import `in`.okcredit.backend.utils.CurrencyUtil
import `in`.okcredit.collection.contract.CollectionConstants
import `in`.okcredit.collection.contract.CollectionCustomerProfile
import `in`.okcredit.collection.contract.CreditGraphicalDataProvider.GraphDuration
import `in`.okcredit.collection.contract.CreditGraphicalDataProvider.GraphResponse
import `in`.okcredit.collection_ui.R
import `in`.okcredit.collection_ui.analytics.CollectionTracker
import `in`.okcredit.collection_ui.analytics.CollectionTracker.CollectionPropertyValue
import `in`.okcredit.collection_ui.analytics.CollectionTracker.CollectionScreen
import `in`.okcredit.collection_ui.dialogs.PaymentReminderDialog
import `in`.okcredit.collection_ui.ui.home.add.AddMerchantDestinationDialog
import `in`.okcredit.collection_ui.ui.insights.views.DefaultersItemView
import `in`.okcredit.shared.base.BaseActivity
import `in`.okcredit.shared.base.UserIntent
import `in`.okcredit.web.WebExperiment
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.view.View
import androidx.annotation.UiThread
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.github.mikephil.charting.components.AxisBase
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.components.YAxis
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.PieData
import com.github.mikephil.charting.data.PieDataSet
import com.github.mikephil.charting.data.PieEntry
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter
import com.github.mikephil.charting.formatter.ValueFormatter
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.perf.metrics.AddTrace
import dagger.Lazy
import io.reactivex.Observable
import io.reactivex.subjects.PublishSubject
import kotlinx.android.synthetic.main.merchant_destination_fragment.*
import tech.okcredit.android.base.extensions.longToast
import tech.okcredit.android.base.extensions.setGroupOnClickListener
import tech.okcredit.app_contract.LegacyNavigator
import tech.okcredit.base.Traces
import tech.okcredit.base.permission.IPermissionListener
import tech.okcredit.base.permission.Permission
import timber.log.Timber
import java.util.*
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import kotlin.math.roundToInt

class CollectionInsightsActivity :
    BaseActivity<CollectionInsightsContract.State, CollectionInsightsContract.ViewEvent, CollectionInsightsContract.Intent>(
        "CollectionInsightsScreen"
    ),
    CollectionInsightsContract.Navigator,
    SelectGraphDurationBottomSheetDialog.SelectGraphDurationListener,
    DefaultersItemView.DefaulterCustomersSelectionListener {

    companion object {
        const val MERCHANT_DESTINATION_SCREEN = "Merchant destination screen"
    }

    internal var alert: Snackbar? = null
    private lateinit var merchantDestinationController: CollectionInsightsController

    @Inject
    internal lateinit var legacyNavigator: LegacyNavigator

    @Inject
    internal lateinit var tracker: Tracker

    @Inject
    internal lateinit var collectionTracker: Lazy<CollectionTracker>

    private val merchantSyncIntent: PublishSubject<UserIntent> = PublishSubject.create()
    private val selectGraphDurationPublishSubject: PublishSubject<GraphDuration> =
        PublishSubject.create()
    internal val sendReminderPublishSubject: PublishSubject<String> = PublishSubject.create()
    internal var hidePaymentDialogPublishSubject = PublishSubject.create<Unit>()
    private var currentGraph: GraphResponse? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.merchant_destination_fragment)

        setCollectionInsightsRV()
        setClickListeners()
    }

    private fun setClickListeners() {
        toolbar.setNavigationOnClickListener {
            onBackPressed()
        }

        grp_date_range.setGroupOnClickListener {
            tracker.trackEvents(eventName = Event.CLICKED_ON_DATE_SELECTION)
            val selectGraphDurationBottomSheetDialog = SelectGraphDurationBottomSheetDialog.newInstance()
            if (!selectGraphDurationBottomSheetDialog.isVisible) {
                selectGraphDurationBottomSheetDialog.show(
                    supportFragmentManager,
                    SelectGraphDurationBottomSheetDialog.TAG
                )
                selectGraphDurationBottomSheetDialog.initialise(this@CollectionInsightsActivity)
            }
        }
    }

    private fun setCollectionInsightsRV() {
        merchantDestinationController = CollectionInsightsController(this)
        recycler_view.layoutManager = LinearLayoutManager(this)
        recycler_view.adapter = merchantDestinationController.adapter
    }

    override fun loadIntent(): UserIntent {
        return CollectionInsightsContract.Intent.Load
    }

    override fun userIntents(): Observable<UserIntent> {
        return Observable.mergeArray(

            selectGraphDurationPublishSubject
                .throttleFirst(300, TimeUnit.MILLISECONDS)
                .map {
                    CollectionInsightsContract.Intent.SelectGraphDuration(it)
                },

            sendReminderPublishSubject
                .throttleFirst(300, TimeUnit.MILLISECONDS)
                .map {
                    val reminderStringsObject = GetPaymentReminderIntent.ReminderStringsObject(
                        paymentReminderText = R.string.payment_reminder_text,
                        toMobile = R.string.to_mobile,
                        dueOn = R.string.due_as_on,
                    )
                    CollectionInsightsContract.Intent.SendReminders(it, reminderStringsObject)
                },

            hidePaymentDialogPublishSubject
                .throttleFirst(200, TimeUnit.MILLISECONDS)
                .map { CollectionInsightsContract.Intent.HidePaymentReminderDialog },

            merchantSyncIntent,
        )
    }

    @AddTrace(name = Traces.RENDER_MERCHANT_DESTINATION)
    override fun render(state: CollectionInsightsContract.State) {

        Timber.i("<<RenderStatesFlag ${state.graphResponse}")
        merchantDestinationController.setState(state)

        if (state.collectionMerchantProfile?.payment_address.isNullOrEmpty()) {
            tracker.trackDebug("CollectionInsightsScreen merchant profile is empty")
        }

        showQRInsightsPage(state)

        showAlertMessage(state)
    }

    private fun showAlertMessage(state: CollectionInsightsContract.State) {
        if (state.alertMessage.isNotEmpty()) {
            longToast(state.alertMessage)
        }
    }

    private fun showAlertErrors(networkError: Boolean) {
        if (networkError) {
            longToast(R.string.home_no_internet_msg)
        } else {
            longToast(R.string.err_default)
        }
    }

    private fun showQRInsightsPage(state: CollectionInsightsContract.State) {

        if (state.dueCustomers.isNotEmpty()) {
            tv_defaulters.visibility = View.VISIBLE
            recycler_view.visibility = View.VISIBLE
        } else {
            tv_defaulters.visibility = View.GONE
            recycler_view.visibility = View.GONE
        }

        drawGraphs(state)
    }

    private fun drawGraphs(state: CollectionInsightsContract.State) {
        val graphData = state.graphResponse
        if (tv_date_range.text.isNullOrEmpty() || !Objects.equals(currentGraph, state.graphResponse)) {
            currentGraph = state.graphResponse
            drawBarGraph(graphData)
            if (graphData != null) {
                tv_offline_collection_amount.text = String.format(
                    "${getString(R.string.rupee_symbol)} %s",
                    CurrencyUtil.formatV2(graphData.offlineCollection)
                )
                tv_online_collection_amount.text = String.format(
                    "${getString(R.string.rupee_symbol)} %s",
                    CurrencyUtil.formatV2(graphData.onlineCollection)
                )
                total_credit_amount.text = String.format(
                    "${getString(R.string.rupee_symbol)} %s",
                    CurrencyUtil.formatV2(graphData.givenCreditAmount)
                )
                tv_date_range.text = graphData.selectedDateRange

                drawPieGraph(graphData)
            }
        }
    }

    private fun drawBarGraph(graphData: GraphResponse?) {
        graphData?.barDataSet?.let {
            val barDataSet = it

            if (graphData.givenCreditAmount == 0L) {
                barChart.visibility = View.GONE
                total_credit_title.visibility = View.GONE
                total_credit_amount.visibility = View.GONE
                iv_empty_graph.visibility = View.VISIBLE
                tv_empty_graph_message.visibility = View.VISIBLE
                return
            } else {
                barChart.visibility = View.VISIBLE
                total_credit_title.visibility = View.VISIBLE
                total_credit_amount.visibility = View.VISIBLE
                iv_empty_graph.visibility = View.GONE
                tv_empty_graph_message.visibility = View.GONE
            }

            barDataSet.color = ContextCompat.getColor(this@CollectionInsightsActivity, R.color.red_primary)
            barDataSet.axisDependency = YAxis.AxisDependency.LEFT
            val data = BarData(barDataSet)

            if (graphData.graphDuration == GraphDuration.TODAY ||
                graphData.graphDuration == GraphDuration.YESTERDAY
            ) {
                data.barWidth = 0.2f
            } else if (graphData.graphDuration == GraphDuration.WEEK) {
                data.barWidth = 0.7f
            } else if (graphData.graphDuration == GraphDuration.MONTH) {
                data.barWidth = 0.7f
            }

            barChart.data = data
            barChart.legend.isEnabled = false
            barChart.description.isEnabled = false
            barChart.axisLeft.isEnabled = false

            val xAxis = barChart.xAxis
            xAxis.valueFormatter = IndexAxisValueFormatter(graphData.labelNames)
            xAxis.position = XAxis.XAxisPosition.BOTTOM
            xAxis.setDrawAxisLine(false)
            xAxis.setDrawGridLines(false)
            xAxis.granularity = 1f
            xAxis.labelCount = graphData.labelNames.size

            val yAxisRight = barChart.axisRight
            yAxisRight.valueFormatter = MyAxisValueFormatter(this)

            barChart.animateY(1000)
            barChart.invalidate()
        }
    }

    class MyAxisValueFormatter(private val context: Context) : ValueFormatter() {

        override fun getAxisLabel(value: Float, axis: AxisBase?): String {
            return context.getString(R.string.rupee_symbol) + " " + CurrencyUtil.formatV2(value.toLong() * 100)
        }
    }

    private fun drawPieGraph(graphData: GraphResponse) {
        val onlineCollection = if (graphData.onlineCollection > 0) graphData.onlineCollection.toFloat() / 100 else 0f
        val offlineCollection = if (graphData.offlineCollection > 0) graphData.offlineCollection.toFloat() / 100 else 0f
        val totalCollection = onlineCollection + offlineCollection

        val pieEntryList = mutableListOf<PieEntry>()
        if (onlineCollection > 0 && totalCollection > 0) {
            pieEntryList.add(PieEntry(onlineCollection, ""))
            pieEntryList.add(PieEntry(offlineCollection, ""))
        } else if (onlineCollection == 0F || totalCollection == 0F) {
            pieEntryList.add(PieEntry(0f, ""))
            pieEntryList.add(PieEntry(100f, ""))
        }

        val colorList = mutableListOf(
            ContextCompat.getColor(this, R.color.green_1),
            ContextCompat.getColor(this, R.color.green_lite_1)
        )

        val pieDataSet = PieDataSet(pieEntryList, "")
        pieDataSet.colors = colorList
        val pieData = PieData(pieDataSet)

        pieChart.data = pieData
        pieChart.legend.isEnabled = false
        pieChart.description.isEnabled = false
        pieChart.setDrawEntryLabels(false)
        pieChart.data.setValueTextColor(Color.TRANSPARENT)
        pieChart.holeRadius = 80f
        pieChart.rotation = 180f
        if (totalCollection > 0) {
            val onlinePercentage = ((onlineCollection / totalCollection) * 100.0).roundToInt()
            centerText.text = StringBuilder(onlinePercentage.toString()).append("%")
        } else if (totalCollection == 0F) {
            centerText.text = StringBuilder("0%")
        }
        pieChart.animate()
        pieChart.invalidate()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK && requestCode == CollectionConstants.QR_SCANNER_REQUEST_CODE) {
            val dialogFragment: Fragment? = supportFragmentManager.findFragmentByTag(AddMerchantDestinationDialog.TAG)

            if (dialogFragment != null) {
                data?.let {
                    val upiVpa = it.getStringExtra(CollectionConstants.UPI_ID)
                    val scanMethod = it.getStringExtra(CollectionConstants.METHOD)
                        ?: CollectionPropertyValue.CAMERA
                    (dialogFragment as AddMerchantDestinationDialog).setUpiVpaFromScanner(upiVpa, scanMethod)
                }
            }
        }
    }

    override fun selectGraphDuration(selectedGraphDuration: GraphDuration) {
        selectGraphDurationPublishSubject.onNext(selectedGraphDuration)

        when (selectedGraphDuration) {
            GraphDuration.TODAY -> {
                tracker.trackEvents(eventName = Event.CLICKED_ON_TODAY)
            }
            GraphDuration.YESTERDAY -> {
                tracker.trackEvents(eventName = Event.CLICKED_ON_YESTERDAY)
            }
            GraphDuration.WEEK -> {
                tracker.trackEvents(eventName = Event.CLICKED_ON_LAST_SEVEN_DAYS)
            }
            GraphDuration.MONTH -> {
                tracker.trackEvents(eventName = Event.CLICKED_ON_LAST_THIRTY_DAYS)
            }
        }
    }

    /****************************************************************
     * Navigation
     ****************************************************************/

    @UiThread
    override fun gotoLogin() {
        runOnUiThread {
            legacyNavigator.goToLoginScreenForAuthFailure(this)
        }
    }

    @UiThread
    override fun gotoCollectionTutorialScreen() {
        runOnUiThread {
            legacyNavigator.goToCollectionTutorialScreen(this, Screen.MERCHANT_DESTINATION_SCREEN)
        }
    }

    internal fun gotoKycScreen() {
        legacyNavigator.goWebExperimentScreen(this, WebExperiment.Experiment.KYC.type)
    }

    override fun gotoCollectionTutorialScreenByClearingStack() {
        runOnUiThread {
            legacyNavigator.goToCollectionTutorialScreenByClearingBackStack(
                this,
                Screen.MERCHANT_DESTINATION_SCREEN
            )
        }
    }

    override fun openPaymentReminderIntent(intent: Intent) {
        runOnUiThread {
            startActivity(intent)
        }
    }

    override fun onCustomerSelection(customer: Customer) {
        pushIntent(CollectionInsightsContract.Intent.ShowPaymentReminderDialog(customer))
    }

    override fun openPaymentReminderDialog(
        collectionCustomerProfile: CollectionCustomerProfile,
        customer: Customer,
    ) {
        runOnUiThread {

            collectionTracker.get().trackViewQr(CollectionScreen.INSIGHTS_RELATIONSHIP)

            val fragmentMangaer = this.supportFragmentManager
            var paymentDialogFrag = fragmentMangaer.findFragmentByTag(PaymentReminderDialog.TAG)

            if (paymentDialogFrag != null && paymentDialogFrag is PaymentReminderDialog) {
                paymentDialogFrag.render(customer, collectionCustomerProfile)
                return@runOnUiThread
            }

            val state = getCurrentState()
            paymentDialogFrag = PaymentReminderDialog.newInstance(
                PaymentReminderDialog.PaymentReminderDialogData(
                    customer = customer,
                    collectionCustomerProfile = collectionCustomerProfile,
                    kycStatus = state.kycStatus,
                    kycRiskCategory = state.kycRiskCategory,
                    isLimitReached = state.isLimitReached
                )
            )
            paymentDialogFrag.show(fragmentMangaer, PaymentReminderDialog.TAG)

            paymentDialogFrag.initialize(object : PaymentReminderDialog.CustomerListener {
                override fun addMobileClicked(customer: Customer) {
                    tracker.trackSelectProfileV1(
                        PropertyValue.CUSTOMER,
                        PropertyValue.CUSTOMER,
                        PropertyValue.MOBILE
                    )
                    legacyNavigator.gotoCustomerProfile(this@CollectionInsightsActivity, customer.id, true)
                }

                override fun onSendReminderClicked(
                    customer: Customer,
                    collectionCustomerProfile: CollectionCustomerProfile,
                ) {
                    sendReminderPublishSubject.onNext(customer.id)
                }

                override fun callIconClicked(customer: Customer) {
                    collectionTracker.get().trackCallRelationShip(
                        screen = CollectionScreen.INSIGHTS_RELATIONSHIP,
                        relation = PropertyValue.CUSTOMER,
                        mobile = customer.mobile,
                        accountId = customer.id
                    )
                    Permission.requestCallPermission(
                        this@CollectionInsightsActivity,
                        object : IPermissionListener {
                            override fun onPermissionGrantedFirstTime() {
                                tracker.trackRuntimePermission(CollectionScreen.INSIGHTS_RELATIONSHIP, Event.CALL, true)
                            }

                            override fun onPermissionGranted() {
                                val intent = Intent(Intent.ACTION_CALL)
                                intent.data = Uri.parse(getString(R.string.call_template, customer.mobile))
                                startActivity(intent)
                            }

                            override fun onPermissionDenied() {
                                tracker.trackRuntimePermission(
                                    CollectionScreen.INSIGHTS_RELATIONSHIP,
                                    Event.CALL,
                                    false
                                )
                            }
                        }
                    )
                }

                override fun onDismiss() {
                    collectionTracker.get().trackCollectionQRClosed(
                        screen = CollectionScreen.INSIGHTS_RELATIONSHIP,
                        relation = PropertyValue.CUSTOMER,
                        accountId = customer.id
                    )
                    hidePaymentDialogPublishSubject.onNext(Unit)
                }

                override fun onDismissKyc(eventName: String) {
                    trackKycEvents(eventName)
                }

                override fun onStartKyc(eventName: String) {
                    gotoKycScreen()
                    trackKycEvents(eventName)
                }
            })
        }
    }

    internal fun trackKycEvents(eventName: String) {
        val state = getCurrentState()
        collectionTracker.get()
            .trackEvents(
                eventName = eventName,
                screen = Screen.MERCHANT_DESTINATION_SCREEN,
                propertiesMap = PropertiesMap.create()
                    .add("merchant_id", state.business?.id ?: "")
                    .add("kyc_status", state.kycStatus.value.lowercase())
                    .add("risk_type", state.kycRiskCategory.value.lowercase())
            )
    }

    override fun handleViewEvent(event: CollectionInsightsContract.ViewEvent) {
        when (event) {
            is CollectionInsightsContract.ViewEvent.Error -> showAlertErrors(event.network)
        }
    }
}
