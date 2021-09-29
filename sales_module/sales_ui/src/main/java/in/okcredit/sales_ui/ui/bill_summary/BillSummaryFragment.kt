package `in`.okcredit.sales_ui.ui.bill_summary

import `in`.okcredit.analytics.Event
import `in`.okcredit.analytics.PropertiesMap
import `in`.okcredit.analytics.PropertyValue
import `in`.okcredit.sales_sdk.models.Models
import `in`.okcredit.sales_ui.BuildConfig
import `in`.okcredit.sales_ui.R
import `in`.okcredit.sales_ui.analytics.SalesAnalytics
import `in`.okcredit.sales_ui.databinding.BillSummaryScreenBinding
import `in`.okcredit.sales_ui.dialogs.DeleteSaleBottomSheetDialog
import `in`.okcredit.sales_ui.ui.billing_name.BillingNameBottomSheetDialog
import `in`.okcredit.sales_ui.utils.SalesUtil
import `in`.okcredit.shared.base.BaseFragment
import `in`.okcredit.shared.base.UserIntent
import `in`.okcredit.shared.utils.CommonUtils
import android.app.DatePickerDialog
import android.graphics.Bitmap
import android.graphics.Canvas
import android.os.Bundle
import android.os.Environment
import android.print.PdfView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.WebChromeClient
import android.webkit.WebResourceError
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.DatePicker
import androidx.core.content.FileProvider
import androidx.navigation.fragment.NavHostFragment.findNavController
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.subjects.PublishSubject
import org.joda.time.DateTime
import tech.okcredit.android.base.ImagePath
import tech.okcredit.android.base.extensions.setBooleanVisibility
import tech.okcredit.android.base.extensions.shortToast
import tech.okcredit.android.base.utils.DateTimeUtils
import tech.okcredit.android.communication.CommunicationRepository
import tech.okcredit.android.communication.ShareIntentBuilder
import tech.okcredit.android.communication.handlers.IntentHelper
import tech.okcredit.app_contract.LegacyNavigator
import timber.log.Timber
import java.io.File
import java.util.*
import java.util.concurrent.TimeUnit
import javax.inject.Inject

