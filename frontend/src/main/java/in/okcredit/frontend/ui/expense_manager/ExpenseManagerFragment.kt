package `in`.okcredit.frontend.ui.expense_manager

import `in`.okcredit.analytics.*
import `in`.okcredit.backend.contract.RxSharedPrefValues
import `in`.okcredit.communication_inappnotification.contract.LocalInAppNotificationHandler
import `in`.okcredit.communication_inappnotification.contract.ui.local.TapTargetLocal
import `in`.okcredit.expense.models.Models
import `in`.okcredit.frontend.R
import `in`.okcredit.frontend.databinding.ExpenseManagerScreenBinding
import `in`.okcredit.frontend.databinding.ViewExpenseInfoGraphicV1Binding
import `in`.okcredit.frontend.databinding.ViewExpenseInfoGraphicV2Binding
import `in`.okcredit.frontend.databinding.ViewExpenseInfoGraphicV3Binding
import `in`.okcredit.frontend.databinding.ViewExpenseYoutubeBinding
import `in`.okcredit.frontend.ui._dialogs.DeleteExpenseBottomSheetDialog
import `in`.okcredit.frontend.ui._dialogs.FeedBackBottomSheetDialog
import `in`.okcredit.frontend.ui.add_expense.AddExpenseFragment
import `in`.okcredit.frontend.ui.expense_manager.views.ExpenseController
import `in`.okcredit.frontend.ui.expense_manager.views.ExpenseDeleteLayout
import `in`.okcredit.frontend.ui.expense_manager.views.ExpensePagerAdapter
import `in`.okcredit.frontend.ui.expense_manager.views.ExpenseView
import `in`.okcredit.frontend.ui.expense_manager.views.ExpenseYoutubeView
import `in`.okcredit.shared.base.BaseFragment
import `in`.okcredit.shared.base.UserIntent
import `in`.okcredit.shared.dialogs.DateRangeDialog
import android.os.Bundle
import android.os.CountDownTimer
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.UiThread
import androidx.core.os.bundleOf
import androidx.core.view.isVisible
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.NavHostFragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.tabs.TabLayoutMediator
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.YouTubePlayer
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.views.YouTubePlayerView
import dagger.Lazy
import io.reactivex.Observable
import io.reactivex.subjects.PublishSubject
import jp.wasabeef.recyclerview.animators.FadeInDownAnimator
import kotlinx.android.synthetic.main.expense_manager_screen.*
import kotlinx.coroutines.launch
import org.jetbrains.annotations.Nullable
import org.joda.time.DateTime
import tech.okcredit.android.base.animation.AnimationUtils
import tech.okcredit.android.base.extensions.gone
import tech.okcredit.android.base.extensions.setBooleanVisibility
import tech.okcredit.android.base.extensions.setStatusBarColor
import tech.okcredit.android.base.extensions.shortToast
import tech.okcredit.android.base.extensions.visible
import tech.okcredit.android.base.language.LocaleManager
import tech.okcredit.android.base.preferences.Scope
import tech.okcredit.android.base.utils.DateTimeUtils
import tech.okcredit.app_contract.LegacyNavigator
import uk.co.samuelwall.materialtaptargetprompt.MaterialTapTargetPrompt
import java.lang.ref.WeakReference
import java.util.concurrent.TimeUnit
import javax.inject.Inject

