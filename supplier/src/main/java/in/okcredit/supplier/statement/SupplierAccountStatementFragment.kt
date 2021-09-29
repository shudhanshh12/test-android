package `in`.okcredit.supplier.statement

import `in`.okcredit.analytics.*
import `in`.okcredit.shared.base.BaseFragment
import `in`.okcredit.shared.base.UserIntent
import `in`.okcredit.shared.utils.addTo
import `in`.okcredit.supplier.R
import `in`.okcredit.supplier.databinding.SupplierAccountStatemntFragmentBinding
import `in`.okcredit.supplier.statement.SupplierAccountStatementContract.Intent
import `in`.okcredit.supplier.statement.SupplierAccountStatementContract.State
import `in`.okcredit.supplier.statement.SupplierAccountStatementContract.ViewEvent
import `in`.okcredit.supplier.statement.views.SupplierTransactionView
import android.Manifest
import android.app.Activity
import android.content.pm.PackageManager
import android.graphics.Color
import android.graphics.Rect
import android.graphics.drawable.AnimatedVectorDrawable
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.PopupWindow
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.vectordrawable.graphics.drawable.Animatable2Compat
import androidx.vectordrawable.graphics.drawable.AnimatedVectorDrawableCompat
import com.google.android.material.snackbar.Snackbar
import com.karumi.dexter.Dexter
import com.karumi.dexter.PermissionToken
import com.karumi.dexter.listener.PermissionDeniedResponse
import com.karumi.dexter.listener.PermissionGrantedResponse
import com.karumi.dexter.listener.PermissionRequest
import com.karumi.dexter.listener.single.PermissionListener
import dagger.Lazy
import io.reactivex.Completable
import io.reactivex.Observable
import io.reactivex.subjects.PublishSubject
import merchant.okcredit.accounting.analytics.AccountingEventTracker
import merchant.okcredit.accounting.databinding.TransactionFilterLayoutBinding
import merchant.okcredit.accounting.views.DateRangePickerDialog
import merchant.okcredit.accounting.views.LoadMoreView
import org.joda.time.DateTime
import tech.okcredit.android.base.extensions.getColorFromAttr
import tech.okcredit.android.base.extensions.snackbar
import tech.okcredit.android.base.extensions.viewLifecycleScoped
import tech.okcredit.android.base.utils.DateTimeUtils
import tech.okcredit.android.base.utils.DimensionUtil
import tech.okcredit.app_contract.LegacyNavigator
import java.lang.ref.WeakReference
import java.util.concurrent.TimeUnit
import javax.inject.Inject