class BillSummaryFragment :
    BaseFragment<BillSummaryContract.State, BillSummaryContract.ViewEvent, BillSummaryContract.Intent>("BillSummaryScreen"),
    WebInterface.Listener,
    BillingNameBottomSheetDialog.Listener,
    DeleteSaleBottomSheetDialog.DeleteDialogListener,
    DatePickerDialog.OnDateSetListener {

    internal lateinit var binding: BillSummaryScreenBinding

    @Inject
    internal lateinit var legacyNavigator: LegacyNavigator

    @Inject
    internal lateinit var communicationApi: CommunicationRepository

    @Inject
    internal lateinit var salesAnalytics: SalesAnalytics

    internal var isDataLoaded = false

    private val updateSaleSubject: PublishSubject<Models.UpdateSaleItemRequest> = PublishSubject.create()

    private var webUrl = BuildConfig.BILLING_WEB_URL
    private var datePickerDialog: DatePickerDialog? = null
    private val showDeleteDialog: PublishSubject<Unit> = PublishSubject.create()
    private val deleteSale: PublishSubject<String> = PublishSubject.create()
    private val showLoader: PublishSubject<Boolean> = PublishSubject.create()
    private val goToMerchantProfile: PublishSubject<Unit> = PublishSubject.create()
    private val showErrorSubject: PublishSubject<String> = PublishSubject.create()
    private val shareSubject: PublishSubject<Models.Sale> = PublishSubject.create()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = BillSummaryScreenBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onBackPressed(): Boolean {
        findNavController(this).popBackStack(R.id.salesOnCashScreen, false)
        return true
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initWebView()
        binding.toolbar.setNavigationOnClickListener {
            onBackPressed()
        }
        binding.share.setOnClickListener {
            if (isDataLoaded.not() || getCurrentState().sale == null) {
                showErrorSubject.onNext("Data Not Ready")
                return@setOnClickListener
            }
            shareSubject.onNext(getCurrentState().sale!!)
        }
        binding.delete.setOnClickListener {
            showDeleteDialog.onNext(Unit)
        }
    }

    private fun initWebView() {
        binding.webView.apply {
            settings.apply {
                javaScriptEnabled = true
                domStorageEnabled = true
                addJavascriptInterface(WebInterface(this@BillSummaryFragment), WebInterface.JAVASCRIPT_WEB_INTERFACE)
            }

            webViewClient = object : WebViewClient() {
                override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
                    super.onPageStarted(view, url, favicon)
                    isDataLoaded = false
                    showLoader.onNext(true)
                }

                override fun onPageFinished(view: WebView?, url: String?) {
                    super.onPageFinished(view, url)
                    WebInterface.injectJavascript(view)
                    isDataLoaded = true
                    showLoader.onNext(false)
                }

                override fun onReceivedError(view: WebView?, request: WebResourceRequest?, error: WebResourceError?) {
                    super.onReceivedError(view, request, error)
                    showLoader.onNext(false)
                    context?.let {
                        showErrorSubject.onNext(it.getString(R.string.err_loading_page))
                    }
                }
            }
            webChromeClient = object : WebChromeClient() {
            }
        }
    }

    private fun loadWebView(saleId: String, authToken: String?) {
        val id = "?id=$saleId"
        val token = "&auth=$authToken"
        val page = webUrl + id + token
        binding.webView.loadUrl(page)
        Timber.d("Bill WebView $page")
    }

    override fun loadIntent(): UserIntent {
        return BillSummaryContract.Intent.Load
    }

    override fun userIntents(): Observable<UserIntent> {
        return Observable.mergeArray(
            updateSaleSubject.map {
                BillSummaryContract.Intent.UpdateBillingDataIntent(it)
            },
            showDeleteDialog.throttleFirst(300, TimeUnit.MILLISECONDS)
                .map {
                    BillSummaryContract.Intent.ShowDeleteDialogIntent
                },
            deleteSale.throttleFirst(300, TimeUnit.MILLISECONDS)
                .map {
                    BillSummaryContract.Intent.DeleteSaleIntent(it)
                },
            showLoader.map {
                BillSummaryContract.Intent.ShowLoaderIntent(it)
            },
            goToMerchantProfile.throttleFirst(300, TimeUnit.MILLISECONDS)
                .map {
                    BillSummaryContract.Intent.GoToMerchantProfile
                },
            showErrorSubject.throttleFirst(300, TimeUnit.MILLISECONDS)
                .map {
                    BillSummaryContract.Intent.ShowErrorIntent(it)
                },
            shareSubject.throttleFirst(300, TimeUnit.MILLISECONDS)
                .map {
                    BillSummaryContract.Intent.ShareIntent(it)
                }
        )
    }

    override fun render(state: BillSummaryContract.State) {
        binding.loader.setBooleanVisibility(state.isLoading)
        binding.delete.setBooleanVisibility(state.isEditable)
    }

    override fun handleViewEvent(effect: BillSummaryContract.ViewEvent) {
        when (effect) {
            BillSummaryContract.ViewEvent.GoToLoginScreen -> {
                legacyNavigator.goToLoginScreenForAuthFailure(requireContext())
            }
            is BillSummaryContract.ViewEvent.LoadWebView -> {
                loadWebView(effect.saleId, effect.authToken)
            }
            BillSummaryContract.ViewEvent.ReLoadWebView -> {
                loadWebView(getCurrentState().saleId ?: "", getCurrentState().authToken)
            }
            is BillSummaryContract.ViewEvent.ShowError -> {
                context?.shortToast(effect.msg)
            }
            is BillSummaryContract.ViewEvent.ShowDeleteDialog -> {
                showDeleteDialog(effect.saleId)
            }
            BillSummaryContract.ViewEvent.OnDeleted -> {
                onDeleted()
            }
            BillSummaryContract.ViewEvent.GoToMerchantProfileScreen -> {
                legacyNavigator.goToMerchantProfile(requireContext())
            }
            BillSummaryContract.ViewEvent.ShareAsImage -> {
                shareImageInWhatsapp()
            }
            BillSummaryContract.ViewEvent.ShareAsPDF -> {
                createWebPrintJob()
            }
            is BillSummaryContract.ViewEvent.UpdateSale -> {
                Timber.d("Privin UpdateSale : ${effect.sale}")
                binding.webView.evaluateJavascript("javascript:updateSale('${effect.sale}')") {}
            }
        }
    }

    override fun onEditMerchant() {
        goToMerchantProfile.onNext(Unit)
    }

    override fun onEditBillingName(buyerName: String?, buyerMobile: String?) {
        val fragment = BillingNameBottomSheetDialog.newInstance(buyerName, buyerMobile)
        fragment.setListener(this)
        fragment.show(childFragmentManager, BillingNameBottomSheetDialog.TAG)
    }

    override fun openDatePicker(date: DateTime?) {
        activity?.runOnUiThread {
            if (datePickerDialog == null) {
                val today = date ?: CommonUtils.currentDateTime()

                datePickerDialog = DatePickerDialog(
                    requireContext(),
                    this,
                    today.year,
                    today.monthOfYear.minus(1),
                    today.dayOfMonth
                )
                if (datePickerDialog?.datePicker != null) {
                    datePickerDialog?.datePicker?.maxDate = CommonUtils.currentDateTime().millis
                }

                datePickerDialog?.setButton(DatePickerDialog.BUTTON_POSITIVE, getString(R.string.ok), datePickerDialog)
                datePickerDialog?.setButton(
                    DatePickerDialog.BUTTON_NEGATIVE,
                    getString(R.string.cancel),
                    datePickerDialog
                )
                datePickerDialog?.setOnCancelListener {
                    it.dismiss()
                }
            }
            datePickerDialog!!.show()
        }
    }

    override fun onSubmit(name: String, mobile: String) {
        val mask = mutableListOf<String>()
        if (name.isNotEmpty()) {
            mask.add("buyer_name")
        }
        if (mobile.isNotEmpty()) {
            mask.add("buyer_mobile")
        }
        val updateSale = Models.UpdateSale(name, mobile)
        val updateSaleItemRequest = Models.UpdateSaleItemRequest(updateSale, mask)
        updateSaleSubject.onNext(updateSaleItemRequest)
    }

    private fun createWebPrintJob() {
        val path = requireContext().getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS)
            ?.getAbsolutePath()
        val fileName = "cash_sales_bill" + UUID.randomUUID()
        val dir = File(path)
        if (!dir.exists())
            dir.mkdirs()

        val file = File(dir, fileName)

        PdfView.createWebPdfJob(
            requireActivity(),
            binding.webView,
            file,
            "CashSalesBill.pdf",
            object : PdfView.Callback {
                override fun success(path: String) {
                    sharePDFViaWhatsApp(File(path))
                }

                override fun failure() {
                }
            }
        )
    }

    internal fun sharePDFViaWhatsApp(file: File) {
        val f = FileProvider.getUriForFile(
            requireContext(),
            requireContext().getApplicationContext().getPackageName() + ".provider",
            file
        )
        val whatsappIntentBuilder = ShareIntentBuilder(
            uri = f,
            phoneNumber = getCurrentState().sale?.buyerMobile ?: "",
            contentType = "application/pdf"
        )
        shareViaWhatsApp(whatsappIntentBuilder)
    }

    private fun shareImageInWhatsapp() {
        val screenShot = binding.webView
        val bitmap =
            Bitmap.createBitmap(screenShot.width, screenShot.height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        screenShot.draw(canvas)
        val whatsappIntentBuilder = ShareIntentBuilder(
            shareText = getString(
                R.string.share_bill_text,
                SalesUtil.currencyDisplayFormat(getCurrentState().sale!!.amount),
                getCurrentState().businessName ?: ""
            ),
            phoneNumber = getCurrentState().sale?.buyerMobile ?: "",
            imageFrom = ImagePath.ImageUriFromBitMap(
                bitmap,
                requireContext(),
                "cash_sales_bill",
                "imageLocal.jpg"
            )
        )
        shareViaWhatsApp(whatsappIntentBuilder)
    }

    private fun shareViaWhatsApp(shareIntentBuilder: ShareIntentBuilder) {
        salesAnalytics.trackEvents(
            SalesAnalytics.Event.CASH_SALE_TRANSACTION_SHARE,
            screen = SalesAnalytics.PropertyValue.CASH_SALE_TX,
            propertiesMap = PropertiesMap.create()
                .add("Tx_id", getCurrentState().saleId ?: "")
                .add(
                    SalesAnalytics.PropertyKey.FLOW,
                    if (getCurrentState().isEditable) "Tx Detail View" else "Tx Completed"
                )
                .add("Items", getCurrentState().sale?.billedItems?.items?.size ?: "")
                .add(SalesAnalytics.PropertyKey.AMOUNT, getCurrentState().sale?.amount.toString())
                .add(
                    SalesAnalytics.PropertyKey.QUANTITY,
                    getCurrentState().sale?.billedItems?.items?.sumByDouble { it.quantity }.toString()
                )
                .add("customer_name", getCurrentState().sale?.buyerName ?: "")
        )
        communicationApi.goToWhatsApp(shareIntentBuilder).map {
            startActivity(it)
        }.doOnError {
            if (it is IntentHelper.NoWhatsAppError)
                showErrorSubject.onNext(getString(R.string.whatsapp_not_installed))
            else
                showErrorSubject.onNext(getString(R.string.err_default))
        }
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe()
    }

    private fun showDeleteDialog(saleId: String) {
        val deleteFragment = DeleteSaleBottomSheetDialog.newInstance(saleId)
        deleteFragment.setListener(this)
        deleteFragment.show(childFragmentManager, DeleteSaleBottomSheetDialog.TAG)
        deleteFragment.isCancelable = false
    }

    private fun onDeleted() {
        if (isStateInitialized()) {
            salesAnalytics.trackEvents(
                eventName = SalesAnalytics.Event.CASH_SALE_DELETED,
                screen = SalesAnalytics.PropertyValue.CASH_SALE_TX,
                propertiesMap = PropertiesMap.create()
                    .add("Tx id", getCurrentState().sale?.id ?: "")
            )
            activity?.runOnUiThread {
                findNavController(this).popBackStack()
            }
        }
    }

    override fun onDelete(saleId: String) {
        deleteSale.onNext(saleId)
    }

    override fun onCancel() {
        salesAnalytics.trackEvents(
            eventName = SalesAnalytics.Event.DELETE_CASH_SALE_CANCELLED,
            screen = SalesAnalytics.PropertyValue.CASH_SALE_TX
        )
    }

    override fun onDestroyView() {
        binding.webView.destroy()
        super.onDestroyView()
    }

    override fun onDateSet(view: DatePicker?, year: Int, month: Int, dayOfMonth: Int) {
        val calendar = Calendar.getInstance()
        calendar.set(Calendar.YEAR, year)
        calendar.set(Calendar.MONTH, month)
        calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth)

        val newDate = DateTime(calendar.timeInMillis)
        val defaultDate = DateTimeUtils.currentDateTime()
        if (defaultDate.dayOfMonth == newDate.dayOfMonth && defaultDate.monthOfYear == newDate.monthOfYear && defaultDate.year == newDate.year) {
            salesAnalytics.trackEvents(
                eventName = Event.UPDATE_CASH_SALE_DATE,
                screen = PropertyValue.CASH_SALE_TX,
                propertiesMap = PropertiesMap.create()
                    .add("Value", DateTimeUtils.formatDateOnly(newDate))
                    .add("Default", true)
            )
        } else {
            salesAnalytics.trackEvents(
                eventName = Event.UPDATE_CASH_SALE_DATE,
                screen = PropertyValue.CASH_SALE_TX,
                propertiesMap = PropertiesMap.create()
                    .add("Value", DateTimeUtils.formatDateOnly(newDate))
                    .add("Default", false)
            )
        }
        val mask = mutableListOf<String>()
        mask.add("sale_date")
        val updateSale = Models.UpdateSale(saleDate = newDate)
        val updateSaleItemRequest = Models.UpdateSaleItemRequest(updateSale, mask)
        updateSaleSubject.onNext(updateSaleItemRequest)
    }
}
