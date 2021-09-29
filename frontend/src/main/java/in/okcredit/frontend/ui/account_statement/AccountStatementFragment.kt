package `in`.okcredit.frontend.ui.account_statement

import `in`.okcredit.analytics.*
import `in`.okcredit.frontend.R
import `in`.okcredit.frontend.databinding.AccountStatementFragmentBinding
import `in`.okcredit.frontend.ui.account_statement.AccountStatementContract.*
import `in`.okcredit.frontend.ui.account_statement.views.TransactionView
import `in`.okcredit.frontend.utils.DimensionUtil
import `in`.okcredit.shared.base.BaseFragment
import `in`.okcredit.shared.base.UserIntent
import `in`.okcredit.shared.dialogs.DateRangePickerDialog
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
import android.widget.LinearLayout
import android.widget.PopupWindow
import android.widget.TextView
import androidx.annotation.UiThread
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.transition.TransitionManager
import androidx.vectordrawable.graphics.drawable.Animatable2Compat
import androidx.vectordrawable.graphics.drawable.AnimatedVectorDrawableCompat
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.perf.metrics.AddTrace
import com.karumi.dexter.Dexter
import com.karumi.dexter.PermissionToken
import com.karumi.dexter.listener.PermissionDeniedResponse
import com.karumi.dexter.listener.PermissionGrantedResponse
import com.karumi.dexter.listener.PermissionRequest
import com.karumi.dexter.listener.single.PermissionListener
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.subjects.PublishSubject
import merchant.okcredit.accounting.model.Transaction
import merchant.okcredit.accounting.views.LoadMoreView
import org.joda.time.DateTime
import tech.okcredit.android.base.extensions.getColorFromAttr
import tech.okcredit.android.base.extensions.snackbar
import tech.okcredit.android.base.extensions.viewLifecycleScoped
import tech.okcredit.android.base.utils.DateTimeUtils
import tech.okcredit.android.base.utils.ThreadUtils
import tech.okcredit.app_contract.LegacyNavigator
import tech.okcredit.base.Traces
import java.lang.ref.WeakReference
import java.util.concurrent.TimeUnit
import javax.inject.Inject

