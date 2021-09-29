package `in`.okcredit.frontend.ui.supplier_transaction_details

import `in`.okcredit.analytics.*
import `in`.okcredit.backend.utils.BitmapUtils
import `in`.okcredit.backend.utils.CurrencyUtil
import `in`.okcredit.collection.contract.CollectionStatus
import `in`.okcredit.fileupload._id.GlideApp
import `in`.okcredit.fileupload.usecase.IImageLoader
import `in`.okcredit.frontend.R
import `in`.okcredit.frontend.R.color
import `in`.okcredit.frontend.databinding.SupplierTransactionFragmentBinding
import `in`.okcredit.frontend.ui.supplier_transaction_details.SupplierTransactionContract.*
import `in`.okcredit.frontend.utils.DimensionUtil
import `in`.okcredit.merchant.contract.Business
import `in`.okcredit.merchant.suppliercredit.Supplier
import `in`.okcredit.merchant.suppliercredit.Transaction
import `in`.okcredit.shared.base.BaseFragment
import `in`.okcredit.shared.base.UserIntent
import `in`.okcredit.shared.utils.ScreenName
import `in`.okcredit.shared.utils.exhaustive
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import android.net.Uri
import android.os.Bundle
import android.text.Html
import android.view.View
import androidx.annotation.ColorRes
import androidx.annotation.UiThread
import androidx.core.content.ContextCompat
import com.amulyakhare.textdrawable.TextDrawable
import com.amulyakhare.textdrawable.util.ColorGenerator
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.load.resource.bitmap.CenterInside
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.RequestOptions
import com.bumptech.glide.request.target.Target
import com.google.android.material.snackbar.Snackbar
import com.google.common.base.Strings
import com.google.firebase.perf.metrics.AddTrace
import com.jakewharton.rxbinding3.view.clicks
import dagger.Lazy
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.subjects.PublishSubject
import kotlinx.android.synthetic.main.supplier_transaction_fragment.*
import merchant.okcredit.accounting.analytics.AccountingEventTracker
import merchant.okcredit.accounting.analytics.AccountingEventTracker.Companion.SOURCE_TXN_PAGE
import merchant.okcredit.accounting.analytics.AccountingEventTracker.Companion.TXN_PAGE_VIEW
import merchant.okcredit.accounting.contract.model.LedgerType
import merchant.okcredit.accounting.ui.customer_support_option_dialog.CustomerSupportOptionDialog
import merchant.okcredit.accounting.utils.AccountingSharedUtils.getWhatsAppMsg
import org.joda.time.DateTime
import tech.okcredit.android.base.ImagePath
import tech.okcredit.android.base.crashlytics.RecordException
import tech.okcredit.android.base.extensions.gone
import tech.okcredit.android.base.extensions.shortToast
import tech.okcredit.android.base.extensions.snackbar
import tech.okcredit.android.base.extensions.viewLifecycleScoped
import tech.okcredit.android.base.extensions.visible
import tech.okcredit.android.base.utils.DateTimeUtils
import tech.okcredit.android.base.utils.ImageCache
import tech.okcredit.android.communication.CommunicationRepository
import tech.okcredit.android.communication.ShareIntentBuilder
import tech.okcredit.android.communication.handlers.IntentHelper
import tech.okcredit.app_contract.LegacyNavigator
import tech.okcredit.base.Traces
import tech.okcredit.userSupport.SupportRepository
import timber.log.Timber
import java.util.concurrent.TimeUnit
import javax.inject.Inject

