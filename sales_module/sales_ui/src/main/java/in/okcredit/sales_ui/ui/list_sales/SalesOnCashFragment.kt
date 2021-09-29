package `in`.okcredit.sales_ui.ui.list_sales

import `in`.okcredit.analytics.Event
import `in`.okcredit.analytics.PropertiesMap
import `in`.okcredit.analytics.PropertyValue
import `in`.okcredit.analytics.Tracker
import `in`.okcredit.sales_sdk.models.Models
import `in`.okcredit.sales_ui.R
import `in`.okcredit.sales_ui.databinding.SalesOnCashScreenBinding
import `in`.okcredit.sales_ui.dialogs.DeleteSaleBottomSheetDialog
import `in`.okcredit.sales_ui.ui.add_sales.AddSaleFragment
import `in`.okcredit.sales_ui.ui.list_sales.views.SaleDeleteLayout
import `in`.okcredit.sales_ui.ui.list_sales.views.SalesController
import `in`.okcredit.sales_ui.ui.list_sales.views.SalesView
import `in`.okcredit.sales_ui.ui.list_sales.views.SalesYoutubeView
import `in`.okcredit.sales_ui.utils.SalesUtil
import `in`.okcredit.shared.base.BaseFragment
import `in`.okcredit.shared.base.UserIntent
import `in`.okcredit.shared.utils.addTo
import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.UiThread
import androidx.core.os.bundleOf
import androidx.core.view.isVisible
import androidx.navigation.fragment.NavHostFragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.snackbar.Snackbar
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.YouTubePlayer
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.views.YouTubePlayerView
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.subjects.PublishSubject
import kotlinx.android.synthetic.main.sales_on_cash_screen.*
import org.jetbrains.annotations.Nullable
import org.joda.time.DateTime
import tech.okcredit.android.base.animation.AnimationUtils
import tech.okcredit.android.base.extensions.setBooleanVisibility
import tech.okcredit.android.base.extensions.snackbar
import tech.okcredit.android.base.language.LocaleManager
import tech.okcredit.android.base.utils.DateTimeUtils
import tech.okcredit.app_contract.LegacyNavigator
import java.util.concurrent.TimeUnit
import javax.inject.Inject