class SupplierAccountStatementFragment :
    BaseFragment<State, ViewEvent, Intent>(
        "SupplierAccountStatement",
        R.layout.supplier_account_statemnt_fragment
    ),
    SupplierTransactionView.Listener,
    LoadMoreView.Listener {

    private val onChangeDate: PublishSubject<Pair<DateTime, DateTime>> = PublishSubject.create()
    private val loadMorePublicSubject: PublishSubject<Unit> = PublishSubject.create()
    private val selectOnlineTxnFilterPublishSubject: PublishSubject<Boolean> = PublishSubject.create()

    private val supplierAccountStatementController: SupplierAccountStatementController =
        SupplierAccountStatementController(this, this)

    @Inject
    internal lateinit var legacyNavigator: Lazy<LegacyNavigator>

    private var popupWindow: PopupWindow? = null
    private var snackbar: Snackbar? = null

    @Inject
    internal lateinit var accountingEventTracker: Lazy<AccountingEventTracker>

    private val binding: SupplierAccountStatemntFragmentBinding by viewLifecycleScoped(
        SupplierAccountStatemntFragmentBinding::bind
    )

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.profileName.text = getString(R.string.supplier_statement)
        binding.menuOverflow.setOnClickListener { showFilterPopup() }

        binding.recyclerView.layoutManager = LinearLayoutManager(context)
        binding.recyclerView.adapter = supplierAccountStatementController.adapter
        binding.recyclerView.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)
                if (dy > 0) {
                    binding.dateContainer.visibility = View.GONE
                    binding.btnDownload.visibility = View.GONE
                } else if (dy < 0 || dy == 0) {
                    binding.dateContainer.visibility = View.VISIBLE
                    setDownloadButtonVisibility(getCurrentState())
                }
            }
        })

        binding.dateContainer.setOnClickListener { showDatePickerDialog() }

        binding.btnDownload.setOnClickListener {
            if (isStateInitialized().not()) return@setOnClickListener
            val state = getCurrentState()
            if (state.isDownloading.not()) {
                downloadAccountStatement(state.startDate, state.endDate)
            }
        }
    }

    override fun onPause() {
        super.onPause()
        clearVectorDrawableAnimationCallbacks(binding.ivDownloading)
    }

    private fun showDatePickerDialog() {
        accountingEventTracker.get()
            .dateContainerEvent(
                AccountingEventTracker.SUPPLIER_STATEMENT_SCREEN,
                AccountingEventTracker.DATE_RANGE,
                PropertyValue.SUPPLIER
            )

        DateRangePickerDialog.show(
            requireContext(),
            getCurrentState().startDate,
            getCurrentState().endDate,
            object : DateRangePickerDialog.Listener {
                override fun onDone(startDate: DateTime, endDate: DateTime) {
                    onChangeDate.onNext(startDate to endDate)
                }
            }
        ).show()
    }

    override fun onBackPressed(): Boolean {
        if (popupWindow?.isShowing == true) {
            popupWindow?.dismiss()
            return true
        }

        return super.onBackPressed()
    }

    override fun userIntents(): Observable<UserIntent> {
        return Observable.mergeArray(
            // load intent
            Observable.just(Intent.ObserveWorkerStatus(WeakReference(viewLifecycleOwner))),
            loadMorePublicSubject
                .throttleFirst(300, TimeUnit.MILLISECONDS)
                .map { Intent.LoadOldTransactions },

            onChangeDate
                .throttleFirst(300, TimeUnit.MILLISECONDS)
                .map { Intent.ChangeDateRange(it.first, it.second) },

            selectOnlineTxnFilterPublishSubject
                .throttleFirst(300, TimeUnit.MILLISECONDS)
                .map { Intent.SelectOnlineTransactions(it) }
        )
    }

    override fun render(state: State) {
        supplierAccountStatementController.setData(state.statementModels)

        if (DateTimeUtils.formatDateOnly(state.startDate) == DateTimeUtils.formatDateOnly(state.endDate)) {
            binding.dateRange.text = DateTimeUtils.formatTx(state.startDate, requireContext())
        } else {
            binding.dateRange.text = getString(
                R.string.date_range_placeholder,
                DateTimeUtils.formatDateOnly(state.startDate).toString(),
                DateTimeUtils.formatDateOnly(state.endDate).toString()
            )
        }

        if (state.statementModels.contains(AccountStatementModel.NetworkError) || state.error) {
            val stringId = if (state.statementModels.contains(AccountStatementModel.NetworkError)) {
                R.string.home_no_internet_msg
            } else {
                R.string.err_default
            }
            snackbar = view?.snackbar(getString(stringId), Snackbar.LENGTH_SHORT)
            snackbar?.show()
        } else {
            snackbar?.dismiss()
        }

        setGroupDownloadVisibility(state.showDownloadAlert)
        setDownloadButtonVisibility(state)
        setProgressBarVisibility(state)
    }

    internal fun setDownloadButtonVisibility(state: State) {
        binding.btnDownload.isVisible = state.transactionModels.isNullOrEmpty().not() &&
            state.statementModels.contains(AccountStatementModel.Loading).not() &&
            state.isDownloading.not() &&
            state.showOnlyOnlineTransactions.not()
    }

    private fun setProgressBarVisibility(state: State) {
        if (state.isDownloading) {
            binding.groupDownloading.isVisible = true
            startVectorDrawableAnimation(binding.ivDownloading)
        } else {
            binding.groupDownloading.isVisible = false
            clearVectorDrawableAnimationCallbacks(binding.ivDownloading)
        }
    }

    private fun setGroupDownloadVisibility(showDownloadAlert: Boolean) {
        binding.groupDownloaded.isVisible = showDownloadAlert
    }

    private fun startVectorDrawableAnimation(view: ImageView) {
        val vectorDrawable = AnimatedVectorDrawableCompat.create(requireContext(), R.drawable.ic_animated_download)
        vectorDrawable?.let {
            view.setImageDrawable(vectorDrawable)
            vectorDrawable.start()
            val callback = getVectorDrawableAnimationLooperCallback(vectorDrawable)
            vectorDrawable.registerAnimationCallback(callback)
        }
    }

    private fun getVectorDrawableAnimationLooperCallback(
        animatedVectorDrawableCompat: AnimatedVectorDrawableCompat
    ): Animatable2Compat.AnimationCallback {
        return object : Animatable2Compat.AnimationCallback() {
            override fun onAnimationEnd(drawable: Drawable) {
                animatedVectorDrawableCompat.start()
            }
        }
    }

    private fun clearVectorDrawableAnimationCallbacks(view: ImageView) {
        when (val drawable = view.drawable) {
            is AnimatedVectorDrawableCompat -> drawable.clearAnimationCallbacks()
            is AnimatedVectorDrawable ->
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
                    drawable.clearAnimationCallbacks()
                }
        }
    }

    private fun showFilterPopup() {
        val popupBinding = TransactionFilterLayoutBinding.inflate(LayoutInflater.from(context))
        val popupView = popupBinding.root

        popupView.measure(
            View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED),
            View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED)
        )

        popupWindow = PopupWindow(context)
        popupWindow?.isOutsideTouchable = true
        popupWindow?.contentView = popupView
        popupWindow?.setBackgroundDrawable(ColorDrawable(Color.WHITE))
        popupWindow?.elevation = 10.0f
        popupWindow?.isFocusable = true
        popupWindow?.width = ViewGroup.LayoutParams.WRAP_CONTENT
        popupWindow?.height = ViewGroup.LayoutParams.WRAP_CONTENT

        val popupWidth = popupView.measuredWidth
        val deviceWidth = context?.resources?.displayMetrics?.widthPixels
        val marginEnd = DimensionUtil.dp2px(requireContext(), 16.0f).toInt()

        val xOffset = deviceWidth?.minus(popupWidth + marginEnd) ?: 0

        val rect = Rect()
        binding.menuOverflow.getGlobalVisibleRect(rect)
        val yOffset = rect.bottom.plus(DimensionUtil.dp2px(requireContext(), 8f).toInt())

        popupWindow?.showAtLocation(binding.menuOverflow, Gravity.NO_GRAVITY, xOffset, yOffset)

        val state = getCurrentState()
        if (state.showOnlyOnlineTransactions) {
            popupBinding.apply {
                onlineTxnsContainer.setBackgroundColor(requireContext().getColorFromAttr(R.attr.colorPrimary1))
                onlineTxns.setTextColor(ContextCompat.getColor(requireContext(), R.color.white))
                onlineTxnsIcon.setColorFilter(ContextCompat.getColor(requireContext(), R.color.white))
                allTxnsContainer.setBackgroundResource(R.color.white)
                allTxns.setTextColor(ContextCompat.getColor(requireContext(), R.color.grey800))
                allTxnsIcon.setColorFilter(requireContext().getColorFromAttr(R.attr.colorPrimary1))
                accountingEventTracker.get().onlineTransactionEvent(
                    AccountingEventTracker.SUPPLIER_STATEMENT_SCREEN,
                    PropertyValue.ONLINE_TRANSACTIONS,
                    PropertyValue.SUPPLIER
                )
            }
        } else {
            popupBinding.apply {
                onlineTxnsContainer.setBackgroundResource(R.color.white)
                onlineTxns.setTextColor(ContextCompat.getColor(requireContext(), R.color.grey800))
                onlineTxnsIcon.setColorFilter(requireContext().getColorFromAttr(R.attr.colorPrimary1))
                allTxnsContainer.setBackgroundColor(requireContext().getColorFromAttr(R.attr.colorPrimary1))
                allTxns.setTextColor(ContextCompat.getColor(requireContext(), R.color.white))
                allTxnsIcon.setColorFilter(ContextCompat.getColor(requireContext(), R.color.white))
                accountingEventTracker.get().allTransactionEvent(
                    AccountingEventTracker.SUPPLIER_STATEMENT_SCREEN,
                    PropertyValue.ALL_TRANSACTIONS,
                    PropertyValue.SUPPLIER
                )
            }
        }

        popupBinding.allTxnsContainer.setOnClickListener {
            selectOnlineTxnFilterPublishSubject.onNext(false)
            accountingEventTracker.get().allTransactionEvent(
                AccountingEventTracker.SUPPLIER_STATEMENT_SCREEN,
                PropertyValue.ALL_TRANSACTIONS,
                PropertyValue.SUPPLIER
            )
            popupWindow?.dismiss()
        }

        popupBinding.onlineTxnsContainer.setOnClickListener {
            accountingEventTracker.get().onlineTransactionEvent(
                AccountingEventTracker.SUPPLIER_STATEMENT_SCREEN,
                PropertyValue.ONLINE_TRANSACTIONS,
                PropertyValue.SUPPLIER
            )
            selectOnlineTxnFilterPublishSubject.onNext(true)
            popupWindow?.dismiss()
        }
    }

    override fun onLoadMoreClicked() {
        loadMorePublicSubject.onNext(Unit)
    }

    override fun onTransactionClicked(transaction: `in`.okcredit.merchant.suppliercredit.Transaction) {
        val type = if (transaction.payment) {
            PropertyValue.PAYMENT
        } else if (!transaction.payment) {
            PropertyValue.CREDIT
        } else {
            "na"
        }

        val status = if (transaction.deleted) {
            PropertyValue.DELETED
        } else if (!transaction.deleted) {
            PropertyValue.EDITED
        } else if (transaction.collectionId.isNullOrBlank().not()) {
            PropertyValue.ONLINE_PAYMENT
        } else {
            "na"
        }

        accountingEventTracker.get().trackViewTransaction(
            screen = AccountingEventTracker.SUPPLIER_STATEMENT_SCREEN,
            relation = PropertyValue.SUPPLIER,
            mobile = null,
            accountId = transaction.id,
            type = type,
            status = status
        )
        legacyNavigator.get().goToSupplierTransactionScreen(requireActivity(), transaction.id)
    }

    override fun handleViewEvent(event: ViewEvent) {
    }

    override fun loadIntent(): UserIntent? {
        return Intent.Load
    }

    private fun downloadAccountStatement(startDate: DateTime, endDate: DateTime) {
        var isPermissionGranted = false
        if (context?.checkCallingOrSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
            isPermissionGranted = true
        }
        val finalIsPermissionGranted = isPermissionGranted
        Dexter.withActivity(context as Activity)
            .withPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE)
            .withListener(object : PermissionListener {
                override fun onPermissionGranted(response: PermissionGrantedResponse) {
                    if (!finalIsPermissionGranted) {
                        trackPermissionGrantedOrDenied(AnalyticsEvents.PERMISSION_ACCEPT)
                        pushIntentWithDelay(Intent.DownloadStatement(startDate, endDate))
                    } else {
                        pushIntent(Intent.DownloadStatement(startDate, endDate))
                    }
                }

                override fun onPermissionDenied(response: PermissionDeniedResponse) {
                    trackPermissionGrantedOrDenied(AnalyticsEvents.PERMISSION_DENIED)
                }

                override fun onPermissionRationaleShouldBeShown(req: PermissionRequest, token: PermissionToken?) {
                    token?.continuePermissionRequest()
                }
            }).check()
    }

    internal fun pushIntentWithDelay(intent: Intent) {
        Completable.timer(100, TimeUnit.MILLISECONDS)
            .observeOn(schedulerProvider.get().ui())
            .subscribe { pushIntent(intent) }
            .addTo(autoDisposable)
    }

    internal fun trackPermissionGrantedOrDenied(eventName: String) {
        Analytics.track(
            eventName,
            EventProperties.create()
                .with(PropertyKey.SCREEN, label)
                .with(PropertyKey.TYPE, "storage")
        )
    }
}