class AccountStatementFragment :
    BaseFragment<State, ViewEvent, Intent>("AccountStatementScreen", R.layout.account_statement_fragment),
    TransactionView.Listener,
    LoadMoreView.Listener {

    private val binding: AccountStatementFragmentBinding by viewLifecycleScoped(AccountStatementFragmentBinding::bind)

    private val onChangeDate: PublishSubject<Pair<DateTime, DateTime>> = PublishSubject.create()
    private val downloadAccStatement: PublishSubject<Pair<DateTime, DateTime>> = PublishSubject.create()
    private val hideDownloadAlert: PublishSubject<Unit> = PublishSubject.create()
    private val loadMorePublicSubject: PublishSubject<Unit> = PublishSubject.create()
    private val showAlert: PublishSubject<String> = PublishSubject.create()
    private val selectOnlineTxnsFilterPublishSubject: PublishSubject<Boolean> = PublishSubject.create()

    private lateinit var accountStatementController: AccountStatementController
    private var alert: Snackbar? = null
    private var isFilterPopupShownOnLaunch = false

    @Inject
    internal lateinit var legacyNavigator: LegacyNavigator

    @Inject
    internal lateinit var tracker: Tracker

    private var popupWindow: PopupWindow? = null

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        accountStatementController = AccountStatementController(this)
        val linearLayoutManager = LinearLayoutManager(context)
        binding.recyclerView.layoutManager = linearLayoutManager
        binding.recyclerView.adapter = accountStatementController.adapter

        binding.menuOverflow.setOnClickListener {
            showFilterPopup()
        }
        binding.rootView.setTracker(performanceTracker)
    }

    override fun onPause() {
        super.onPause()
        clearVectorDrawableAnimationCallbacks(binding.ivDownloading)
    }

    override fun onBackPressed(): Boolean {
        val popup = popupWindow
        if (popup != null && popup.isShowing) {
            popup.dismiss()
            return true
        }

        return super.onBackPressed()
    }

    override fun loadIntent(): UserIntent {
        return Intent.Load
    }

    override fun userIntents(): Observable<UserIntent> {
        return Observable.mergeArray(
            Observable.just(Intent.ObserveWorkerStatus(WeakReference(viewLifecycleOwner))),

            downloadAccStatement
                .throttleFirst(300, TimeUnit.MILLISECONDS)
                .map { Intent.DownloadStatement(it.first, it.second) },

            hideDownloadAlert
                .throttleFirst(300, TimeUnit.MILLISECONDS)
                .map { Intent.HideDownloadAlert },

            loadMorePublicSubject
                .throttleFirst(300, TimeUnit.MILLISECONDS)
                .map { Intent.LoadOldTxns },

            onChangeDate
                .throttleFirst(300, TimeUnit.MILLISECONDS)
                .map { Intent.ChangeDateRange(it.first, it.second) },

            showAlert
                .map { Intent.ShowAlert(it) },

            selectOnlineTxnsFilterPublishSubject
                .throttleFirst(300, TimeUnit.MILLISECONDS)
                .map {
                    Intent.SelectOnlineTransactions(it)
                }
        )
    }

    @AddTrace(name = Traces.RENDER_ACCOUNT_STATEMENT)
    override fun render(state: State) {

        binding.dateContainer.setOnClickListener {
            Analytics.track(AnalyticsEvents.REQUEST_ACCOUNT_STATEMENT)

            DateRangePickerDialog.show(
                requireContext(),
                state.startDate,
                state.endDate,
                object : DateRangePickerDialog.Listener {
                    override fun onDone(startDate: DateTime, endDate: DateTime) {
                        onChangeDate.onNext(startDate to endDate)
                    }
                }
            ).show()
        }

        binding.btnDownload.setOnClickListener {
            if (!state.isLoadingDownload) {
                downloadAccountStatement(state.startDate, state.endDate)
            }
        }

        setGroupDownloadVisibility(state.isShowDownloadAlert)

        if (state.sourceScreen == "collection_screen" && !isFilterPopupShownOnLaunch && state.isOnlineTransactionSelected) {
            isFilterPopupShownOnLaunch = true
            binding.rootView.post { showFilterPopup() }
        }

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
                hideDownloadedAlertIfShowing()
            }
        })

        accountStatementController.setState(state)

        if (DateTimeUtils.formatDateOnly(state.startDate) == DateTimeUtils.formatDateOnly(state.endDate)) {
            binding.dateRange.text = DateTimeUtils.formatTx(state.startDate, requireContext())
        } else {
            binding.dateRange.text = getString(
                R.string.date_range_placeholder,
                DateTimeUtils.formatDateOnly(state.startDate),
                DateTimeUtils.formatDateOnly(state.endDate)
            )
        }

        setDownloadButtonVisibility(state)
        setProgressBarVisibility(state)

        // show/hide alert
        if (state.networkError or state.error or state.isAlertVisible) {
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
    }

    internal fun hideDownloadedAlertIfShowing() {
        if (getCurrentState().isShowDownloadAlert) {
            hideDownloadAlert.onNext(Unit)
        }
    }

    private fun setGroupDownloadVisibility(showDownloadAlert: Boolean) {
        if (showDownloadAlert) TransitionManager.beginDelayedTransition(binding.rootView)
        binding.groupDownloaded.isVisible = showDownloadAlert
    }

    internal fun setDownloadButtonVisibility(state: State) {
        binding.btnDownload.isVisible = state.transactions.isNotEmpty() &&
            state.isLoading.not() &&
            state.isLoadingDownload.not() &&
            state.isOnlineTransactionSelected.not()
    }

    private fun setProgressBarVisibility(state: State) {
        if (state.isLoadingDownload) {
            TransitionManager.beginDelayedTransition(binding.rootView)
            binding.groupDownloading.isVisible = true
            startVectorDrawableAnimation(binding.ivDownloading)
        } else {
            clearVectorDrawableAnimationCallbacks(binding.ivDownloading)
            binding.groupDownloading.isVisible = false
        }
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
                        Analytics.track(
                            AnalyticsEvents.PERMISSION_ACCEPT,
                            EventProperties.create()
                                .with(PropertyKey.SCREEN, "account_statement")
                                .with(PropertyKey.TYPE, "storage")
                        )

                        Observable.timer(500, TimeUnit.MILLISECONDS, ThreadUtils.newThread())
                            .observeOn(AndroidSchedulers.mainThread())
                            .subscribe {
                                downloadAccStatement.onNext(startDate to endDate)
                            }
                    } else {
                        downloadAccStatement.onNext(startDate to endDate)
                    }
                }

                override fun onPermissionDenied(response: PermissionDeniedResponse) {
                    Analytics.track(
                        AnalyticsEvents.PERMISSION_DENIED,
                        EventProperties.create()
                            .with(PropertyKey.SCREEN, "account_statement")
                            .with(PropertyKey.TYPE, "storage")
                    )
                }

                override fun onPermissionRationaleShouldBeShown(
                    permission: PermissionRequest,
                    token: PermissionToken?
                ) {
                    token?.continuePermissionRequest()
                }
            }).check()
    }

    private fun showFilterPopup() {

        if (activity?.isFinishing == true || activity?.isDestroyed == true) return

        val contentView = LayoutInflater.from(context).inflate(R.layout.transaction_filter_layout, null)

        contentView.measure(
            View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED),
            View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED)
        )

        popupWindow = PopupWindow(context)
        popupWindow?.isOutsideTouchable = true
        popupWindow?.contentView = contentView
        popupWindow?.setBackgroundDrawable(ColorDrawable(Color.WHITE))
        popupWindow?.elevation = 10.0f
        popupWindow?.isFocusable = true
        popupWindow?.width = ViewGroup.LayoutParams.WRAP_CONTENT
        popupWindow?.height = ViewGroup.LayoutParams.WRAP_CONTENT

        val popupWidth = contentView.measuredWidth
        val deviceWidth = context?.resources?.displayMetrics?.widthPixels
        val margindEnd = DimensionUtil.dp2px(requireContext(), 16.0f).toInt()

        val xOffset = deviceWidth?.minus(popupWidth + margindEnd) ?: 0

        val rect = Rect()
        binding.menuOverflow.getGlobalVisibleRect(rect)
        val yOffset = rect.bottom.plus(DimensionUtil.dp2px(requireContext(), 8f).toInt())

        popupWindow?.showAtLocation(binding.menuOverflow, Gravity.NO_GRAVITY, xOffset, yOffset)

        val allTxns = contentView.findViewById<TextView>(R.id.all_txns)
        val onlineTxns = contentView.findViewById<TextView>(R.id.online_txns)
        val onlineTxnsContainer = contentView.findViewById<LinearLayout>(R.id.online_txns_container)
        val allTxnsContainer = contentView.findViewById<LinearLayout>(R.id.all_txns_container)
        val onlineTxnsIcon = contentView.findViewById<ImageView>(R.id.online_txns_icon)
        val allTxnsIcon = contentView.findViewById<ImageView>(R.id.all_txns_icon)

        val state = getCurrentState()
        if (state.isOnlineTransactionSelected) {
            onlineTxnsContainer.setBackgroundColor(requireContext().getColorFromAttr(R.attr.colorPrimary1))
            onlineTxns.setTextColor(ContextCompat.getColor(requireContext(), R.color.white))
            onlineTxnsIcon.setColorFilter(ContextCompat.getColor(requireContext(), R.color.white))
            allTxnsContainer.setBackgroundResource(R.color.white)
            allTxns.setTextColor(ContextCompat.getColor(requireContext(), R.color.grey800))
            allTxnsIcon.setColorFilter(requireContext().getColorFromAttr(R.attr.colorPrimary1))
            tracker.trackEvents(Event.TRANSACTION_FILTER_POPUP_SHOWN, type = PropertyValue.ONLINE_TRANSACTIONS)
        } else {
            onlineTxnsContainer.setBackgroundResource(R.color.white)
            onlineTxns.setTextColor(ContextCompat.getColor(requireContext(), R.color.grey800))
            onlineTxnsIcon.setColorFilter(requireContext().getColorFromAttr(R.attr.colorPrimary1))
            allTxnsContainer.setBackgroundColor(requireContext().getColorFromAttr(R.attr.colorPrimary1))
            allTxns.setTextColor(ContextCompat.getColor(requireContext(), R.color.white))
            allTxnsIcon.setColorFilter(ContextCompat.getColor(requireContext(), R.color.white))
            tracker.trackEvents(Event.TRANSACTION_FILTER_POPUP_SHOWN, type = PropertyValue.ALL_TRANSACTIONS)
        }

        allTxnsContainer.setOnClickListener {
            selectOnlineTxnsFilterPublishSubject.onNext(false)
            tracker.trackEvents(Event.TRANSACTION_FILTER_TYPE_SELECTED, type = PropertyValue.ALL_TRANSACTIONS)
            popupWindow?.dismiss()
        }

        onlineTxnsContainer.setOnClickListener {
            tracker.trackEvents(Event.TRANSACTION_FILTER_TYPE_SELECTED, type = PropertyValue.ONLINE_TRANSACTIONS)
            selectOnlineTxnsFilterPublishSubject.onNext(true)
            popupWindow?.dismiss()
        }
    }

    @UiThread
    override fun onTransactionClicked(transaction: Transaction) {
        val type = if (transaction.type == Transaction.PAYMENT || transaction.type == Transaction.RETURN) {
            PropertyValue.PAYMENT
        } else if (transaction.type == Transaction.CREDIT) {
            PropertyValue.CREDIT
        } else {
            "na"
        }

        val status = if (transaction.isDeleted) {
            PropertyValue.DELETED
        } else if (transaction.amountUpdated) {
            PropertyValue.EDITED
        } else if (transaction.isOnlinePaymentTransaction) {
            PropertyValue.ONLINE_PAYMENT
        } else {
            "na"
        }

        tracker.trackViewTransaction(
            screen = PropertyValue.Account_Statement,
            relation = "",
            accountId = transaction.customerId,
            type = type,
            status = status
        )
        legacyNavigator.goToTransactionDetailFragment(requireContext(), transaction.id)
    }

    override fun onLoadMoreClicked() {
        loadMorePublicSubject.onNext(Unit)
    }

    override fun handleViewEvent(event: ViewEvent) {}
}