class SupplierTransactionFragment : BaseFragment<State, ViewEvent, SupplierTransactionContract.Intent>(
    "SupplierTransactionScreen",
    R.layout.supplier_transaction_fragment
) {

    private val syncNow: PublishSubject<Unit> = PublishSubject.create()
    private val showAlert: PublishSubject<String> = PublishSubject.create()
    private val btnDeleteSubject: PublishSubject<Unit> = PublishSubject.create()
    private val onKnowMoreClicked: PublishSubject<String> = PublishSubject.create()
    private val whatsAppUsPublishSubject: PublishSubject<Boolean> = PublishSubject.create()
    private var status = ""

    @Inject
    internal lateinit var legacyNavigator: Lazy<LegacyNavigator>

    @Inject
    internal lateinit var imageLoader: Lazy<IImageLoader>

    @Inject
    internal lateinit var tracker: Lazy<Tracker>

    @Inject
    internal lateinit var communicationApi: Lazy<CommunicationRepository>

    @Inject
    internal lateinit var imageCache: Lazy<ImageCache>

    private val binding: SupplierTransactionFragmentBinding by viewLifecycleScoped(SupplierTransactionFragmentBinding::bind)

    @Inject
    lateinit var userSupport: Lazy<SupportRepository>

    @Inject
    lateinit var accountingEventTracker: Lazy<AccountingEventTracker>

    private var alert: Snackbar? = null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initListener()
        binding.rootView.setTracker(performanceTracker)
    }

    private fun initListener() {
        binding.contextualHelp.setScreenNameValue(
            ScreenName.SupplierTxnDetailsScreen.value, tracker.get(), userSupport.get(),
            legacyNavigator.get()
        )
        binding.smsContainer.setOnClickListener {
            var channel = ""

            getCurrentState().supplier?.let {
                channel = if (it.registered) {
                    PropertyValue.NOTIFICATION
                } else {
                    PropertyValue.SMS
                }
            }
            tracker.get().trackTransactionDetails(
                eventName = Event.COMMUNICATION_DELIVERED,
                relation = PropertyValue.SUPPLIER,
                type = getTransactionType()!!,
                accountId = getCurrentState().supplier?.id!!,
                channel = channel
            )
        }

        binding.llAddedOnDate.setOnClickListener {
            tracker.get().trackTransactionDetails(
                eventName = Event.ADDED_ON,
                relation = PropertyValue.SUPPLIER,
                type = getTransactionType()!!,
                accountId = getCurrentState().supplier?.id!!
            )
        }

        binding.dateContainer.setOnClickListener {
            tracker.get().trackTransactionDetails(
                eventName = Event.DATE_CLICKED,
                relation = PropertyValue.SUPPLIER,
                type = getTransactionType()!!,
                accountId = getCurrentState().supplier?.id!!
            )
        }

        binding.rlAmount.setOnClickListener {
            tracker.get().trackTransactionDetails(
                eventName = Event.AMOUNT_CLICKED,
                relation = PropertyValue.SUPPLIER,
                type = getTransactionType()!!,
                accountId = getCurrentState().supplier?.id!!
            )
        }

        binding.llCustomerSupport.setOnClickListener {
            getCurrentState().let {
                accountingEventTracker.get().trackCustomerSupportMsgClicked(
                    source = TXN_PAGE_VIEW,
                    txnId = it.collection?.paymentId ?: "",
                    amount = it.transaction?.amount.toString(),
                    relation = LedgerType.SUPPLIER.value.lowercase(),
                    status = status.lowercase(),
                    supportMsg = getWhatsAppMsg(
                        requireContext(),
                        amount = CurrencyUtil.formatV2(it.transaction?.amount ?: 0L),
                        paymentTime = DateTimeUtils.formatLong(it.transaction?.billDate ?: DateTime()),
                        txnId = it.collection?.paymentId ?: "",
                        status = status,
                    ),
                    type = it.supportType.value
                )
                CustomerSupportOptionDialog
                    .newInstance(
                        amount = it.transaction?.amount.toString(),
                        paymentTime = DateTimeUtils.formatLong(it.transaction?.billDate ?: DateTime()),
                        txnId = it.collection?.paymentId ?: "",
                        status = status,
                        accountId = getCurrentState().supplier?.id ?: "",
                        ledgerType = LedgerType.SUPPLIER.value.lowercase(),
                        source = SOURCE_TXN_PAGE,
                    ).show(
                        childFragmentManager,
                        CustomerSupportOptionDialog.TAG
                    )
            }
        }
    }

    override fun loadIntent(): UserIntent {
        return SupplierTransactionContract.Intent.Load
    }

    override fun userIntents(): Observable<UserIntent> {
        return Observable.mergeArray(

            syncNow
                .throttleFirst(300, TimeUnit.MILLISECONDS)
                .map { SupplierTransactionContract.Intent.SyncTransaction },

            showAlert
                .map { SupplierTransactionContract.Intent.ShowAlert(it) },

            btnDeleteSubject
                .throttleFirst(300, TimeUnit.MILLISECONDS)
                .map {
                    tracker.get().trackDeleteTransaction(
                        getCurrentState().supplier?.id,
                        getCurrentState().transaction?.id,
                        PropertyValue.SUPPLIER
                    )
                    SupplierTransactionContract.Intent.Delete
                },

            binding.btnShare.clicks()
                .throttleFirst(300, TimeUnit.MILLISECONDS)
                .map {
                    Analytics.track(
                        AnalyticsEvents.SHARE_TXN,
                        EventProperties
                            .create()
                            .with(PropertyKey.ACCOUNT_ID, getCurrentState().supplier?.id)
                            .with("supplier_id", getCurrentState().supplier?.id)
                            .with("transaction_id", getCurrentState().transaction?.id)
                            .with(PropertyKey.RELATION, PropertyValue.SUPPLIER)
                    )
                    SupplierTransactionContract.Intent.ShareOnWhatsApp
                },
            onKnowMoreClicked.throttleFirst(300, TimeUnit.MILLISECONDS).map {
                SupplierTransactionContract.Intent.OnKnowMoreClicked(it)
            },

            whatsAppUsPublishSubject
                .map {
                    SupplierTransactionContract.Intent.WhatsApp(it)
                }
        )
    }

    @AddTrace(name = Traces.RENDER_SUPPLIER_TRANSACTION)
    override fun render(state: State) {
        Timber.i(" ˆˆˆ accountId 3 =${state.supplier}")

        if (state.transaction != null && state.supplier != null) {

            binding.syncContainer.setOnClickListener {

                if (!state.transaction.syncing) {
                    tracker.get().trackTransactionDetails(
                        eventName = Event.SYNC_CLICKED,
                        relation = PropertyValue.SUPPLIER,
                        type = getTransactionType()!!,
                        accountId = state.supplier.id,
                        status = PropertyValue.FAILED
                    )

                    Analytics.track(
                        AnalyticsEvents.SYNC_TXN,
                        EventProperties
                            .create()
                            .with(PropertyKey.ACCOUNT_ID, state.supplier.id)
                            .with("customer_id", state.supplier.id)
                            .with("transaction_id", state.transaction.id)
                    )
                    syncNow.onNext(Unit)
                } else {
                    tracker.get().trackTransactionDetails(
                        eventName = Event.SYNC_CLICKED,
                        relation = PropertyValue.SUPPLIER,
                        type = getTransactionType()!!,
                        accountId = state.supplier.id,
                        status = PropertyValue.SUCCESS
                    )
                }
            }

            @ColorRes var color = color.tx_payment
            color = if (state.transaction.payment) {
                R.color.tx_payment
            } else {
                R.color.tx_credit
            }

            binding.rupeeSymbol.setTextColor(ContextCompat.getColor(requireContext(), color))
            binding.amount.setTextColor(ContextCompat.getColor(requireContext(), color))

            binding.amount.text = CurrencyUtil.formatV2(state.transaction.amount)
            binding.date.text = getString(R.string.bill_on, DateTimeUtils.formatDateOnly(state.transaction.billDate))
            binding.createdDate.text =
                getString(R.string.added_on_date, DateTimeUtils.formatLong(state.transaction.createTime))

            if (state.transaction.payment) {
                binding.screenTitle.text = getString(R.string.payment_details)
                binding.deleteText.text = getString(R.string.delete_payment)
            } else {
                binding.screenTitle.text = getString(R.string.credit_details)
                binding.deleteText.text = getString(R.string.delete_credit)
            }

            if (Strings.isNullOrEmpty(state.transaction.note)) {
                binding.noteContainer.visibility = View.GONE
                binding.noteDivider.visibility = View.GONE
            } else {
                binding.noteDivider.visibility = View.VISIBLE
                binding.noteDivider.visibility = View.VISIBLE
                binding.note.text = state.transaction.note
            }

            if (!state.transaction.receiptUrl.isNullOrEmpty()) {
                binding.txImageLoader?.visibility = View.VISIBLE

                val requestOptions = RequestOptions().transforms(
                    CenterInside(),
                    RoundedCorners(DimensionUtil.dp2px(requireContext(), 12.0f).toInt())
                )

                Timber.d("Render image started: %s", state.transaction.receiptUrl)
                Glide.with(requireContext())
                    .load(state.transaction.finalReceiptUrl)
                    .apply(requestOptions)
                    .listener(object : RequestListener<Drawable> {
                        override fun onLoadFailed(
                            e: GlideException?,
                            model: Any?,
                            target: Target<Drawable>?,
                            isFirstResource: Boolean,
                        ): Boolean {
                            Timber.d("Render image failed")
                            binding.txImageLoader.visibility = View.GONE
                            return true
                        }

                        override fun onResourceReady(
                            resource: Drawable?,
                            model: Any?,
                            target: Target<Drawable>?,
                            dataSource: DataSource?,
                            isFirstResource: Boolean,
                        ): Boolean {
                            try {
                                viewLifecycleOwner
                            } catch (e: Exception) {
                                RecordException.recordException(Exception("Glide Memory Leak", e))
                                return false
                            }
                            Timber.d("Render image success")
                            binding.txImageLoader.visibility = View.GONE
                            return false
                        }
                    })
                    .into(binding.ivReceipt)
            }

            if (state.transaction.syncing) {
                binding.sycTitle.text = getString(R.string.txn_synced)
                binding.sycTitle.setTextColor(ContextCompat.getColor(requireContext(), R.color.grey900))
                binding.syncLeftIcon.setImageResource(`in`.okcredit.merchant.customer_ui.R.drawable.ic_single_tick)
                binding.syncLeftIcon.setColorFilter(ContextCompat.getColor(requireContext(), R.color.grey900))

                var deliveredMessage = ""

                binding.smsContainer.visibility = View.VISIBLE
                binding.smsDivider.visibility = View.VISIBLE

                // If supplier is registered(using app)
                deliveredMessage = if (state.supplier.registered) {
                    getString(R.string.notification_delivered)
                } else {
                    getString(R.string.sms_delivered)
                }

                binding.smsTitle.text = deliveredMessage
                binding.smsTitle.setTextColor(ContextCompat.getColor(requireContext(), R.color.grey900))
                binding.smsLeftIcon.setImageResource(R.drawable.ic_sms)
                binding.smsLeftIcon.setColorFilter(ContextCompat.getColor(requireContext(), R.color.grey900))
            } else {
                binding.sycTitle.text = getString(R.string.txn_sync_now)
                binding.sycTitle.setTextColor(ContextCompat.getColor(requireContext(), R.color.grey900))
                binding.syncLeftIcon.setImageResource(R.drawable.clock_outline)
                binding.syncLeftIcon.setColorFilter(ContextCompat.getColor(requireContext(), R.color.grey900))
            }

            if (state.collection != null) {
                Timber.e("<<<Txn ${state.collection.status} and ${state.collection.id}")
                online_payment.visibility = View.VISIBLE
                txn_id.text = state.collection.paymentId

                if (state.collection.merchantName.isNullOrEmpty()) {
                    binding.llTxnTo.gone()
                    binding.separatorView.gone()
                } else {
                    binding.llTxnTo.visible()
                    binding.separatorView.visible()
                    binding.upiIdToTitle.text = state.collection.merchantName
                }

                when (state.collection.status) {
                    CollectionStatus.REFUNDED -> {
                        status = "Refunded"
                        ll_payment_failed_status.visibility = View.VISIBLE
                        payment_status_divider.visibility = View.VISIBLE
                        tv_payment_status.text = getString(R.string.payment_refunded)
                        tv_payment_date.text = DateTimeUtils.formatLong(state.collection.create_time)
                        llCustomerSupport.visible()
                    }
                    CollectionStatus.FAILED -> {
                        status = "Failed"
                        ll_payment_failed_status.visibility = View.VISIBLE
                        payment_status_divider.visibility = View.VISIBLE
                        tv_payment_status.text = getString(R.string.payment_failed)
                        tv_payment_date.text = DateTimeUtils.formatLong(state.collection.create_time)
                        llCustomerSupport.visible()
                    }
                    else -> {
                        ll_payment_failed_status.visibility = View.GONE
                        payment_status_divider.visibility = View.GONE
                    }
                }
            } else {
                online_payment.visibility = View.GONE
            }

            transactionDeleteUITweak(state.transaction, state.supplier)

            if (state.supplier.mobile.isNullOrEmpty()) {
                binding.btnShare.visibility = View.GONE
            }

            // show/hide alert
            if (state.error or state.isAlertVisible or state.networkError) {
                alert = when {
                    state.networkError -> view?.snackbar(
                        getString(R.string.home_no_internet_msg),
                        Snackbar.LENGTH_INDEFINITE
                    )
                    state.isAlertVisible -> view?.snackbar(state.alertMessage, Snackbar.LENGTH_INDEFINITE)
                    else -> view?.snackbar(getString(R.string.err_default), Snackbar.LENGTH_INDEFINITE)
                }
                alert?.show()
            } else {
                alert?.dismiss()
            }

            if (state.syncing) binding.pbSyncNow.visibility = View.VISIBLE
            else binding.pbSyncNow.visibility = View.GONE

            val deletedTransaction = state.transaction.deleted
            val addedTransaction = state.transaction.deleted.not()

            when {
                addedTransaction && state.transaction.createdBySupplier -> {
                    binding.addedByContainer.visibility = View.VISIBLE
                    binding.addedByTitle.text =
                        Html.fromHtml(context?.getString(R.string.added_by_v2, state.supplier.name))
                }
                deletedTransaction && state.transaction.deletedBySupplier -> {
                    binding.addedByContainer.visibility = View.VISIBLE
                    binding.addedByTitle.text =
                        Html.fromHtml(context?.getString(R.string.deleted_by_v2, state.supplier.name))
                }
                else -> {
                    binding.addedByContainer.visibility = View.GONE
                }
            }
            if (state.deleteStatus == SupplierTransactionContract.DeleteLayoutStatus.InActive) {
                binding.knowMoreContainer.visibility = View.VISIBLE
                binding.btnDelete.visibility = View.GONE
                binding.deleteTextKnowmore.text = Html.fromHtml(getString(R.string.cannot_delete_supplier_transaction))
                binding.knowMoreContainer.setOnClickListener {
                    state.supplier.id.let {
                        onKnowMoreClicked.onNext(it)
                    }
                }
            } else if (state.deleteStatus == SupplierTransactionContract.DeleteLayoutStatus.Unknown) {
                binding.knowMoreContainer.visibility = View.GONE
                binding.btnDelete.visibility = View.GONE
            } else if (state.deleteStatus == SupplierTransactionContract.DeleteLayoutStatus.Active) {
                binding.knowMoreContainer.visibility = View.GONE
                transactionDeleteUITweak(state.transaction, state.supplier)
            }
        }

        setToolbarInfo(state)
    }

    private fun transactionDeleteUITweak(
        transaction: Transaction,
        supplier: Supplier,
    ) {
        if (transaction.deleted) {
            binding.deleteContainer.visibility = View.VISIBLE
            binding.deleteDivider.visibility = View.VISIBLE
            binding.btnDelete.visibility = View.GONE
            binding.btnShare.visibility = View.GONE
            binding.rupeeSymbol.setTextColor(ContextCompat.getColor(requireContext(), color.grey400))
            binding.amount.setTextColor(ContextCompat.getColor(requireContext(), color.grey400))
            binding.deletedDate.text =
                "${getString(R.string.deleted_on)} : ${DateTimeUtils.formatLong(transaction.deleteTime)}"
        } else {
            binding.deleteContainer.visibility = View.GONE
            binding.deleteDivider.visibility = View.GONE
            binding.btnShare.visibility = View.VISIBLE
            if (transaction.collectionId.isNullOrEmpty().not()) {
                binding.btnDelete.visibility = View.GONE
            } else {
                if (supplier.addTransactionRestricted) {
                    binding.btnDelete.visibility = View.GONE
                } else {
                    binding.btnDelete.visibility = View.VISIBLE
                    binding.btnDelete.setOnClickListener {
                        btnDeleteSubject.onNext(Unit)
                    }
                }
            }
        }

        if (supplier.deleted) {
            binding.btnDelete.visibility = View.GONE
            binding.knowMoreContainer.visibility = View.GONE
        }
    }

    private fun setToolbarInfo(state: State) {
        state.supplier?.let {
            binding.screenTitle.text = state.supplier.name

            val defaultPic = TextDrawable
                .builder()
                .buildRound(
                    state.supplier.name.substring(0, 1).toUpperCase(),
                    ColorGenerator.MATERIAL.getColor(state.supplier.name)
                )

            GlideApp
                .with(this)
                .load(state.supplier.profileImage)
                .circleCrop()
                .placeholder(defaultPic)
                .fallback(defaultPic)
                .into(binding.profileImage)
        }
    }

    private fun shareImageInWhatsApp(bitmap: Bitmap, supplier: Supplier?) {

        val whatsAppIntentBuilder = ShareIntentBuilder(
            shareText = getString(R.string.also_have_business, getCurrentState().referralId),
            phoneNumber = supplier?.mobile,
            imageFrom = ImagePath.ImageUriFromBitMap(
                bitmap,
                requireContext(),
                "reminder_images",
                "imageLocal.jpg"
            )
        )
        communicationApi.get().goToWhatsApp(whatsAppIntentBuilder).map {
            startActivity(it)
        }.doOnError {
            if (it is IntentHelper.NoWhatsAppError)
                showAlert.onNext(getString(R.string.whatsapp_not_installed))
            else
                showAlert.onNext(it.message ?: getString(R.string.default_error_msg))
        }
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe()
    }

    override fun onBackPressed(): Boolean {
        return false
    }

    @UiThread
    fun gotoLogin() {
        activity?.runOnUiThread {
            legacyNavigator.get().goToLoginScreenForAuthFailure(requireActivity())
        }
    }

    fun goToDeletePage(transactionId: String) {
        legacyNavigator.get().goToDeleteSupplierTxnScreen(requireActivity(), transactionId)
    }

    private fun goToWhatsappShare(supplier: Supplier, business: Business, transaction: Transaction) {
        val bitmap =
            BitmapUtils.createSupplierTransactionClusterBitmap(requireContext(), supplier, business, transaction)
        shareImageInWhatsApp(bitmap, supplier)
    }

    private fun goToKnowMoreScreen(it: String, accountType: String) {
        legacyNavigator.get().goToKnowMoreScreen(requireActivity(), it, accountType)
    }

    private fun getTransactionType(): String? {
        return getCurrentState().transaction?.let {
            return@let if (it.payment) {
                PropertyKey.PAYMENT
            } else {
                PropertyKey.CREDIT
            }
        }
    }

    private fun openWhatsApp(okCreditNumber: String) {
        val uri = Uri.parse("whatsapp://send")
            .buildUpon()
            .appendQueryParameter("text", getString(R.string.help_whatsapp_msg))
            .appendQueryParameter("phone", "91$okCreditNumber")
            .build()

        val intent = Intent(Intent.ACTION_VIEW)
        intent.data = uri
        val packageManager = activity?.packageManager
        if (packageManager != null && intent.resolveActivity(packageManager) != null) {
            startActivity(intent)
        } else {
            shortToast(getString(R.string.whatsapp_not_installed))
        }
    }

    private fun goToWhatsAppOptIn() {
        legacyNavigator.get().goToWhatsAppScreen(requireContext())
    }

    override fun handleViewEvent(event: ViewEvent) {
        when (event) {
            is ViewEvent.GoToDeletePage -> goToDeletePage(event.transactionId)

            is ViewEvent.GoToKnowMoreScreen -> goToKnowMoreScreen(
                event.it,
                event.accountType
            )

            is ViewEvent.GoToWhatsAppShare -> goToWhatsappShare(
                event.supplier,
                event.business,
                event.transaction
            )

            is ViewEvent.GoToWhatsApp -> openWhatsApp(event.okCreditNumber)

            is ViewEvent.WhatsAppOptIn -> goToWhatsAppOptIn()

            ViewEvent.GoToHome -> goToHome()
        }.exhaustive
    }

    private fun goToHome() {
        legacyNavigator.get().goToHome(requireActivity())
        activity?.finishAffinity()
    }
}