class SalesOnCashFragment :
    BaseFragment<SalesOnCashContract.State, SalesOnCashContract.ViewEvent, SalesOnCashContract.Intent>("SalesOnCashScreen"),
    SalesOnCashContract.Navigator,
    @Nullable SalesView.Listener,
    SaleDeleteLayout.Listener,
    DeleteSaleBottomSheetDialog.DeleteDialogListener,
    @Nullable SalesYoutubeView.YoutubeListener {

    @Inject
    internal lateinit var tracker: Tracker

    @Inject
    internal lateinit var legacyNavigator: LegacyNavigator

    enum class Filter {
        ALL, TODAY, THIS_MONTH, LAST_MONTH
    }

    private val onNewBillSubject: PublishSubject<Unit> = PublishSubject.create()
    private val getAllSales: PublishSubject<Unit> = PublishSubject.create()
    private val onAddSaleSubject: PublishSubject<Unit> = PublishSubject.create()
    private val getSales: PublishSubject<Pair<DateTime, DateTime>> = PublishSubject.create()
    private val deleteSale: PublishSubject<String> = PublishSubject.create()
    private val changeFilter: PublishSubject<Filter> = PublishSubject.create()
    private val setDate: PublishSubject<Pair<DateTime, DateTime>> = PublishSubject.create()
    private val showSalesDetailSubject: PublishSubject<String> = PublishSubject.create()
    private val showBillSummarySubject: PublishSubject<String> = PublishSubject.create()

    private lateinit var binding: SalesOnCashScreenBinding
    private val controller = SalesController(this)
    private var scrollToTop = true
    private var resumed = false
    private var alert: Snackbar? = null

    private var youTubePlayerView: YouTubePlayerView? = null
    private var youTubePlayer: YouTubePlayer? = null
    private var shouldPageReloadData = false

    companion object {
        const val NAVIGATE_TO_ADD_SALE = "NAVIGATE_TO_ADD_SALE"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            if (it.getBoolean(NAVIGATE_TO_ADD_SALE, false)) {
                findNavController(this).navigate(R.id.addSaleScreen, bundleOf("deeplink" to true))
            }
        }
    }

    private fun setAddExpenseListener() {
        val navController = findNavController(this)
        navController.currentBackStackEntry?.savedStateHandle?.getLiveData<Boolean>(AddSaleFragment.ADDED_SALE)
            ?.observe(viewLifecycleOwner) { isSuccess ->
                shouldPageReloadData = isSuccess
            }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = SalesOnCashScreenBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setAddExpenseListener()
        binding.rvSale.adapter = controller.adapter
        binding.rvSale.layoutManager = LinearLayoutManager(activity)
        binding.saleInfoGraphic.youtubePopUp.initYoutubePlayer(this)
        controller.adapter.registerAdapterDataObserver(object : RecyclerView.AdapterDataObserver() {
            @SuppressLint("CheckResult")
            override fun onItemRangeInserted(positionStart: Int, itemCount: Int) {
                scrollToTop()
            }

            @SuppressLint("CheckResult")
            override fun onItemRangeChanged(positionStart: Int, itemCount: Int) {
                scrollToTop()
            }
        })
        binding.summaryView.dateRange.setOnClickListener {
            tracker.trackEvents(eventName = Event.DISPLAY_DATE_CLICK, screen = PropertyValue.CASH_SALES)
        }
        binding.addSale.setOnClickListener {
            if (isStateInitialized() && getCurrentState().isBillingAbEnabled) {
                onNewBillSubject.onNext(Unit)
            } else {
                tracker.trackEvents(eventName = Event.ADD_CASH_SALE_STARTED, screen = PropertyValue.CASH_SALES)
                onAddSaleSubject.onNext(Unit)
            }
        }
        binding.toolbar.setNavigationOnClickListener {
            activity?.onBackPressed()
        }
        binding.summaryView.all.setOnClickListener {
            changeFilter.onNext(Filter.ALL)
            tracker.trackEvents(
                eventName = Event.UPDATE_CASH_SALE_DATE_RANGE,
                screen = PropertyValue.CASH_SALES,
                propertiesMap = PropertiesMap.create()
                    .add("Value", "All")
            )
        }
        binding.summaryView.today.setOnClickListener {
            changeFilter.onNext(Filter.TODAY)
            tracker.trackEvents(
                eventName = Event.UPDATE_CASH_SALE_DATE_RANGE,
                screen = PropertyValue.CASH_SALES,
                propertiesMap = PropertiesMap.create()
                    .add("Value", "Today")
            )
        }
        binding.summaryView.lastMonth.setOnClickListener {
            changeFilter.onNext(Filter.LAST_MONTH)
            tracker.trackEvents(
                eventName = Event.UPDATE_CASH_SALE_DATE_RANGE,
                screen = PropertyValue.CASH_SALES,
                propertiesMap = PropertiesMap.create()
                    .add("Value", "Last Month")
            )
        }
        binding.summaryView.thisMonth.setOnClickListener {
            changeFilter.onNext(Filter.THIS_MONTH)
            tracker.trackEvents(
                eventName = Event.UPDATE_CASH_SALE_DATE_RANGE,
                screen = PropertyValue.CASH_SALES,
                propertiesMap = PropertiesMap.create()
                    .add("Value", "This month")
            )
        }
        binding.retry.setOnClickListener {
            binding.noInternetView.visibility = View.GONE
            reLoad()
        }
        binding.saleInfoGraphic.youtubeThumbnail.setOnClickListener {
            binding.saleInfoGraphic.youtubePopUp.setBooleanVisibility(true)
            youTubePlayerView?.exitFullScreen()
            youTubePlayer?.seekTo(0f)
            youTubePlayer?.play()
        }
        binding.saleInfoGraphic.root.setOnClickListener {
            youTubePlayer?.pause()
            binding.saleInfoGraphic.youtubePopUp.setBooleanVisibility(false)
        }
    }

    private fun filter(filter: Filter?) {
        binding.summaryView.all.isSelected = filter == Filter.ALL
        binding.summaryView.today.isSelected = filter == Filter.TODAY
        binding.summaryView.lastMonth.isSelected = filter == Filter.LAST_MONTH
        binding.summaryView.thisMonth.isSelected = filter == Filter.THIS_MONTH
        setTextColors()
    }

    private fun setTextColors() {
        binding.summaryView.all.setTextColor(getTextColor(binding.summaryView.all.isSelected))
        binding.summaryView.today.setTextColor(getTextColor(binding.summaryView.today.isSelected))
        binding.summaryView.lastMonth.setTextColor(getTextColor(binding.summaryView.lastMonth.isSelected))
        binding.summaryView.thisMonth.setTextColor(getTextColor(binding.summaryView.thisMonth.isSelected))
    }

    private fun getTextColor(isSelected: Boolean): Int {
        if (isSelected) {
            return resources.getColor(R.color.white)
        } else {
            return resources.getColor(R.color.grey900)
        }
    }

    private fun checkPageReload() {
        if (shouldPageReloadData && isStateInitialized()) {
            shouldPageReloadData = false
            pushIntent(SalesOnCashContract.Intent.Load)
        }
    }

    override fun onResume() {
        super.onResume()
        resumed = true
        if (isStateInitialized()) {
            getCurrentState().filter?.let {
                changeFilter.onNext(it)
            }
        }
        checkPageReload()
    }

    override fun onPause() {
        super.onPause()
        resumed = false
        youTubePlayer?.pause()
    }

    override fun onDestroyView() {
        youTubePlayerView?.release()
        super.onDestroyView()
    }

    override fun onBackPressed(): Boolean {
        LocaleManager.fixWebViewLocale(requireContext())
        return super.onBackPressed()
    }

    internal fun scrollToTop() {
        Observable.timer(300, TimeUnit.MILLISECONDS)
            .observeOn(AndroidSchedulers.mainThread())
            .doOnNext {
                if (scrollToTop) {
                    binding.rvSale.scrollToPosition(0)
                }
            }
            .filter { scrollToTop.not() }
            .delay { Observable.timer(5, TimeUnit.SECONDS) }
            .doOnNext {
                if (resumed) {
                    scrollToTop = true
                }
            }
            .filter { resumed.not() }
            .delay { Observable.timer(5, TimeUnit.SECONDS) }
            .doOnNext {
                if (resumed) {
                    scrollToTop = true // resetting to true
                }
            }
            .subscribe()
            .addTo(autoDisposable)
    }

    override fun loadIntent(): UserIntent {
        return SalesOnCashContract.Intent.Load
    }

    override fun userIntents(): Observable<UserIntent> {
        return Observable.mergeArray(
            getAllSales.throttleFirst(300, TimeUnit.MILLISECONDS)
                .map {
                    SalesOnCashContract.Intent.GetAllSales
                },
            getSales.throttleFirst(300, TimeUnit.MILLISECONDS)
                .map {
                    SalesOnCashContract.Intent.GetSales(it.first, it.second)
                },
            changeFilter.throttleFirst(300, TimeUnit.MILLISECONDS)
                .map {
                    SalesOnCashContract.Intent.ChangeFilter(it)
                },
            deleteSale.throttleFirst(300, TimeUnit.MILLISECONDS)
                .map {
                    SalesOnCashContract.Intent.DeleteSale(it)
                },
            setDate.throttleFirst(300, TimeUnit.MILLISECONDS)
                .map {
                    SalesOnCashContract.Intent.SetDate(it.first, it.second)
                },
            onAddSaleSubject.throttleFirst(300, TimeUnit.MILLISECONDS)
                .map {
                    SalesOnCashContract.Intent.OnAddSaleIntent
                },
            showSalesDetailSubject.throttleFirst(300, TimeUnit.MILLISECONDS)
                .map {
                    SalesOnCashContract.Intent.ShowSaleDetailIntent(it)
                },
            showBillSummarySubject.throttleFirst(300, TimeUnit.MILLISECONDS)
                .map {
                    SalesOnCashContract.Intent.ShowBillSummaryIntent(it)
                },
            onNewBillSubject.throttleFirst(300, TimeUnit.MILLISECONDS)
                .map {
                    SalesOnCashContract.Intent.OnNewBillIntent
                }
        )
    }

    override fun render(state: SalesOnCashContract.State) {
        controller.setStates(state)
        if (state.isLoading) {
            binding.progressBar.visibility = View.VISIBLE
        } else {
            binding.progressBar.visibility = View.INVISIBLE
        }
        if (state.networkError) {
            binding.noInternetView.visibility = View.VISIBLE
        } else {
            binding.noInternetView.visibility = View.GONE
        }
        binding.summaryView.totalSales.text =
            getString(R.string.rupees, SalesUtil.currencyDisplayFormat(state.totalAmount))

        if (state.startDate != null && state.endDate != null) {
            if (state.startDate.withTimeAtStartOfDay()
                .isEqual(state.endDate.withTimeAtStartOfDay()) || state.startDate.isEqual(state.endDate.minusDays(1))
            ) {
                binding.summaryView.dateRange.text = getString(R.string.total_sales_on, DateTimeUtils.getFormat1(state.startDate))
            } else {
                binding.summaryView.dateRange.text = getString(
                    R.string.total_sales_on,
                    DateTimeUtils.getFormat1(state.startDate) + " - " + DateTimeUtils.getFormat1(state.endDate)
                )
            }
        }
        filter(state.filter)
        if (state.isBillingAbEnabled) {
            binding.addSale.text = resources.getString(R.string.new_bill)
        } else {
            binding.addSale.text = resources.getString(R.string.add_sale)
        }
        if (state.canYoutubeVideoShow) {
            binding.handText.visibility = View.VISIBLE
            binding.hand.visibility = View.VISIBLE
            AnimationUtils.leftRightMotion(binding.hand)
            binding.summaryView.filters.visibility = View.GONE
            summary_view.visibility = View.GONE
            if (state.isBillingAbEnabled) {
                if (state.isBillingInfoGraphicAbEnabled) {
                    binding.saleInfoGraphic.infoText.text =
                        requireContext().getString(R.string.sales_billing_info_text_ab)
                    binding.saleInfoGraphic.infoImg.setImageResource(R.drawable.ic_sales_bill_info_graphic_ab)
                    binding.handText.text = requireContext().getString(R.string.start_making_bill)
                } else {
                    binding.saleInfoGraphic.infoText.text = requireContext().getString(R.string.sales_billing_info_text)
                    binding.saleInfoGraphic.infoImg.setImageResource(R.drawable.ic_sales_bill_info_graphic)
                    binding.handText.text = requireContext().getString(R.string.make_your_first_bill)
                }
            }
            binding.saleInfoGraphic.root.setBooleanVisibility(true)
            binding.rvSale.setBooleanVisibility(false)
        } else {
            youTubePlayer?.pause()
            binding.handText.visibility = View.GONE
            binding.hand.visibility = View.GONE
            binding.summaryView.filters.visibility = View.VISIBLE
            summary_view.visibility = View.VISIBLE
            binding.rvSale.setBooleanVisibility(true)
            binding.saleInfoGraphic.root.setBooleanVisibility(false)
        }

        if (state.canShowAlert) {
            alert = when {
                state.alert.isNotEmpty() -> view?.snackbar(state.alert, Snackbar.LENGTH_INDEFINITE)
                else -> view?.snackbar(getString(R.string.err_default), Snackbar.LENGTH_INDEFINITE)
            }
            if (alert?.isShown?.not()!!) {
                alert?.show()
            }
        } else {
            alert?.dismiss()
        }
    }

    override fun showAll() {
        getAllSales.onNext(Unit)
    }

    override fun showToday() {
        val startDate = DateTimeUtils.currentDateTime().withTimeAtStartOfDay()
        val endDate = DateTimeUtils.currentDateTime().plusDays(1).withTimeAtStartOfDay()
        getSales.onNext(Pair(startDate, endDate))
        setDate.onNext(Pair(startDate, endDate.minusDays(1)))
    }

    override fun showLastMonth() {
        val startDate = DateTimeUtils.currentDateTime().minusMonths(1).withDayOfMonth(1).withTimeAtStartOfDay()
        val endDate = DateTimeUtils.currentDateTime().withDayOfMonth(1).withTimeAtStartOfDay()
        getSales.onNext(Pair(startDate, endDate))
        setDate.onNext(Pair(startDate, endDate.minusDays(1)))
    }

    override fun showThisMonth() {
        val startDate = DateTimeUtils.currentDateTime().withDayOfMonth(1).withTimeAtStartOfDay()
        val endDate = DateTimeUtils.currentDateTime().plusDays(1).withTimeAtStartOfDay()
        getSales.onNext(Pair(startDate, endDate))
        setDate.onNext(Pair(startDate, endDate.minusDays(1)))
    }

    override fun reLoad() {
        getCurrentState().filter?.let {
            changeFilter.onNext(it)
        }
    }

    override fun onDeleted(saleId: String) {
        tracker.trackEvents(
            eventName = Event.CASH_SALE_DELETED,
            screen = "Cash Sale LongPress",
            propertiesMap = PropertiesMap.create()
                .add("Tx id", saleId)
        )
        reLoad()
    }

    override fun gotoLogin() {
        activity?.runOnUiThread {
            legacyNavigator.goToLoginScreen(requireActivity())
        }
    }

    override fun onLongClick(sale: Models.Sale) {
        tracker.trackEvents(
            eventName = Event.CASH_SALE_TRANSACTION_LONGPRESS,
            screen = PropertyValue.CASH_SALES,
            propertiesMap = PropertiesMap.create().add("Tx id", sale.id)
        )
        binding.deleteLayout.visibility = View.VISIBLE
        binding.deleteLayout.setContent(sale, getCurrentState().filter == Filter.TODAY)
        binding.deleteLayout.setListener(this)
        binding.addSale.visibility = View.GONE
    }

    override fun onClick(sale: Models.Sale) {
        tracker.trackEvents(
            eventName = Event.VIEW_CASH_SALE_TRANSACTION,
            screen = PropertyValue.CASH_SALES,
            propertiesMap = PropertiesMap.create().add("Deleted", sale.deletedAt != null)
        )
        if (getCurrentState().isBillingAbEnabled) {
            showBillSummarySubject.onNext(sale.id)
        } else {
            showSalesDetailSubject.onNext(sale.id)
        }
    }

    @UiThread
    override fun gotoSalesDetailScreen(saleId: String) {
        activity?.runOnUiThread {
            findNavController(this).navigate(
                R.id.action_salesOnCashScreen_to_salesDetailScreen,
                bundleOf("sale_id" to saleId)
            )
        }
    }

    override fun gotoBillSummaryScreen(saleId: String) {
        activity?.runOnUiThread {
            findNavController(this).navigate(
                R.id.action_salesOnCashScreen_to_billSummaryScreen,
                bundleOf("sale_id" to saleId, "editable" to true)
            )
        }
    }

    @UiThread
    override fun gotoAddSaleScreen() {
        activity?.runOnUiThread {
            findNavController(this).navigate(
                R.id.action_salesOnCashScreen_to_addSaleScreen,
                bundleOf()
            )
        }
    }

    @UiThread
    override fun gotoBillItemScreen() {
        activity?.runOnUiThread {
            findNavController(this).navigate(
                R.id.action_salesOnCashScreen_to_addBillItemsScreen,
                bundleOf()
            )
        }
    }

    override fun onDeleteClicked(sale: Models.Sale) {
        binding.deleteLayout.visibility = View.GONE
        binding.addSale.visibility = View.VISIBLE
        val deleteFragment = DeleteSaleBottomSheetDialog.newInstance(sale.id)
        deleteFragment.setListener(this)
        deleteFragment.show(childFragmentManager, DeleteSaleBottomSheetDialog.TAG)
        deleteFragment.isCancelable = false
        tracker.trackEvents(
            eventName = Event.DELETE_CASH_SALE_CLICK,
            screen = "Cash Sale LongPress",
            propertiesMap = PropertiesMap.create()
                .add("Tx_id", sale.id)
                .add("Amount", sale.amount)
        )
    }

    override fun onDismiss() {
        tracker.trackEvents(eventName = Event.DELETE_CASH_SALE_CANCELLED, screen = "Cash Sale LongPress")
        binding.deleteLayout.visibility = View.GONE
        binding.addSale.visibility = View.VISIBLE
    }

    override fun onDelete(saleId: String) {
        scrollToTop = false
        deleteSale.onNext(saleId)
    }

    override fun onCancel() {
        tracker.trackEvents(eventName = Event.DELETE_CASH_SALE_CANCELLED, screen = "Cash Sale LongPress")
    }

    override fun onYouTubeReady(youTubePlayerView: YouTubePlayerView, youTubePlayer: YouTubePlayer) {
        this.youTubePlayerView = youTubePlayerView
        this.youTubePlayer = youTubePlayer
        if (isStateInitialized()) {
            val videoUrl = if (getCurrentState().isBillingAbEnabled) getCurrentState().videoUrlWithBill else getCurrentState().videoUrl
            youTubePlayer.loadVideo(videoUrl, 0F)
            if (binding.saleInfoGraphic.youtubePopUp.isVisible) youTubePlayer.play() else youTubePlayer.pause()
        }
    }

    override fun videoStartedListener(youTubeState: String) {
        tracker.trackEvents(
            eventName = Event.YOUTUBE_VIDEO,
            type = PropertyValue.STARTED,
            screen = PropertyValue.CASH_SALES,
            propertiesMap = PropertiesMap.create().add("Video Id", getCurrentState().videoUrl)
        )
    }

    override fun videoPlayListener(youTubeState: String) {
    }

    override fun videoPauseListener(youTubeState: String) {
    }

    override fun videoCompletedListener(youTubeState: String) {
        tracker.trackEvents(
            eventName = Event.YOUTUBE_VIDEO,
            type = PropertyValue.COMPLETED,
            screen = PropertyValue.CASH_SALES,
            propertiesMap = PropertiesMap.create().add("Video Id", getCurrentState().videoUrl)
        )
    }

    override fun videoOnError(youTubeState: String) {
        tracker.trackEvents(
            eventName = Event.YOUTUBE_VIDEO,
            type = PropertyValue.FAILED,
            screen = PropertyValue.CASH_SALES,
            propertiesMap = PropertiesMap.create().add("Video Id", getCurrentState().videoUrl)
        )
    }

    override fun handleViewEvent(event: SalesOnCashContract.ViewEvent) {
    }
}