class ExpenseManagerFragment :
    BaseFragment<ExpenseManagerContract.State, ExpenseManagerContract.ViewEvent, ExpenseManagerContract.Intent>("ExpenseManagerScreen"),
    ExpenseManagerContract.Navigator,
    ExpenseView.Listener,
    DeleteExpenseBottomSheetDialog.DeleteDialogListener,
    ExpenseDeleteLayout.Listener,
    @Nullable ExpenseYoutubeView.YoutubeListener,
    FeedBackBottomSheetDialog.Listener,
    DateRangeDialog.Listener {

    @Inject
    internal lateinit var legacyNavigator: LegacyNavigator

    @Inject
    internal lateinit var tracker: Tracker

    @Inject
    lateinit var localInAppNotificationHandler: Lazy<LocalInAppNotificationHandler>

    private var controller = ExpenseController(this)

    private lateinit var linearLayout: LinearLayoutManager

    private val deleteExpense: PublishSubject<String> = PublishSubject.create()
    private val getExpenses: PublishSubject<Pair<DateTime, DateTime>> = PublishSubject.create()
    private val getAllExpenses: PublishSubject<Unit> = PublishSubject.create()
    private val changeFilter: PublishSubject<ExpenseManagerContract.Filter> = PublishSubject.create()
    private val setDateRange: PublishSubject<Pair<DateTime, DateTime>> = PublishSubject.create()
    private var firstAddExpenseEducationSubject = PublishSubject.create<Boolean>()
    private val retrySubject: PublishSubject<Unit> = PublishSubject.create()
    private val addExpenseClicked: PublishSubject<Unit> = PublishSubject.create()
    private val showDeleteLayout: PublishSubject<Models.Expense> = PublishSubject.create()
    private val hideDeleteLayout: PublishSubject<Unit> = PublishSubject.create()
    private val showDeleteConfirmDialog: PublishSubject<Boolean> = PublishSubject.create()
    private val submitFeedBack: PublishSubject<String> = PublishSubject.create()
    private var canShowFirstAddExpenseEducation = false
    private var scrollToTop = false
    private var deleteFragment: DeleteExpenseBottomSheetDialog? = null
    private lateinit var binding: ExpenseManagerScreenBinding
    private lateinit var expenseYoutubeBinding: ViewExpenseYoutubeBinding
    private lateinit var expenseInfoGraphicV1: ViewExpenseInfoGraphicV1Binding
    private lateinit var expenseInfoGraphicV2: ViewExpenseInfoGraphicV2Binding
    private lateinit var expenseInfoGraphicV3: ViewExpenseInfoGraphicV3Binding
    private var youTubePlayerView: YouTubePlayerView? = null
    private var youTubePlayer: YouTubePlayer? = null
    private var feedBackDialog: FeedBackBottomSheetDialog? = null

    private var filter: ExpenseManagerContract.Filter? = null
    private var shouldPageReloadData = false

    private var addExpenseEducationTimer = object : CountDownTimer(2000, 1000) {
        override fun onFinish() {
            canShowFirstAddExpenseEducation = true
            firstAddExpenseEducationSubject.onNext(true)
        }

        override fun onTick(millisUntilFinished: Long) {
        }
    }.start()

    private var carousalSwipeTimer = object : CountDownTimer(2000, 1000) {
        override fun onFinish() {
            showNextCarousal()
        }

        override fun onTick(millisUntilFinished: Long) {
        }
    }.start()

    private fun resetEducationTimer() {
        addExpenseEducationTimer.cancel()
        addExpenseEducationTimer.start()
        firstAddExpenseEducationSubject.onNext(false)
    }

    companion object {
        const val NAVIGATE_TO_ADD_EXPENSE = "NAVIGATE_TO_ADD_EXPENSE"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            if (it.getBoolean(NAVIGATE_TO_ADD_EXPENSE, false)) {
                findNavController(this).navigate(R.id.addExpenseScreen, bundleOf("deeplink" to true))
            }
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = ExpenseManagerScreenBinding.inflate(inflater, container, false)
        expenseYoutubeBinding = ViewExpenseYoutubeBinding.bind(binding.expenseInfoGraphic.root)
        expenseInfoGraphicV1 = ViewExpenseInfoGraphicV1Binding.bind(binding.expenseInfoGraphic.root)
        expenseInfoGraphicV2 = ViewExpenseInfoGraphicV2Binding.bind(binding.expenseInfoGraphic.root)
        expenseInfoGraphicV3 = ViewExpenseInfoGraphicV3Binding.bind(binding.expenseInfoGraphic.root)
        return binding.root
    }

    private fun showNextCarousal() {
        if (isStateInitialized() && getCurrentState().isNewUser && getCurrentState().onBoardingVariant == ExpenseManagerContract.OnBoardingVariant.v3) {
            val max = expenseInfoGraphicV3.viewPager.adapter?.itemCount ?: 1 - 1
            val next = expenseInfoGraphicV3.viewPager.currentItem + 1
            if (next == max) {
                expenseInfoGraphicV3.viewPager.setCurrentItem(0, true)
            } else {
                expenseInfoGraphicV3.viewPager.setCurrentItem(next, true)
            }
        }
    }

    private fun setAddExpenseListener() {
        val navController = findNavController(this)
        navController.currentBackStackEntry?.savedStateHandle?.getLiveData<Boolean>(AddExpenseFragment.ADDED_EXPENSE)
            ?.observe(viewLifecycleOwner) { isSuccess ->
                shouldPageReloadData = isSuccess
            }
    }

    override fun onResume() {
        super.onResume()
        if (shouldPageReloadData && isStateInitialized()) {
            shouldPageReloadData = false
            pushIntent(ExpenseManagerContract.Intent.Load)
        }
    }

    override fun loadIntent(): UserIntent {
        return ExpenseManagerContract.Intent.Load
    }

    override fun userIntents(): Observable<UserIntent> {
        resetEducationTimer()
        return Observable.mergeArray(
            deleteExpense.throttleLast(300, TimeUnit.MILLISECONDS)
                .map {
                    scrollToTop = false
                    ExpenseManagerContract.Intent.DeleteExpense(it)
                },
            getExpenses.throttleFirst(300, TimeUnit.MILLISECONDS)
                .map {
                    ExpenseManagerContract.Intent.GetExpenses(it.first, it.second)
                },
            getAllExpenses.throttleFirst(300, TimeUnit.MILLISECONDS)
                .map {
                    ExpenseManagerContract.Intent.GetAllExpenses
                },
            changeFilter.throttleFirst(300, TimeUnit.MILLISECONDS)
                .map {
                    resetEducationTimer()
                    ExpenseManagerContract.Intent.ChangeFilter(it)
                },
            firstAddExpenseEducationSubject
                .map {
                    ExpenseManagerContract.Intent.SetFirstAddExpenseEducation(it)
                },
            retrySubject
                .throttleFirst(300, TimeUnit.MILLISECONDS)
                .map {
                    ExpenseManagerContract.Intent.Retry
                },
            addExpenseClicked
                .throttleFirst(300, TimeUnit.MILLISECONDS)
                .map {
                    scrollToTop = true
                    ExpenseManagerContract.Intent.OnAddExpenseClicked
                },
            showDeleteLayout
                .throttleFirst(300, TimeUnit.MILLISECONDS)
                .map {
                    scrollToTop = false
                    ExpenseManagerContract.Intent.ShowDeleteLayout(it)
                },
            hideDeleteLayout
                .throttleFirst(300, TimeUnit.MILLISECONDS)
                .map {
                    scrollToTop = false
                    ExpenseManagerContract.Intent.HideDeleteLayout
                },
            showDeleteConfirmDialog
                .throttleFirst(300, TimeUnit.MILLISECONDS)
                .map {
                    ExpenseManagerContract.Intent.ShowDeleteConfirmDialog(it)
                },
            setDateRange
                .throttleFirst(300, TimeUnit.MILLISECONDS)
                .map {
                    ExpenseManagerContract.Intent.SetDateRangeIntent(it.first, it.second)
                },
            submitFeedBack
                .throttleFirst(300, TimeUnit.MILLISECONDS)
                .map {
                    ExpenseManagerContract.Intent.SubmitFeedBack(it)
                }
        )
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initView()
        initViewPager()
        setListeners()
        setAddExpenseListener()
    }

    private fun initViewPager() {
        expenseInfoGraphicV3.viewPager.adapter = ExpensePagerAdapter()
        TabLayoutMediator(expenseInfoGraphicV3.pageIndicator, expenseInfoGraphicV3.viewPager) { tab, position ->
        }.attach()
        expenseInfoGraphicV3.viewPager.setCurrentItem(0, true)
    }

    private fun showOnBoarding(onBoardingVariant: ExpenseManagerContract.OnBoardingVariant) {
        when (onBoardingVariant) {
            ExpenseManagerContract.OnBoardingVariant.v1 -> {
                expenseYoutubeBinding.infoText.text = getString(R.string.expense_info_text)
                expenseInfoGraphicV1.infoImgV1.visible()
                expenseInfoGraphicV1.infoDescriptionV1.visible()
                expenseInfoGraphicV2.infoImgV2.gone()
                expenseInfoGraphicV3.viewPager.gone()
                expenseInfoGraphicV3.pageIndicator.gone()
                expenseInfoGraphicV3.imgDescription.gone()
            }
            ExpenseManagerContract.OnBoardingVariant.v2 -> {
                setStatusBarColor(resources.getColor(R.color.green_lite))
                binding.root.setBackgroundColor(resources.getColor(R.color.green_lite))
                binding.toolbar.setBackgroundColor(resources.getColor(R.color.green_lite))
                expenseYoutubeBinding.root.setBackgroundColor(resources.getColor(R.color.green_lite))
                expenseYoutubeBinding.infoText.text = getString(R.string.expense_info_text_v3)
                expenseInfoGraphicV1.infoImgV1.gone()
                expenseInfoGraphicV1.infoDescriptionV1.gone()
                expenseInfoGraphicV2.infoImgV2.visible()
                expenseInfoGraphicV3.viewPager.gone()
                expenseInfoGraphicV3.pageIndicator.gone()
                expenseInfoGraphicV3.imgDescription.gone()
            }
            ExpenseManagerContract.OnBoardingVariant.v3 -> {
                expenseYoutubeBinding.infoText.text = getString(R.string.expense_info_text_v3)
                expenseInfoGraphicV1.infoImgV1.gone()
                expenseInfoGraphicV1.infoDescriptionV1.gone()
                expenseInfoGraphicV2.infoImgV2.gone()
                expenseInfoGraphicV3.viewPager.visible()
                expenseInfoGraphicV3.pageIndicator.visible()
                expenseInfoGraphicV3.imgDescription.gone()
                carousalSwipeTimer.start()
            }
        }
    }

    override fun onPause() {
        super.onPause()
        youTubePlayer?.pause()
    }

    override fun onDestroyView() {
        youTubePlayerView?.release()
        carousalSwipeTimer.cancel()
        super.onDestroyView()
    }

    /************************ Initialization  ***********************/

    private fun initView() {
        linearLayout = LinearLayoutManager(activity)
        binding.rvExpense.layoutManager = linearLayout
        binding.rvExpense.adapter = controller.adapter
        binding.rvExpense.itemAnimator = FadeInDownAnimator()
        expenseYoutubeBinding.youtubePopUp.initYoutubePlayer(this)
    }

    private fun setListeners() {
        binding.feedback.setOnClickListener {
            tracker.trackEvents(
                eventName = Event.EXPENSE_FEEDBACK_CLICKED,
                screen = PropertyValue.EXPENSE
            )
            if (feedBackDialog == null) {
                feedBackDialog = FeedBackBottomSheetDialog()
            }
            feedBackDialog?.setListener(this)
            feedBackDialog?.show(childFragmentManager, FeedBackBottomSheetDialog.TAG)
        }
        binding.toolbar.setNavigationOnClickListener {
            activity?.finish()
        }
        binding.addExpenseCard.setOnClickListener {
            filter = getCurrentState().filter
            tracker.trackEvents(eventName = Event.ADD_EXPENSE_STARTED, screen = PropertyValue.EXPENSE)
            addExpenseClicked.onNext(Unit)
        }
        binding.retry.setOnClickListener {
            retrySubject.onNext(Unit)
        }
        binding.all.setOnClickListener {
            tracker.trackEvents(
                eventName = Event.UPDATE_DATE_RANGE,
                screen = PropertyValue.EXPENSE,
                propertiesMap = PropertiesMap.create()
                    .add("Value", "All")
            )
            changeFilter.onNext(ExpenseManagerContract.Filter.ALL)
        }
        binding.thisMonth.setOnClickListener {
            tracker.trackEvents(
                eventName = Event.UPDATE_DATE_RANGE,
                screen = PropertyValue.EXPENSE,
                propertiesMap = PropertiesMap.create()
                    .add("Value", "This month")
            )
            changeFilter.onNext(ExpenseManagerContract.Filter.THIS_MONTH)
        }
        binding.today.setOnClickListener {
            tracker.trackEvents(
                eventName = Event.UPDATE_DATE_RANGE,
                screen = PropertyValue.EXPENSE,
                propertiesMap = PropertiesMap.create()
                    .add("Value", "Last Seven days")
            )
            changeFilter.onNext(ExpenseManagerContract.Filter.TODAY)
        }
        binding.lastMonth.setOnClickListener {
            tracker.trackEvents(
                eventName = Event.UPDATE_DATE_RANGE,
                screen = PropertyValue.EXPENSE,
                propertiesMap = PropertiesMap.create()
                    .add("Value", "Last Month")
            )
            changeFilter.onNext(ExpenseManagerContract.Filter.LAST_MONTH)
        }
        expenseYoutubeBinding.youtubeThumbnail.setOnClickListener {
            expenseYoutubeBinding.youtubePopUp.setBooleanVisibility(true)
            youTubePlayerView?.exitFullScreen()
            youTubePlayer?.seekTo(0f)
            youTubePlayer?.play()
        }
        binding.expenseInfoGraphic.root.setOnClickListener {
            youTubePlayer?.pause()
            expenseYoutubeBinding.youtubePopUp.setBooleanVisibility(false)
        }

        binding.dateRange.setOnClickListener {
            tracker.trackEvents(
                eventName = Event.SELECT_DATE_RANGE,
                screen = PropertyValue.EXPENSE
            )
            val dateRangeDialog = DateRangeDialog()
            dateRangeDialog.setListener(this)
            dateRangeDialog.show(childFragmentManager, DateRangeDialog.TAG)
        }
    }

    override fun goToAddExpenseScreen() {
        activity?.runOnUiThread {
            findNavController(this).navigate(R.id.addExpenseScreen)
        }
    }

    override fun trackEventOnLoad(isNewUser: Boolean, isInfoGraphicShown: Boolean) {
        tracker.trackEvents(
            eventName = Event.EXPENSE_LOADED,
            screen = PropertyValue.EXPENSE,
            propertiesMap = PropertiesMap.create()
                .add("User Type", if (isNewUser) "New" else "Retained")
                .add("Tutorial", isInfoGraphicShown)
        )
    }

    override fun render(state: ExpenseManagerContract.State) {
        controller.setStates(state)

        binding.addExpenseCard.text =
            if (state.showAddexpense) getString(R.string.add_expense) else getString(R.string.new_expense)

        if (state.isLoading) {
            showLoader()
        } else {
            hideLoader()
        }
        if (state.networkError) {
            binding.noInternetView.visibility = View.VISIBLE
        } else {
            binding.noInternetView.visibility = View.GONE
        }
        filter(state.filter)

        if (state.isSummaryViewAbEnabled) {
            binding.summaryView1.setExpense(state)
            binding.summaryView1.visibility = View.VISIBLE
            binding.summaryView2.visibility = View.GONE
        } else {
            binding.summaryView2.setExpense(state)
            binding.summaryView2.visibility = View.VISIBLE
            binding.summaryView1.visibility = View.GONE
        }
        if (state.isNewUser) {
            binding.filters.visibility = View.GONE
            binding.summaryView.visibility = View.GONE
            binding.sideHand.visibility = View.VISIBLE
            binding.rvExpense.visibility = View.GONE
            AnimationUtils.leftRightMotion(binding.sideHand)
            showOnBoarding(state.onBoardingVariant)
        } else {
            youTubePlayer?.pause()
            binding.rvExpense.visibility = View.VISIBLE
            binding.filters.visibility = View.VISIBLE
            binding.summaryView.visibility = View.VISIBLE
            binding.handText.visibility = View.GONE
            binding.hand.visibility = View.GONE
            binding.toolbar.setBackgroundColor(resources.getColor(R.color.white))
            binding.infographic.visibility = View.GONE
            binding.sideHand.visibility = View.GONE
            binding.root.setBackgroundColor(resources.getColor(R.color.white))
            binding.toolbar.setBackgroundColor(resources.getColor(R.color.white))
            setStatusBarColor(resources.getColor(R.color.white))
        }
        binding.expenseInfoGraphic.root.setBooleanVisibility(state.isNewUser)
        if (state.canShowAddExpenseEducation && canShowFirstAddExpenseEducation) {
            canShowFirstAddExpenseEducation = false
            pushIntent(
                ExpenseManagerContract.Intent.RxPreferenceBoolean(
                    RxSharedPrefValues.SHOULD_SHOW_FIRST_SALE_EDUCATION, true, Scope.Individual
                )
            )
            showAddExpenseEducation()
        }
        if (scrollToTop) {
            linearLayout.scrollToPosition(0)
        }

        if (state.canShowDeleteLayout) {
            binding.deleteLayout.visibility = View.VISIBLE
            binding.deleteLayout.setListener(this)
            binding.deleteLayout.setContent(state.selectedExpense!!)
            binding.addExpenseCard.visibility = View.GONE
        } else {
            binding.deleteLayout.visibility = View.GONE
            binding.addExpenseCard.visibility = View.VISIBLE
        }
        if (state.canShowDeleteConfirmDialog) {
            binding.deleteLayout.visibility = View.GONE
            binding.addExpenseCard.visibility = View.VISIBLE
            if (deleteFragment == null) {
                deleteFragment = DeleteExpenseBottomSheetDialog()
            }
            deleteFragment!!.setExpenseId(state.selectedExpense!!.id)
            deleteFragment!!.setListener(this)
            deleteFragment!!.isCancelable = false
            if (!deleteFragment!!.isVisible) {
                deleteFragment!!.show(childFragmentManager, DeleteExpenseBottomSheetDialog.TAG)
            }
        } else {
            deleteFragment?.dismiss()
        }
    }

    private fun filter(filter: ExpenseManagerContract.Filter) {
        pushIntent(
            ExpenseManagerContract.Intent.RxPreferenceString(
                ExpenseManagerContract.DEFAULT_EXPENSE_FILTER,
                filter.name,
                Scope.Individual
            )
        )
        binding.all.isSelected = filter == ExpenseManagerContract.Filter.ALL
        binding.thisMonth.isSelected = filter == ExpenseManagerContract.Filter.THIS_MONTH
        binding.lastMonth.isSelected = filter == ExpenseManagerContract.Filter.LAST_MONTH
        binding.today.isSelected = filter == ExpenseManagerContract.Filter.TODAY
        binding.dateRange.isSelected = filter == ExpenseManagerContract.Filter.DATE_RANGE
        setTextColors()
    }

    private fun setTextColors() {
        binding.all.setTextColor(getTextColor(binding.all.isSelected))
        binding.thisMonth.setTextColor(getTextColor(binding.thisMonth.isSelected))
        binding.lastMonth.setTextColor(getTextColor(binding.lastMonth.isSelected))
        binding.today.setTextColor(getTextColor(binding.today.isSelected))
        binding.dateRange.setTextColor(getTextColor(binding.dateRange.isSelected))
        setDateRangeDrawable()
    }

    private fun setDateRangeDrawable() {
        if (binding.dateRange.isSelected) {
            binding.dateRange.setCompoundDrawablesWithIntrinsicBounds(
                null,
                null,
                resources.getDrawable(R.drawable.ic_arrow_down_white),
                null
            )
        } else {
            binding.dateRange.setCompoundDrawablesWithIntrinsicBounds(
                null,
                null,
                resources.getDrawable(R.drawable.ic_arrow_down_black),
                null
            )
        }
    }

    private fun getTextColor(isSelected: Boolean): Int {
        if (isSelected) {
            return resources.getColor(R.color.white)
        } else {
            return resources.getColor(R.color.grey900)
        }
    }

    private fun showLoader() {
        binding.progressBar.visibility = View.VISIBLE
    }

    private fun hideLoader() {
        binding.progressBar.visibility = View.INVISIBLE
    }

    override fun showAll() {
        getAllExpenses.onNext(Unit)
    }

    override fun showToday() {
        val startDate = DateTimeUtils.currentDateTime().withTimeAtStartOfDay()
        val endDate = DateTimeUtils.currentDateTime().plusDays(1).withTimeAtStartOfDay()
        getExpenses.onNext(Pair(startDate, endDate))
    }

    override fun showThisMonth() {
        val startDate = DateTimeUtils.currentDateTime().withDayOfMonth(1).withTimeAtStartOfDay()
        val endDate = DateTimeUtils.currentDateTime().plusDays(1).withTimeAtStartOfDay()
        getExpenses.onNext(Pair(startDate, endDate))
    }

    override fun showLastMonth() {
        val startDate = DateTimeUtils.currentDateTime().minusMonths(1).withDayOfMonth(1).withTimeAtStartOfDay()
        val endDate = DateTimeUtils.currentDateTime().withDayOfMonth(1).withTimeAtStartOfDay()
        getExpenses.onNext(Pair(startDate, endDate))
    }

    override fun showForSelectedRange(startDate: DateTime, endDate: DateTime) {
        getExpenses.onNext(Pair(startDate.withTimeAtStartOfDay(), endDate.plusDays(1).withTimeAtStartOfDay()))
    }

    private fun showAddExpenseEducation() {
        Analytics.track(
            AnalyticsEvents.IN_APP_NOTI_DISPLAYED,
            EventProperties.create()
                .with(PropertyKey.TYPE, "add_expense")
                .with(PropertyKey.SCREEN, PropertyValue.CASH_SALES)
        )
        activity?.runOnUiThread {
            lifecycleScope.launch {
                localInAppNotificationHandler.get()
                    .generateTapTarget(
                        weakScreen = WeakReference(requireActivity()),
                        tapTarget = TapTargetLocal(
                            screenName = label,
                            targetView = WeakReference(add_expense_card),
                            title = getString(R.string.add_expense_education_primary),
                            subtitle = getString(R.string.add_expense_education_secondary),
                            subtitleGravity = Gravity.END,
                            listener = { _, state ->
                                if (state == MaterialTapTargetPrompt.STATE_FOCAL_PRESSED) {
                                    Analytics.track(
                                        AnalyticsEvents.IN_APP_NOTI_CLICKED,
                                        EventProperties.create()
                                            .with(PropertyKey.TYPE, "add_expense")
                                            .with("focal_area", true)
                                            .with(PropertyKey.SCREEN, PropertyValue.CASH_SALES)
                                    )
                                    pushIntent(
                                        ExpenseManagerContract.Intent.RxPreferenceBoolean(
                                            RxSharedPrefValues.SHOULD_SHOW_FIRST_ADD_EXPENSE_EDUCATION, false, Scope.Individual
                                        )
                                    )
                                    firstAddExpenseEducationSubject.onNext(false)
                                    goToAddExpenseScreen()
                                } else if (state == MaterialTapTargetPrompt.STATE_NON_FOCAL_PRESSED) {
                                    Analytics.track(
                                        AnalyticsEvents.IN_APP_NOTI_CLICKED,
                                        EventProperties.create()
                                            .with(PropertyKey.TYPE, "add_expense")
                                            .with("focal_area", false)
                                            .with(PropertyKey.SCREEN, PropertyValue.CASH_SALES)
                                    )
                                    pushIntent(
                                        ExpenseManagerContract.Intent.RxPreferenceBoolean(
                                            RxSharedPrefValues.SHOULD_SHOW_FIRST_ADD_EXPENSE_EDUCATION, false, Scope.Individual
                                        )
                                    )
                                    firstAddExpenseEducationSubject.onNext(false)
                                    goToAddExpenseScreen()
                                }
                            }
                        )
                    )
            }
        }
    }

    @UiThread
    override fun reLoad() {
        changeFilter.onNext(filter ?: getCurrentState().filter)
    }

    override fun gotoLogin() {
        activity?.runOnUiThread {
            legacyNavigator.goToLoginScreenForAuthFailure(requireActivity())
        }
    }

    override fun onLongClick(expense: Models.Expense) {
        filter = getCurrentState().filter
        tracker.trackEvents(
            eventName = Event.EXPENSE_LONG_PRESS,
            screen = PropertyValue.EXPENSE,
            propertiesMap = PropertiesMap.create().add("Tx id", expense.id ?: "")
        )
        showDeleteLayout.onNext(expense)
    }

    override fun onClick(expense: Models.Expense) {
        tracker.trackEvents(
            eventName = Event.VIEW_EXPENSE,
            screen = PropertyValue.EXPENSE,
            propertiesMap = PropertiesMap.create().add("Deleted", expense.deletedAt != null)
        )
    }

    override fun onDeleteClicked(expense: Models.Expense) {
        tracker.trackEvents(
            eventName = Event.DELETE_EXPENSE_CLICK,
            screen = PropertyValue.EXPENSE,
            propertiesMap = PropertiesMap.create()
                .add("Tx_id", expense.id ?: "")
                .add("Amount", expense.amount ?: 0)
        )
        showDeleteConfirmDialog.onNext(true)
    }

    override fun onDismiss() {
        hideDeleteLayout.onNext(Unit)
    }

    override fun onDelete(expenseId: String) {
        tracker.trackEvents(
            eventName = Event.DELETE_EXPENSE_CLICK,
            screen = PropertyValue.EXPENSE,
            propertiesMap = PropertiesMap.create()
                .add("Tx_id", expenseId)
        )
        deleteExpense.onNext(expenseId)
    }

    override fun onCancel() {
        tracker.trackEvents(eventName = Event.DELETE_EXPENSE_CANCELLED, screen = PropertyValue.EXPENSE)
        showDeleteConfirmDialog.onNext(false)
    }

    override fun onYouTubeReady(youTubePlayerView: YouTubePlayerView, youTubePlayer: YouTubePlayer) {
        this.youTubePlayerView = youTubePlayerView
        this.youTubePlayer = youTubePlayer
        if (isStateInitialized()) {
            youTubePlayer.loadVideo(getCurrentState().videoUrl, 0F)
            if (expenseYoutubeBinding.youtubePopUp.isVisible) youTubePlayer.play() else youTubePlayer.pause()
        }
    }

    override fun videoStartedListener(youTubeState: String) {
        tracker.trackEvents(
            eventName = Event.YOUTUBE_VIDEO,
            type = PropertyValue.STARTED,
            screen = PropertyValue.EXPENSE,
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
            type = PropertyValue.ENDED,
            screen = PropertyValue.EXPENSE,
            propertiesMap = PropertiesMap.create().add("Video Id", getCurrentState().videoUrl)
        )
    }

    override fun videoOnError(youTubeState: String) {
        context?.shortToast(requireContext().getString(R.string.err_default))
        tracker.trackEvents(
            eventName = Event.YOUTUBE_VIDEO,
            type = PropertyValue.FAILED,
            screen = PropertyValue.EXPENSE,
            propertiesMap = PropertiesMap.create().add("Video Id", getCurrentState().videoUrl)
        )
    }

    override fun onSubmitFeedBack(msg: String) {
        tracker.trackEvents(
            eventName = Event.EXPENSE_FEEDBACK_SUBMIT,
            screen = PropertyValue.EXPENSE
        )
        submitFeedBack.onNext(msg)
        feedBackDialog?.dismiss()
    }

    override fun onDateRangeChanged(start: DateTime, end: DateTime) {
        tracker.trackEvents(
            eventName = Event.UPDATE_DATE_RANGE,
            screen = PropertyValue.EXPENSE,
            propertiesMap = PropertiesMap.create()
                .add("Value", "Date Range")
                .add(
                    "date_range",
                    DateTimeUtils.getFormat2(context, start) + ", " + DateTimeUtils.getFormat2(context, end)
                )
        )
        setDateRange.onNext(Pair(start, end))
    }

    override fun onClearDateRange() {
        tracker.trackEvents(
            eventName = Event.CLEAR_CALENDAR,
            screen = PropertyValue.EXPENSE
        )
        changeFilter.onNext(ExpenseManagerContract.Filter.ALL)
    }

    override fun onBackPressed(): Boolean {
        LocaleManager.fixWebViewLocale(requireContext())
        return super.onBackPressed()
    }

    override fun handleViewEvent(event: ExpenseManagerContract.ViewEvent) {
    }
}
