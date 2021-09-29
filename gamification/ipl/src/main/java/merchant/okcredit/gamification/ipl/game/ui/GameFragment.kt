package merchant.okcredit.gamification.ipl.game.ui

import `in`.okcredit.collection.contract.CollectionNavigator
import `in`.okcredit.customer.contract.CustomerNavigator
import `in`.okcredit.customer.contract.RelationshipType
import `in`.okcredit.merchant.contract.BusinessConstants
import `in`.okcredit.merchant.contract.BusinessNavigator
import `in`.okcredit.merchant.contract.BusinessType
import `in`.okcredit.merchant.contract.BusinessTypeListener
import `in`.okcredit.shared.base.BaseFragment
import `in`.okcredit.shared.base.UserIntent
import android.app.Activity
import android.content.Context
import android.content.res.ColorStateList
import android.graphics.Color
import android.graphics.PorterDuff
import android.graphics.Rect
import android.graphics.drawable.LayerDrawable
import android.os.Bundle
import android.provider.ContactsContract
import android.view.View
import android.widget.ImageView
import android.widget.ProgressBar
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintSet
import androidx.constraintlayout.widget.Group
import androidx.core.content.ContextCompat
import androidx.core.widget.NestedScrollView
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.android.material.snackbar.Snackbar
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.PlayerConstants
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.YouTubePlayer
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.listeners.YouTubePlayerCallback
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.listeners.YouTubePlayerListener
import com.skydoves.balloon.ArrowOrientation
import com.skydoves.balloon.Balloon
import com.skydoves.balloon.BalloonAnimation
import com.skydoves.balloon.OnBalloonClickListener
import com.skydoves.balloon.createBalloon
import dagger.Lazy
import io.reactivex.Observable
import io.reactivex.disposables.Disposable
import io.reactivex.subjects.PublishSubject
import kotlinx.android.synthetic.main.game_rules_new.view.*
import kotlinx.android.synthetic.main.ipl_select_batsman_card.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import merchant.okcredit.gamification.ipl.R
import merchant.okcredit.gamification.ipl.databinding.GameFragmentBinding
import merchant.okcredit.gamification.ipl.databinding.IplSelectTeamCardBinding
import merchant.okcredit.gamification.ipl.game.data.server.model.response.BoosterQuestion
import merchant.okcredit.gamification.ipl.game.ui.GameContract.*
import merchant.okcredit.gamification.ipl.game.ui.epoxy.ItemLoadMore
import merchant.okcredit.gamification.ipl.game.ui.epoxy.batsman.BatsmanController
import merchant.okcredit.gamification.ipl.game.ui.epoxy.batsman.ItemBatman
import merchant.okcredit.gamification.ipl.game.ui.epoxy.bowler.BowlersController
import merchant.okcredit.gamification.ipl.game.ui.epoxy.bowler.ItemBowlers
import merchant.okcredit.gamification.ipl.game.ui.youtube.YoutubeActivity
import merchant.okcredit.gamification.ipl.game.utils.BatsmanStatus
import merchant.okcredit.gamification.ipl.game.utils.BowlingStatus
import merchant.okcredit.gamification.ipl.game.utils.IplEventTracker
import merchant.okcredit.gamification.ipl.game.utils.MatchStatusMapping
import merchant.okcredit.gamification.ipl.rewards.IplRewardsController
import merchant.okcredit.gamification.ipl.utils.IplUtils
import tech.okcredit.android.base.crashlytics.RecordException
import tech.okcredit.android.base.extensions.*
import tech.okcredit.app_contract.LegacyNavigator
import tech.okcredit.base.permission.IPermissionListener
import tech.okcredit.base.permission.Permission
import timber.log.Timber
import java.util.*
import java.util.concurrent.TimeUnit
import java.util.regex.Matcher
import java.util.regex.Pattern
import javax.inject.Inject
import kotlin.math.roundToInt

class GameFragment :
    BaseFragment<State, ViewEvent, Intent>(
        "GameScreen",
        R.layout.game_fragment
    ),
    ItemBatman.BatsmanListener,
    ItemBowlers.BowlersListener,
    ItemLoadMore.LoadMoreListener,
    BoosterQuestionBottomSheet.BoosterSubmitListener {

    companion object {
        fun newInstance() = GameFragment()
        data class GameExpiry(val expired: Boolean, val timeRemaining: String)

        const val REQUEST_ADD_CUSTOMER = 1001
        const val REQUEST_UPDATE_BUSINESS_CATEGORY = 1002
        const val REQUEST_MERCHANT_INPUT_SCREEN = 1003
        const val REQUEST_ADD_CONTACT = 1004
        const val REQUEST_VOICE_COLLECTION = 1005
        const val NICE_CHOICE_DELAY = 2000L
        const val FRC_SUPPORT_NUMBER_KEY = "support_number"

        const val OK_SHOP_APP_LINK =
            "https://play.google.com/store/apps/details?id=in.okcredit.dukaan.onlineshop.nearme"
        const val OKC_NAME = "OKCredit"
        const val INTENT_KEY_FINISH_ACTIVITY_ON_SAVE_COMPLETED = "finishActivityOnSaveCompleted"
    }

    internal val binding: GameFragmentBinding by viewLifecycleScoped(GameFragmentBinding::bind)

    private var timerDisposable: Disposable? = null
    private var boosterTimerDisposable: Disposable? = null
    private var boosterActivationTimerDisposable: Disposable? = null

    private var intentSubject = PublishSubject.create<Intent>()

    private var isTeamCtaClicked = false
    private var isBatsmanCtasSelected = false
    private var isBowlersCtaSelected = false

    @Inject
    lateinit var batsmanController: Lazy<BatsmanController>

    @Inject
    lateinit var bowlersController: Lazy<BowlersController>

    @Inject
    lateinit var legacyNavigator: Lazy<LegacyNavigator>

    @Inject
    lateinit var collectionNavigator: Lazy<CollectionNavigator>

    @Inject
    lateinit var rewardsController: Lazy<IplRewardsController>

    @Inject
    lateinit var iplEventTracker: Lazy<IplEventTracker>

    @Inject
    lateinit var businessNavigator: Lazy<BusinessNavigator>

    @Inject
    lateinit var customerNavigator: Lazy<CustomerNavigator>

    private var snackbar: Snackbar? = null

    private var balloon: Balloon? = null

    private var titleListener: TitleListener? = null

    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (context is TitleListener) {
            titleListener = context
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        iplEventTracker.get().matchOnboardingViewed()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        init()
    }

    private fun init() {
        collapseRulesCard()

        binding.inSelectBatsman.rvBatsman.apply {
            layoutManager = GridLayoutManager(requireActivity(), 2)
            adapter = batsmanController.get().adapter
            setItemSpacingPx(resources.getDimension(R.dimen.view_12dp).roundToInt())
        }

        binding.inSelectBowlersCard.rvBowlers.apply {
            layoutManager = GridLayoutManager(requireActivity(), 2)
            adapter = bowlersController.get().adapter
            setItemSpacingPx(resources.getDimension(R.dimen.view_12dp).roundToInt())
        }

        binding.inSelectTeamCard.tvSelectTeam1.setOnClickListener {
            isTeamCtaClicked = true
            val teamId = getCurrentState().onboardingDetails?.teams?.homeTeam?.id
            intentSubject.onNext(Intent.SelectTeam(teamId!!))
        }

        binding.inSelectTeamCard.tvSelectTeam2.setOnClickListener {
            isTeamCtaClicked = true
            val teamId = getCurrentState().onboardingDetails?.teams?.awayTeam?.id
            intentSubject.onNext(Intent.SelectTeam(teamId!!))
        }

        binding.inProgressCard.viewLiteGreenBg.setOnClickListener {
            showGameToolTip()
        }
        binding.inSelectBatsman.viewShowAllClick.setOnClickListener {
            onBatsmanLoadMore()
        }
        binding.inSelectBowlersCard.viewShowAllClick.setOnClickListener {
            onBowlersLoadMore()
        }

        binding.inError.mbRetry.setOnClickListener {
            intentSubject.onNext(Intent.Retry)
        }

        binding.scrollView.setOnScrollChangeListener(
            NestedScrollView.OnScrollChangeListener { _, _, scrollY, _, oldScrollY ->
                if (scrollY > oldScrollY) {
                    collapseRulesCard()
                }
            }
        )

        binding.rvTodaysRewards.apply {
            addItemDecoration(object : RecyclerView.ItemDecoration() {
                override fun getItemOffsets(
                    outRect: Rect,
                    view: View,
                    parent: RecyclerView,
                    state: RecyclerView.State,
                ) {
                    val spacing = view.context.resources.getDimensionPixelSize(R.dimen.spacing_10)
                    outRect.top = spacing
                    outRect.bottom = spacing
                    outRect.right = spacing
                    outRect.left = spacing
                }
            })
            adapter = rewardsController.get().adapter
        }
        binding.inGameRulesCard.tvHowToPlayGame.setOnClickListener {
            iplEventTracker.get().youtubeSelected(IplEventTracker.Value.TODAYS_TAB_SCREEN)
            YoutubeActivity.start(
                requireActivity(), getCurrentState().youtubeUrl,
                IplEventTracker.Value.MATCH_ONBOARDING
            )
        }
        binding.inGameRulesCard.apply {
            cvGameRules.setOnClickListener {
                val rules = findViewById<Group>(R.id.groupRules)
                if (rules.isVisible()) {
                    findViewById<Group>(R.id.groupRules).gone()
                    findViewById<ImageView>(R.id.ivArrow).rotation = 360f
                    iplEventTracker.get().gameRuleClosed(IplEventTracker.Value.GAME_SCREEN)
                } else {
                    findViewById<Group>(R.id.groupRules).visible()
                    findViewById<ImageView>(R.id.ivArrow).rotation = 180f
                    iplEventTracker.get().gameRuleOpened(IplEventTracker.Value.GAME_SCREEN)
                }
            }
        }
    }

    private fun collapseRulesCard() {
        binding.inGameRulesCard.apply {
            findViewById<Group>(R.id.groupRules).gone()
            findViewById<ImageView>(R.id.ivArrow).rotation = 360f
        }
    }

    override fun loadIntent(): UserIntent {
        return Intent.Load
    }

    override fun userIntents(): Observable<UserIntent> {
        return Observable.mergeArray(
            intentSubject
        )
    }

    override fun render(state: State) {
        setGameProgress(state)
        renderCardVisibility(state)
        renderTeamCard(state)
        renderBatsmanCard(state)
        renderBowlersCard(state)
        renderSelectTeamCardLoading(state)
        renderSelectBatsmanCardLoading(state)
        renderSelectBowlersCardLoading(state)
        renderGameExpired(state)
        renderRewards(state)
        renderPrediction(state)
        renderCardsOneByOne(state)
        renderBoosterQuestion(state)
    }

    private fun renderCardsOneByOne(state: State) {
        if (state.onboardingDetails == null) return

        val showFirstCard =
            state.onboardingDetails.teams.chosenTeam == null && state.onboardingDetails.batsmen.chosenPlayer == null && state.onboardingDetails.bowlers.chosenPlayer == null
        val showSecondCard =
            state.onboardingDetails.teams.chosenTeam != null && state.onboardingDetails.batsmen.chosenPlayer == null && state.onboardingDetails.bowlers.chosenPlayer == null
        val showThirdCard =
            state.onboardingDetails.teams.chosenTeam != null && state.onboardingDetails.batsmen.chosenPlayer != null && state.onboardingDetails.bowlers.chosenPlayer == null
        val showAllCardCard =
            state.onboardingDetails.teams.chosenTeam != null && state.onboardingDetails.batsmen.chosenPlayer != null && state.onboardingDetails.bowlers.chosenPlayer != null

        binding.apply {
            when {
                showFirstCard -> {
                    renderFirstCard()
                }
                showSecondCard -> {
                    renderSecondCard()
                }

                showThirdCard -> {
                    renderThirdCard()
                }
                showAllCardCard -> {
                    renderAllCard()
                }
                else -> {
                    renderOlderCardUI(state)
                }
            }
        }
    }

    private fun renderOlderCardUI(state: State) {
        val referenceIds = intArrayOf(
            R.id.lucky_draw_qualified_card,
            R.id.booster_question_card,
            R.id.tvPleaseComeBack,
            R.id.inSelectTeamCard,
            R.id.team_score_card,
            R.id.inSelectBatsman,
            R.id.batsman_score_card,
            R.id.inSelectBowlersCard,
            R.id.bowler_score_card,
            R.id.bottom_margin
        )
        binding.flowAllCard.referencedIds = referenceIds
        val hasTeamSelected = state.onboardingDetails?.teams?.chosenTeam != null
        if (hasTeamSelected) {
            binding.apply {
                inSelectTeamCard.root.gone()
                teamScoreCard.root.visible()
            }
        } else {
            binding.apply {
                inSelectTeamCard.root.visible()
                teamScoreCard.root.gone()
            }
        }

        val hasBatsmanSelected = state.onboardingDetails?.batsmen?.chosenPlayer != null
        if (hasBatsmanSelected) {
            binding.apply {
                inSelectBatsman.root.gone()
                batsmanScoreCard.root.visible()
            }
        } else {
            binding.apply {
                inSelectBatsman.root.visible()
                batsmanScoreCard.root.gone()
            }
        }

        val hasBowlersSelected = state.onboardingDetails?.bowlers?.chosenPlayer != null
        if (hasBowlersSelected) {
            binding.apply {
                inSelectBowlersCard.root.gone()
                bowlerScoreCard.root.visible()
            }
        } else {
            binding.apply {
                inSelectBowlersCard.root.visible()
                bowlerScoreCard.root.gone()
            }
        }
    }

    private fun renderFirstCard() {
        val referenceIds = intArrayOf(R.id.tvPleaseComeBack, R.id.inSelectTeamCard, R.id.bottom_margin)
        binding.apply {
            inSelectTeamCard.root.visible()
            flowAllCard.referencedIds = referenceIds
        }
    }

    private fun renderSecondCard() {
        binding.inSelectTeamCard.root.gone()
        val referenceIds =
            intArrayOf(R.id.tvPleaseComeBack, R.id.inSelectBatsman, R.id.team_score_card, R.id.bottom_margin)
        binding.flowAllCard.referencedIds = referenceIds
        binding.teamScoreCard.apply {
            root.visible()
            if (isTeamCtaClicked) {
                isTeamCtaClicked = false
                cvTeamScoreCard.setCardBackgroundColor(getColorCompat(R.color.green_lite))
                tvNiceChoice.visible()
                clTeam.gone()
                viewLifecycleOwner.lifecycleScope.launch {
                    withContext(dispatcherProvider.get().main()) {
                        delay(NICE_CHOICE_DELAY)
                        cvTeamScoreCard.setCardBackgroundColor(getColorCompat(R.color.white))
                        tvNiceChoice.gone()
                        clTeam.visible()
                        binding.apply {
                            scrollView.scrollTo(0, 0)
                            inSelectBatsman.root.visible()
                        }
                    }
                }
            } else {
                binding.apply {
                    inSelectBatsman.root.visible()
                    teamScoreCard.root.visible()
                }
            }
        }
    }

    private fun renderThirdCard() {
        binding.inSelectBatsman.root.gone()

        binding.batsmanScoreCard.apply {
            root.visible()
            if (isBatsmanCtasSelected) {
                val referenceIds_ =
                    intArrayOf(
                        R.id.tvPleaseComeBack,
                        R.id.batsman_score_card,
                        R.id.team_score_card,
                        R.id.bottom_margin
                    )
                binding.apply {
                    teamScoreCard.root.visible()
                    flowAllCard.referencedIds = referenceIds_
                }

                isBatsmanCtasSelected = false
                cvBatsmanScoreCard.setCardBackgroundColor(getColorCompat(R.color.green_lite))
                tvNiceChoice.visible()
                clBatsman.gone()
                viewLifecycleOwner.lifecycleScope.launch {
                    withContext(dispatcherProvider.get().main()) {
                        delay(NICE_CHOICE_DELAY)
                        cvBatsmanScoreCard.setCardBackgroundColor(getColorCompat(R.color.white))
                        tvNiceChoice.gone()
                        clBatsman.visible()
                        binding.apply {
                            scrollView.scrollTo(0, 0)
                            inSelectBowlersCard.root.visible()
                        }
                        val referenceIds =
                            intArrayOf(
                                R.id.tvPleaseComeBack,
                                R.id.inSelectBowlersCard,
                                R.id.team_score_card,
                                R.id.batsman_score_card,
                                R.id.bottom_margin
                            )
                        binding.flowAllCard.referencedIds = referenceIds
                    }
                }
            } else {
                val referenceIds =
                    intArrayOf(
                        R.id.tvPleaseComeBack,
                        R.id.inSelectBowlersCard,
                        R.id.team_score_card,
                        R.id.batsman_score_card,
                        R.id.bottom_margin
                    )
                binding.flowAllCard.referencedIds = referenceIds
                binding.apply {
                    inSelectBowlersCard.root.visible()
                    teamScoreCard.root.visible()
                    batsmanScoreCard.root.visible()
                }
            }
        }
    }

    private fun renderAllCard() {

        binding.apply {
            teamScoreCard.root.visible()
            inSelectBowlersCard.root.gone()
            inSelectTeamCard.root.gone()
            inSelectBatsman.root.gone()
        }

        binding.bowlerScoreCard.apply {
            root.visible()
            if (isBowlersCtaSelected) {
                val referenceIds_ =
                    intArrayOf(
                        R.id.tvPleaseComeBack,
                        R.id.lucky_draw_qualified_card,
                        R.id.booster_question_card,
                        R.id.bowler_score_card,
                        R.id.batsman_score_card,
                        R.id.team_score_card,
                        R.id.bottom_margin
                    )
                binding.apply {
                    batsmanScoreCard.root.visible()
                    flowAllCard.referencedIds = referenceIds_
                }

                isBowlersCtaSelected = false
                cvBowlersScoreCard.setCardBackgroundColor(getColorCompat(R.color.green_lite))
                tvNiceChoice.visible()
                clBowlers.gone()
                viewLifecycleOwner.lifecycleScope.launch {
                    withContext(dispatcherProvider.get().main()) {
                        delay(NICE_CHOICE_DELAY)
                        cvBowlersScoreCard.setCardBackgroundColor(getColorCompat(R.color.white))
                        tvNiceChoice.gone()
                        clBowlers.visible()
                        val referenceIds = intArrayOf(
                            R.id.lucky_draw_qualified_card,
                            R.id.booster_question_card,
                            R.id.team_score_card,
                            R.id.batsman_score_card,
                            R.id.bowler_score_card,
                            R.id.bottom_margin
                        )
                        binding.apply {
                            scrollView.scrollTo(0, 0)
                            bowlerScoreCard.root.visible()
                            batsmanScoreCard.root.visible()
                            teamScoreCard.root.visible()
                            flowAllCard.referencedIds = referenceIds
                        }
                    }
                }
            } else {
                val referenceIds = intArrayOf(
                    R.id.lucky_draw_qualified_card,
                    R.id.booster_question_card,
                    R.id.team_score_card,
                    R.id.batsman_score_card,
                    R.id.bowler_score_card,
                    R.id.bottom_margin
                )
                binding.apply {
                    flowAllCard.referencedIds = referenceIds
                }
            }
        }
    }

    private fun renderRewards(state: State) {
        rewardsController.get().setData(state.rewards)
    }

    private fun renderBoosterQuestion(state: State) {
        if (state.boosterQuestion == null || state.isMatchOver()) {
            if (state.shouldShowPendingBooster()) {
                showPendingBoosterCard(state.onboardingDetails?.boosterStartTime ?: 0L)
            } else if (binding.boosterQuestionCard.boosterCard.visibility == View.VISIBLE && state.isBoosterCompleted()) {
                // if the user has completed the booster task then show him the success message and then hide booster
                boosterTimerDisposable?.dispose()
                lifecycleScope.launch {
                    binding.boosterQuestionCard.tvBoosterSuccess.text =
                        getString(R.string.got_3x_mutipler, state.boosterCompletedMultiplier)
                    binding.boosterQuestionCard.tvBoosterSuccess.visible()
                    binding.boosterQuestionCard.contentGroup.invisible()
                    delay(2_000)
                    binding.boosterQuestionCard.boosterCard.gone()
                    showQualifiedCard()
                }
            } else {
                binding.boosterQuestionCard.boosterCard.gone()
                if (state.isBoosterCompleted()) {
                    showQualifiedCard()
                }
            }
            return
        }

        bindBoosterData(state.boosterQuestion)
    }

    private fun bindBoosterData(boosterQuestion: BoosterQuestion.Question) {
        binding.boosterQuestionCard.apply {
            tvBoosterBody.text = getString(
                R.string.booster_question_formatted,
                boosterQuestion.text,
                boosterQuestion.multiplier.toString()
            )
            if (boosterQuestion.youtubeLink.isNotNullOrBlank()) {
                buttonSubmit.icon = getDrawableCompact(R.drawable.ic_play)
                buttonSubmit.text = getString(R.string.play_booster_video)
                buttonSubmit.tag = boosterQuestion.youtubeLink
            } else {
                buttonSubmit.icon = null
                buttonSubmit.text = boosterQuestion.cta
            }
            buttonSubmit.setOnClickListener {
                // play youtube video if set as tag
                val youtubeLink = it.tag as String?
                if (youtubeLink.isNotNullOrBlank()) {
                    binding.boosterQuestionCard.youtubePlayerView.visible()
                    binding.boosterQuestionCard.contentGroup.gone()
                    youtubeLink?.let { it1 -> initializePlayer(it1) }
                } else {
                    pushIntent(Intent.BoosterClicked)
                }
            }
            tvTime.visible()
            tvBoosterTimer.gone()
            buttonSubmit.visible()
            tvBoosterSuccess.gone()
            boosterActivationTimerDisposable?.dispose()
            boosterCard.visible()
        }

        calculateBoosterExpiryDate(expiryTime = boosterQuestion.expiryTime)
    }

    private fun calculateBoosterExpiryDate(expiryTime: Long) {
        boosterTimerDisposable = Observable.interval(1000, TimeUnit.MILLISECONDS)
            .map {
                val currentDateTime = IplUtils.getCurrentDateTime()
                val expiryDateTime = Date(expiryTime * 1000)
                val remainingTime = IplUtils.getEndsInTime(currentDateTime, expiryDateTime, requireContext())
                GameExpiry(IplUtils.hasGameExpired(expiryTime), remainingTime)
            }
            .subscribeOn(schedulerProvider.get().io())
            .observeOn(schedulerProvider.get().ui())
            .subscribe {
                if (it.expired) {
                    pushIntent(Intent.BoosterExpired)
                    boosterTimerDisposable?.dispose()
                } else {
                    binding.boosterQuestionCard.tvTime.text = getString(R.string.ends_in, it.timeRemaining)
                }
            }
    }

    private fun setGameProgress(state: State) {
        binding.inProgressCard.apply {
            tvPercentage.text = getString(R.string.ipl_progress, state.progressCardState.progress.toString())
            tvTotalPoints.text = state.totalPoints.toString()
            if (state.boosterCompletedMultiplier != null && state.boosterCompletedMultiplier > 1) {
                multiplierGroup.visible()
                val constraintSet = ConstraintSet()
                constraintSet.clone(progressConstraint)
                constraintSet.connect(
                    R.id.tvTotalPoints,
                    ConstraintSet.END,
                    R.id.tvPointLabel,
                    ConstraintSet.END,
                    0
                )
                constraintSet.clear(R.id.tvTotalPoints, ConstraintSet.START)
                constraintSet.applyTo(progressConstraint)
                tvMultiplier.text = getString(R.string.formatted_multiplier, state.boosterCompletedMultiplier)
                tvPoints.text = (state.totalPoints / state.boosterCompletedMultiplier).toString()
            } else {
                val constraintSet = ConstraintSet()
                constraintSet.clone(progressConstraint)
                constraintSet.connect(
                    R.id.tvTotalPoints,
                    ConstraintSet.END,
                    R.id.tvPointLabel,
                    ConstraintSet.END,
                    0
                )
                constraintSet.connect(
                    R.id.tvTotalPoints,
                    ConstraintSet.START,
                    R.id.tvPointLabel,
                    ConstraintSet.START,
                    0
                )
                constraintSet.applyTo(progressConstraint)
                multiplierGroup.gone()
            }
        }
        val completedSteps = state.progressCardState.completedSteps
        when {
            completedSteps <= 0 -> setNoStepsCompleted()
            completedSteps == 1 -> setOneStepCompleted()
            completedSteps == 2 -> setTwoStepsCompleted()
            completedSteps == 3 -> setThreeStepsCompleted()
            completedSteps >= 4 -> setAllStepsCompleted()
        }
    }

    private fun showQualifiedCard() {
        binding.luckyDrawQualifiedCard.tvBoosterTitle.text = getString(R.string.today_lucky_draw)
        binding.luckyDrawQualifiedCard.tvTime.text = getString(R.string.today_lucky_draw_available)
        binding.luckyDrawQualifiedCard.root.visible()
    }

    private fun showPendingBoosterCard(boosterStartTime: Long) {
        binding.boosterQuestionCard.apply {
            tvTime.invisible()
            tvBoosterBody.text = getString(R.string.booster_available_in)
            tvBoosterTimer.visible()
            buttonSubmit.invisible()
            boosterCard.visible()
            calculateBoosterActivationTimer(boosterStartTime)
        }
    }

    private fun calculateBoosterActivationTimer(boosterStartTime: Long) {
        if (boosterActivationTimerDisposable != null || boosterStartTime == 0L) return

        val expiryDateTime = Date(boosterStartTime * 1000)
        boosterActivationTimerDisposable = Observable.interval(1000, TimeUnit.MILLISECONDS)
            .takeUntil {
                Calendar.getInstance().timeInMillis >= expiryDateTime.time
            }
            .map {
                val currentDateTime = IplUtils.getCurrentDateTime()
                IplUtils.getEndsInTime(currentDateTime, expiryDateTime, requireContext())
            }
            .subscribeOn(schedulerProvider.get().io())
            .observeOn(schedulerProvider.get().ui())
            .doOnComplete {
                binding.boosterQuestionCard.tvBoosterTimer.text = getString(R.string.seconds, "0")
            }
            .subscribe {
                binding.boosterQuestionCard.tvBoosterTimer.text = it
            }
    }

    private fun setAllStepsCompleted() {
        binding.inProgressCard.apply {
            ivTick1.background = getDrawableCompact(R.drawable.ic_green_tick)
            viewProgress1.background = getDrawableCompact(R.drawable.bg_no_dash)
            ivTick1.text = ""

            ivTick2.background = getDrawableCompact(R.drawable.ic_green_tick)
            viewProgress2.background = getDrawableCompact(R.drawable.bg_no_dash)
            ivTick2.text = ""

            ivTick3.background = getDrawableCompact(R.drawable.ic_green_tick)
            viewProgress3.background = getDrawableCompact(R.drawable.bg_no_dash)
            ivTick3.text = ""

            ivTick4.background = getDrawableCompact(R.drawable.ic_green_tick)
            ivTick4.text = ""
        }
    }

    private fun setThreeStepsCompleted() {
        binding.inProgressCard.apply {
            ivTick1.background = getDrawableCompact(R.drawable.ic_green_tick)
            viewProgress1.background = getDrawableCompact(R.drawable.bg_no_dash)
            ivTick1.text = ""

            ivTick2.background = getDrawableCompact(R.drawable.ic_green_tick)
            viewProgress2.background = getDrawableCompact(R.drawable.bg_no_dash)
            ivTick2.text = ""

            ivTick3.background = getDrawableCompact(R.drawable.ic_green_tick)
            viewProgress3.background = getDrawableCompact(R.drawable.bg_dash)
            ivTick3.text = ""

            ivTick4.background = getDrawableCompact(R.drawable.bg_circle)
            ivTick4.text = "4"
        }
    }

    private fun setTwoStepsCompleted() {
        binding.inProgressCard.apply {
            ivTick1.background = getDrawableCompact(R.drawable.ic_green_tick)
            viewProgress1.background = getDrawableCompact(R.drawable.bg_no_dash)
            ivTick1.text = ""

            ivTick2.background = getDrawableCompact(R.drawable.ic_green_tick)
            viewProgress2.background = getDrawableCompact(R.drawable.bg_dash)
            ivTick2.text = ""

            ivTick3.background = getDrawableCompact(R.drawable.bg_circle)
            ivTick3.text = "3"
            viewProgress3.background = getDrawableCompact(R.drawable.bg_grey_300)

            ivTick4.background = getDrawableCompact(R.drawable.bg_circle)
            ivTick4.text = "4"
        }
    }

    private fun setOneStepCompleted() {
        binding.inProgressCard.apply {
            ivTick1.background = getDrawableCompact(R.drawable.ic_green_tick)
            viewProgress1.background = getDrawableCompact(R.drawable.bg_dash)
            ivTick1.text = ""

            ivTick2.background = getDrawableCompact(R.drawable.bg_circle)
            ivTick2.text = "2"
            viewProgress2.background = getDrawableCompact(R.drawable.bg_grey_300)

            ivTick3.background = getDrawableCompact(R.drawable.bg_circle)
            ivTick3.text = "3"
            viewProgress3.background = getDrawableCompact(R.drawable.bg_grey_300)

            ivTick4.background = getDrawableCompact(R.drawable.bg_circle)
            ivTick4.text = "4"
        }
    }

    private fun setNoStepsCompleted() {
        binding.inProgressCard.apply {
            ivTick1.background = getDrawableCompact(R.drawable.bg_circle)
            ivTick1.text = "1"
            viewProgress1.background = getDrawableCompact(R.drawable.bg_grey_300)

            ivTick2.background = getDrawableCompact(R.drawable.bg_circle)
            ivTick2.text = "2"
            viewProgress2.background = getDrawableCompact(R.drawable.bg_grey_300)

            ivTick3.background = getDrawableCompact(R.drawable.bg_circle)
            ivTick3.text = "3"
            viewProgress3.background = getDrawableCompact(R.drawable.bg_grey_300)

            ivTick4.background = getDrawableCompact(R.drawable.bg_circle)
            ivTick4.text = "4"
        }
    }

    private fun renderTeamCard(state: State) {
        if (state.onboardingDetails == null) return
        val teamCard = state.onboardingDetails.teams
        titleListener?.updateTitle(
            getString(
                R.string.match_title,
                teamCard.homeTeam.shortName,
                teamCard.awayTeam.shortName,
                teamCard.matchNumber
            )
        )
        val hasTeamSelected = teamCard.chosenTeam != null
        if (hasTeamSelected) {

            binding.teamScoreCard.apply {
                tvSeriesName.text =
                    getString(R.string.series_name, teamCard.seriesName, teamCard.matchNumber.toString())
                tvSelectTeamHome.text = teamCard.homeTeam.shortName
                tvSelectTeamAway.text = teamCard.awayTeam.shortName

                if (teamCard.chosenTeam!!.id == teamCard.homeTeam.id) {
                    homeTeamChosenHint.visible()
                    awayTeamChosenHint.gone()
                } else {
                    homeTeamChosenHint.gone()
                    awayTeamChosenHint.visible()
                }

                Glide.with(requireActivity())
                    .load(teamCard.homeTeam.logoLink)
                    .placeholder(R.drawable.ic_user)
                    .error(R.drawable.ic_user)
                    .fallback(R.drawable.ic_user)
                    .thumbnail(0.25f)
                    .into(ivTeamHome)
                Glide.with(requireActivity())
                    .load(teamCard.awayTeam.logoLink)
                    .placeholder(R.drawable.ic_user)
                    .error(R.drawable.ic_user)
                    .fallback(R.drawable.ic_user)
                    .thumbnail(0.25f)
                    .into(ivTeamAway)

                state.homeTeamScore?.let {
                    tvTeamHomeScore.text =
                        getString(
                            R.string.team_score,
                            it.runs.toString(),
                            it.wickets.toString(),
                            it.overs.toString(),
                            it.balls.toString()
                        )
                }

                state.awayTeamScore?.let {
                    tvTeamAwayScore.text =
                        getString(
                            R.string.team_score,
                            it.runs.toString(),
                            it.wickets.toString(),
                            it.overs.toString(),
                            it.balls.toString()
                        )
                }

                val status = getString(MatchStatusMapping.fromStatus(state.matchStatusText).resource)
                if (state.matchStatusText.isBlank() || status.isBlank()) {
                    tvLive.gone()
                    matchTime.text = IplUtils.getStartTime(state.onboardingDetails.teams.startTime)
                    matchTime.visible()
                } else {
                    tvLive.text = status
                    tvLive.visible()
                    matchTime.gone()
                }

                when (state.chosenTeamWon) {
                    true -> {
                        viewLineSeparator.visible()
                        tvWinStatus.text =
                            getString(R.string.win_status, state.winningTeamName, state.winPoints.toString())
                        tvWinStatus.visible()
                    }
                    false -> {
                        viewLineSeparator.visible()
                        tvWinStatus.text = getString(R.string.lose_status, state.winningTeamName)
                        tvWinStatus.visible()
                    }
                    null -> {
                        viewLineSeparator.gone()
                        tvWinStatus.gone()
                    }
                }
            }
        } else {
            binding.inSelectTeamCard.apply {
                tvTeamHomeName.text = teamCard.homeTeam.name
                tvTeamAwayName.text = teamCard.awayTeam.name
                tvMatchDate.text = IplUtils.getStartTime(state.onboardingDetails.expiryTime)
                Glide.with(requireActivity())
                    .load(teamCard.homeTeam.logoLink)
                    .placeholder(R.drawable.ic_user)
                    .error(R.drawable.ic_user)
                    .fallback(R.drawable.ic_user)
                    .thumbnail(0.25f)
                    .into(ivTeam1)

                Glide.with(requireActivity())
                    .load(teamCard.awayTeam.logoLink)
                    .placeholder(R.drawable.ic_user)
                    .error(R.drawable.ic_user)
                    .fallback(R.drawable.ic_user)
                    .thumbnail(0.25f)
                    .into(ivTeam2)
            }
        }
    }

    private fun renderPrediction(state: State) {
        val prediction = state.prediction
        val onboarding = state.onboardingDetails
        if (prediction == null || onboarding == null) return

        val homeTeamName = onboarding.teams.homeTeam.shortName.toUpperCase(Locale.getDefault())
        val homePrediction = prediction.matchPrediction.homeTeam.prediction
        val homeColor = prediction.matchPrediction.homeTeam.colorCode

        val awayTeamName = onboarding.teams.awayTeam.shortName.toUpperCase(Locale.getDefault())
        val awayPrediction = prediction.matchPrediction.awayTeam.prediction
        val awayColor = prediction.matchPrediction.awayTeam.colorCode

        var homeParsedColor = getColorCompat(R.color.red_lite_1)
        var awayParsedColor = getColorCompat(R.color.orange_lite_1)
        try {
            homeParsedColor = Color.parseColor(homeColor)
            awayParsedColor = Color.parseColor(awayColor)
        } catch (e: Exception) {
            RecordException.recordException(e)
        }

        binding.inSelectTeamCard.apply {
            tvSelectTeamHomePrediction.text =
                getString(R.string.prediction_value, homeTeamName, homePrediction.toString())
            tvSelectTeamAwayPrediction.text =
                getString(R.string.prediction_value, awayTeamName, awayPrediction.toString())
            setProgress(pbSelectPrediction, homeParsedColor, awayParsedColor)
            pbSelectPrediction.progress = homePrediction
        }

        binding.teamScoreCard.apply {
            tvTeamHome.text =
                getString(R.string.prediction_value, homeTeamName, homePrediction.toString())
            tvTeamAway.text =
                getString(R.string.prediction_value, awayTeamName, awayPrediction.toString())
            setProgress(pbPrediction, homeParsedColor, awayParsedColor)
            pbPrediction.progress = homePrediction
        }
    }

    private fun setProgress(progressBar: ProgressBar, primaryColor: Int, secondaryColor: Int) {
        try {
            val layers = progressBar.progressDrawable as LayerDrawable
            layers.getDrawable(0).setColorFilter(primaryColor, PorterDuff.Mode.SRC_IN)
            layers.getDrawable(1).setColorFilter(secondaryColor, PorterDuff.Mode.SRC_IN)
        } catch (e: Exception) {
            RecordException.recordException(e)
        }
    }

    private fun renderBatsmanCard(state: State) {
        if (state.onboardingDetails == null) return

        val batsmanCard = state.onboardingDetails.batsmen
        val hasBatsmanSelected = batsmanCard.chosenPlayer != null
        if (hasBatsmanSelected) {
            binding.inSelectBatsman.root.gone()
            binding.batsmanScoreCard.root.visible()
            binding.batsmanScoreCard.apply {
                tvBatmanName.text = batsmanCard.chosenPlayer?.name

                if (state.batsmanScore == null) {
                    tvBatsmanStatus.setText(BatsmanStatus.YET_TO_BAT.resource)
                    tvBatsmanStatus.visible()
                } else {
                    state.batsmanScore.let {
                        if (it.batting_state.isBlank()) {
                            tvBatsmanStatus.setText(BatsmanStatus.YET_TO_BAT.resource)
                            tvBatsmanStatus.visible()
                        } else {
                            val status = getString(BatsmanStatus.fromStatus(it.batting_state).resource)
                            if (status.isBlank()) {
                                tvBatsmanStatus.gone()
                            } else {
                                tvBatsmanStatus.text = status
                                tvBatsmanStatus.visible()
                            }
                            val runs = if (it.batting_runs == null) {
                                ""
                            } else {
                                resources.getQuantityString(R.plurals.runs, it.batting_runs, it.batting_runs)
                            }

                            val balls = if (it.batting_balls == null) {
                                ""
                            } else {
                                resources.getQuantityString(R.plurals.balls, it.batting_balls, it.batting_balls)
                            }

                            tvBatsmanScore.text = if (runs.isNotBlank() && balls.isNotBlank()) {
                                getString(R.string.batsman_score, runs, balls)
                            } else if (balls.isBlank()) {
                                runs
                            } else {
                                balls
                            }
                        }
                    }
                }
                tvMatchVs.text = getString(
                    R.string.team_vs_team,
                    state.onboardingDetails.teams.homeTeam.shortName.toUpperCase(Locale.getDefault()),
                    state.onboardingDetails.teams.awayTeam.shortName.toUpperCase(Locale.getDefault())
                )
            }
        } else {
            binding.apply {
                tvMatchDate.text = IplUtils.getStartTime(state.onboardingDetails.expiryTime)
                inSelectBatsman.tvMatchDate.text = IplUtils.getStartTime(state.onboardingDetails.expiryTime)
                batsmanController.get().setData(state)
            }
        }
    }

    private fun renderBowlersCard(state: State) {
        if (state.onboardingDetails == null) return

        val bowlersCard = state.onboardingDetails.bowlers
        val hasBowlersSelected = bowlersCard.chosenPlayer != null
        if (hasBowlersSelected) {
            binding.bowlerScoreCard.apply {
                tvBowlersName.text = bowlersCard.chosenPlayer?.name

                if (state.bowlerScore == null) {
                    tvBowlerStatus.setText(BowlingStatus.YET_TO_BOWL.resource)
                } else {
                    state.bowlerScore.let {
                        tvBowlerStatus.setText(BowlingStatus.fromStatus(it.bowling_state).resource)
                        if (state.bowlerScore.bowling_state.isNotNullOrBlank()) {
                            val wickets = if (it.bowling_wickets == null) {
                                ""
                            } else {
                                resources.getQuantityString(R.plurals.wickets, it.bowling_wickets, it.bowling_wickets)
                            }

                            val overs = if (it.bowling_overs == null || it.bowling_balls == null) {
                                ""
                            } else {
                                getString(R.string.overs, it.bowling_overs.toString(), it.bowling_balls.toString())
                            }

                            tvBowlerScore.text = if (wickets.isNotBlank() && overs.isNotBlank()) {
                                getString(R.string.bowler_score, wickets, overs)
                            } else if (wickets.isBlank()) {
                                overs
                            } else {
                                wickets
                            }
                        }
                    }
                }
                tvMatchVs.text = getString(
                    R.string.team_vs_team,
                    state.onboardingDetails.teams.homeTeam.shortName.toUpperCase(Locale.getDefault()),
                    state.onboardingDetails.teams.awayTeam.shortName.toUpperCase(Locale.getDefault())
                )
            }
        } else {
            binding.apply {

                inSelectBowlersCard.tvMatchDate.text = IplUtils.getStartTime(state.onboardingDetails.expiryTime)
                bowlersController.get().setData(state)
            }
        }
    }

    private fun renderCardVisibility(state: State) {
        binding.apply {
            when {
                state.isLoading -> {
                    groupCards.gone()
                    inLoading.root.visible()
                    inError.root.gone()
                }
                state.networkError -> {
                    groupCards.gone()
                    inLoading.root.gone()
                    inError.root.visible()
                    inError.apply {
                        tvError.text = getString(R.string.interent_error)
                        ivError.setImageResource(R.drawable.bg_network_error)
                    }
                }
                state.serverError -> {
                    groupCards.gone()
                    inLoading.root.gone()
                    inError.root.visible()
                    inError.apply {
                        tvError.text = getString(R.string.err_default)
                        ivError.setImageResource(R.drawable.bg_server_error)
                    }
                }
                else -> {
                    groupCards.visible()
                    inLoading.root.gone()
                    inError.root.gone()
                    if (state.rewards.isNullOrEmpty()) {
                        tvRewards.gone()
                        bottomMargin.visible()
                    } else {
                        tvRewards.visible()
                        bottomMargin.gone()
                    }
                }
            }
        }
    }

    private fun renderSelectTeamCardLoading(state: State) {
        binding.inSelectTeamCard.apply {
            if (state.isTeamSelectLoading) {
                pbSelectTeam.visible()
                disableSelectTeamCta()
            } else {
                pbSelectTeam.gone()
                enableSelectTeamCta()
            }
        }
    }

    private fun renderSelectBatsmanCardLoading(state: State) {
        binding.inSelectBatsman.apply {
            if (state.isBatsmanSelectLoading) {
                pbSelectBatsman.visible()
            } else {
                pbSelectBatsman.gone()
            }

            if (state.showAllBatsman) {
                viewLineSeparator.gone()
                ivArrow.gone()
                tvShowAllPlayers.gone()
            }
        }
    }

    private fun renderSelectBowlersCardLoading(state: State) {
        binding.inSelectBowlersCard.apply {
            if (state.isBowlersSelectLoading) {
                pbSelectBowler.visible()
            } else {
                pbSelectBowler.gone()
            }
            if (state.showAllBowler) {
                viewLineSeparator.gone()
                ivArrow.gone()
                tvShowAllPlayers.gone()
            }
        }
    }

    override fun handleViewEvent(event: ViewEvent) {
        when (event) {
            is ViewEvent.ShowError -> shortToast(event.error)
            is ViewEvent.UpdateMerchantAbout -> goMerchantInputScreen(
                BusinessConstants.ABOUT,
                getString(R.string.about)
            )
            is ViewEvent.UpdateBusinessCategory -> goToMerchantBusinessCategory()
            is ViewEvent.UpdateMerchantAddress -> goToAddressScreen()
            is ViewEvent.UpdateBusinessName -> goMerchantInputScreen(
                BusinessConstants.BUSINESS_NAME,
                getString(R.string.title_business_name)
            )
            is ViewEvent.UpdateMerchantEmail -> goMerchantInputScreen(
                BusinessConstants.EMAIL,
                getString(R.string.email)
            )
            is ViewEvent.UpdateMerchantName -> goMerchantInputScreen(
                BusinessConstants.PERSON_NAME,
                getString(R.string.contact_person)
            )
            is ViewEvent.AddCustomer -> goToAddCustomer()
            is ViewEvent.SetupBankAccount -> goToSetupBankAccount()
            is ViewEvent.ShowMCQ -> showBoosterBottomSheet(event.boosterQuestion)
            is ViewEvent.ShowOpenQuestion -> showBoosterBottomSheet(event.boosterQuestion)
            is ViewEvent.RateUs -> goToPlayStore(event.appLink)
            is ViewEvent.ShareBusinessCard -> goToShareBusinessCard()
            is ViewEvent.UpdateBusinessType -> goToBusinessTypes(event.list)
            is ViewEvent.ShareApp -> goToShareApp(event.intent)
            is ViewEvent.AddOkcNumberInPhone -> addOkcNumberInDevice()
            is ViewEvent.InstallOkShopApp -> goToPlayStore(event.appLink)
            is ViewEvent.ShowSnackBar -> showSnackbar(event.error)
            is ViewEvent.VoiceCollection -> goToVoiceCollectionScreen(event.text)
        }
    }

    private fun goToVoiceCollectionScreen(text: String) {
        startActivityForResult(
            `in`.okcredit.voice_first.ui.voice_collection.BoosterVoiceCollectionActivity.startIntent(requireContext(), text),
            REQUEST_VOICE_COLLECTION
        )
    }

    private fun goToShareApp(intent: android.content.Intent) {
        pushIntent(Intent.BoosterTaskCompleted)
        startActivity(intent)
    }

    private fun goToBusinessTypes(list: List<BusinessType>) {
        businessNavigator.get().showBusinessTypeDialog(
            childFragmentManager,
            list,
            object : BusinessTypeListener {
                override fun onSelectBusinessType(type: BusinessType) {
                    showSuccessSheet(getString(R.string.message_business_type_success), type)
                }
            }
        )
    }

    private fun goToPlayStore(playStoreLink: String) {
        pushIntent(Intent.BoosterTaskCompleted)
        legacyNavigator.get().goToPlayStore(requireActivity())
    }

    private fun goToShareBusinessCard() {
        pushIntent(Intent.BoosterTaskCompleted)
        legacyNavigator.get().goToMerchantProfileForBusinessCardShare(requireContext())
    }

    private fun showBoosterBottomSheet(question: BoosterQuestion.Question) {
        BoosterQuestionBottomSheet.getInstance(question).apply {
            setListener(this@GameFragment)
        }.show(childFragmentManager, BoosterQuestionBottomSheet::class.java.simpleName)
    }

    private fun goToAddCustomer() {
        val intent = customerNavigator.get().goToAddRelationshipActivity(
            requireContext(),
            RelationshipType.ADD_CUSTOMER,
            canShowTutorial = false,
            showManualFlow = false,
        )
        this@GameFragment.startActivityForResult(
            intent,
            REQUEST_ADD_CUSTOMER
        )
    }

    internal fun goMerchantInputScreen(inputType: Int, title: String) {
        legacyNavigator.get().goToMerchantInputScreenForResult(
            fragment = this,
            inputType = inputType,
            inputTitle = title,
            requestCode = REQUEST_MERCHANT_INPUT_SCREEN
        )
    }

    private fun goToSetupBankAccount() {
        collectionNavigator.get().showMerchantDestinationDialog(
            childFragmentManager,
            asyncRequest = true,
            source = IplEventTracker.Value.SOURCE_BOOSTER_QUESTION
        )
    }

    private fun goToMerchantBusinessCategory() {
        val intent = legacyNavigator.get().getCategoryScreenIntent()
        startActivityForResult(intent, REQUEST_UPDATE_BUSINESS_CATEGORY)
    }

    private fun goToAddressScreen() {
        Permission.requestLocationPermission(
            activity as AppCompatActivity,
            object : IPermissionListener {
                override fun onPermissionGrantedFirstTime() {}

                override fun onPermissionGranted() {
                    goMerchantInputScreen(BusinessConstants.ADDRESS, getString(R.string.address))
                }

                override fun onPermissionDenied() {
                    goMerchantInputScreen(BusinessConstants.ADDRESS, getString(R.string.address))
                }

                override fun onPermissionPermanentlyDenied() {}
            }
        )
    }

    override fun onBatsmanSelected(id: String) {
        isBatsmanCtasSelected = true
        intentSubject.onNext(Intent.SelectBatsman(id))
    }

    override fun onBowlerSelected(id: String) {
        isBowlersCtaSelected = true
        intentSubject.onNext(Intent.SelectBowlers(id))
    }

    private fun showSnackbar(errorMessage: Int) {
        if (snackbar == null || snackbar?.isShown == false) {
            snackbar = view?.snackbar(getString(errorMessage), Snackbar.LENGTH_INDEFINITE)
            snackbar?.show()
            viewLifecycleOwner.lifecycleScope.launch {
                delay(2000L)
                snackbar?.dismiss()
            }
        }
    }

    private fun renderGameExpired(state: State) {
        if (state.onboardingDetails == null) return

        if (state.gameExpired || IplUtils.hasGameExpired(state.onboardingDetails.expiryTime)) {
            timerDisposable?.dispose()
            intentSubject.onNext(Intent.GameExpired)
            disableAllCards()
            renderGameExpiredText(state)
        } else {
            calculateExpiryDate(state.onboardingDetails.expiryTime)
        }
    }

    private fun renderGameExpiredText(state: State) {
        if (state.onboardingDetails == null) return
        val hasTeamNotSelected = state.onboardingDetails.teams.chosenTeam == null
        val hasBatsmanNotSelected = state.onboardingDetails.batsmen.chosenPlayer == null
        val hasBowlersNotSelected = state.onboardingDetails.bowlers.chosenPlayer == null

        if (hasTeamNotSelected || hasBatsmanNotSelected || hasBowlersNotSelected) {
            binding.tvPleaseComeBack.visible()
        } else {
            binding.tvPleaseComeBack.gone()
        }
    }

    private fun disableAllCards() {
        binding.apply {
            inSelectTeamCard.tvExpiryTime.text = getString(R.string.expired)
            inSelectBatsman.tvExpiryTime.text = getString(R.string.expired)
            inSelectBowlersCard.tvExpiryTime.text = getString(R.string.expired)

            inSelectTeamCard.apply {
                viewOrangeBg.setBackgroundColor(getColorCompat(R.color.grey600))
                tvCardNumber.setTextColor(getColorCompat(R.color.grey600))
                groupPrediction.gone()
                ivTeam1.greyOut()
                ivTeam2.greyOut()
                disableSelectTeamCta()
            }

            inSelectBatsman.apply {
                viewShowAllClick.disable()
                viewOrangeBg.setBackgroundColor(getColorCompat(R.color.grey600))
                tvShowAllPlayers.setTextColor(getColorCompat(R.color.grey600))
                ivArrow.setColorFilter(getColorCompat(R.color.grey600), PorterDuff.Mode.SRC_IN)
                tvCardNumber.setTextColor(getColorCompat(R.color.grey600))
            }

            inSelectBowlersCard.apply {
                viewShowAllClick.disable()
                tvShowAllPlayers.setTextColor(getColorCompat(R.color.grey600))
                ivArrow.setColorFilter(getColorCompat(R.color.grey600), PorterDuff.Mode.SRC_IN)
                viewOrangeBg.setBackgroundColor(getColorCompat(R.color.grey600))
                tvCardNumber.setTextColor(getColorCompat(R.color.grey600))
            }
        }
    }

    private fun calculateExpiryDate(expiryTime: Long) {
        timerDisposable = Observable.interval(1000, TimeUnit.MILLISECONDS)
            .map {
                val currentDateTime = IplUtils.getCurrentDateTime()
                val expiryDateTime = Date(expiryTime * 1000)
                val remainingTime = IplUtils.getEndsInTime(currentDateTime, expiryDateTime, requireContext())
                GameFragment.Companion.GameExpiry(IplUtils.hasGameExpired(expiryTime), remainingTime)
            }
            .subscribeOn(schedulerProvider.get().io())
            .observeOn(schedulerProvider.get().ui())
            .subscribe {
                if (it.expired) {
                    intentSubject.onNext(Intent.GameExpired)
                    timerDisposable?.dispose()
                } else {
                    binding.apply {
                        inSelectTeamCard.tvExpiryTime.text = getString(R.string.ends_in, it.timeRemaining)
                        inSelectBatsman.tvExpiryTime.text = getString(R.string.ends_in, it.timeRemaining)
                        inSelectBowlersCard.tvExpiryTime.text = getString(R.string.ends_in, it.timeRemaining)
                    }
                }
            }
    }

    private fun IplSelectTeamCardBinding.disableSelectTeamCta() {
        tvSelectTeam1.setTextColor(getColorCompat(R.color.white))
        tvSelectTeam2.setTextColor(getColorCompat(R.color.white))

        tvSelectTeam1.elevation = 0f
        tvSelectTeam1.backgroundTintList = getColorStateListCompat(R.color.grey400)
        tvSelectTeam1.isEnabled = false

        tvSelectTeam2.elevation = 0f
        tvSelectTeam2.backgroundTintList = getColorStateListCompat(R.color.grey400)
        tvSelectTeam2.isEnabled = false
    }

    private fun IplSelectTeamCardBinding.enableSelectTeamCta() {
        tvSelectTeam1.setTextColor(getColorCompat(R.color.white))
        tvSelectTeam2.setTextColor(getColorCompat(R.color.white))

        tvSelectTeam1.elevation = resources.getDimension(R.dimen.view_4dp)
        tvSelectTeam1.backgroundTintList =
            ColorStateList.valueOf(getColorFromAttr(R.attr.colorPrimary))
        tvSelectTeam1.isEnabled = true

        tvSelectTeam2.elevation = resources.getDimension(R.dimen.view_4dp)
        tvSelectTeam2.backgroundTintList =
            ColorStateList.valueOf(getColorFromAttr(R.attr.colorPrimary))
        tvSelectTeam2.isEnabled = true
    }

    override fun onBatsmanLoadMore() {
        pushIntent(Intent.BatmanLoadMore)
    }

    override fun onBowlersLoadMore() {
        pushIntent(Intent.BowlerLoadMore)
    }

    override fun onDestroy() {
        super.onDestroy()
        timerDisposable?.dispose()
    }

    override fun boosterSubmitted(question: BoosterQuestion.Question, choice: String) {
        // add explicit delay so that intent relay is subscribed
        pushIntentWithDelay(Intent.BoosterSubmitted(choice, question.id))
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: android.content.Intent?) {
        if (requestCode == REQUEST_MERCHANT_INPUT_SCREEN || requestCode == REQUEST_ADD_CUSTOMER ||
            requestCode == REQUEST_UPDATE_BUSINESS_CATEGORY || requestCode == REQUEST_ADD_CONTACT ||
            requestCode == REQUEST_VOICE_COLLECTION
        ) {
            if (resultCode == Activity.RESULT_OK) {
                Timber.d("Booster task completed for requestCode = $requestCode")
                val message = when (requestCode) {
                    REQUEST_MERCHANT_INPUT_SCREEN -> {
                        val inputType = data?.getIntExtra("input_type", -1) ?: -1
                        getSuccessMessageForMerchantInput(inputType)
                    }
                    REQUEST_UPDATE_BUSINESS_CATEGORY -> getString(R.string.message_business_category_success)
                    REQUEST_ADD_CUSTOMER -> getString(R.string.message_add_customer_success)
                    REQUEST_ADD_CONTACT -> getString(R.string.contact_added)
                    else -> getString(R.string.success)
                }
                showSuccessSheet(message)
            } else {
                Timber.d("Booster task cancelled for requestCode = $requestCode")
            }
        }
    }

    internal fun showSuccessSheet(message: String, type: BusinessType? = null) {
        val boosterSuccessBottomSheet = BoosterSuccessBottomSheet.newInstance(message).apply {
            setListener {
                if (type != null) {
                    pushIntentWithDelay(Intent.BusinessTypeSelected(type))
                } else {
                    pushIntentWithDelay(Intent.BoosterTaskCompleted)
                }
            }
        }
        boosterSuccessBottomSheet.show(childFragmentManager, BoosterSuccessBottomSheet::class.java.simpleName)
    }

    private fun getSuccessMessageForMerchantInput(inputType: Int): String {
        return when (inputType) {
            BusinessConstants.ADDRESS -> getString(R.string.message_business_location_success)
            BusinessConstants.BUSINESS_NAME -> getString(R.string.message_business_name_success)
            BusinessConstants.BUSINESS_TYPE -> getString(R.string.message_business_type_success)
            BusinessConstants.ABOUT -> getString(R.string.message_merchant_about_success)
            BusinessConstants.EMAIL -> getString(R.string.message_merchant_email_success)
            BusinessConstants.PERSON_NAME -> getString(R.string.message_merchant_name_success)
            else -> getString(R.string.success)
        }
    }

    private fun initializePlayer(link: String) {
        binding.boosterQuestionCard.youtubePlayerView.apply {
            lifecycle.addObserver(this)
            this.clipToOutline = true
            getYouTubePlayerWhenReady(object : YouTubePlayerCallback {
                override fun onYouTubePlayer(youTubePlayer: YouTubePlayer) {
                    youTubePlayer.loadVideo(getYouTubeId(link), 0f)
                    youTubePlayer.addListener(object : YouTubePlayerListener {
                        override fun onApiChange(youTubePlayer: YouTubePlayer) {
                        }

                        override fun onCurrentSecond(youTubePlayer: YouTubePlayer, second: Float) {
                        }

                        override fun onError(youTubePlayer: YouTubePlayer, error: PlayerConstants.PlayerError) {
                        }

                        override fun onPlaybackQualityChange(
                            youTubePlayer: YouTubePlayer,
                            playbackQuality: PlayerConstants.PlaybackQuality,
                        ) {
                        }

                        override fun onPlaybackRateChange(
                            youTubePlayer: YouTubePlayer,
                            playbackRate: PlayerConstants.PlaybackRate,
                        ) {
                        }

                        override fun onReady(youTubePlayer: YouTubePlayer) {
                        }

                        override fun onStateChange(youTubePlayer: YouTubePlayer, state: PlayerConstants.PlayerState) {
                            if (state == PlayerConstants.PlayerState.ENDED) {
                                binding.boosterQuestionCard.youtubePlayerView.gone()
                                binding.boosterQuestionCard.contentGroup.visible()
                            }
                        }

                        override fun onVideoDuration(youTubePlayer: YouTubePlayer, duration: Float) {
                        }

                        override fun onVideoId(youTubePlayer: YouTubePlayer, videoId: String) {
                        }

                        override fun onVideoLoadedFraction(youTubePlayer: YouTubePlayer, loadedFraction: Float) {
                        }
                    })
                }
            })
        }
    }

    internal fun getYouTubeId(youTubeUrl: String): String {
        val pattern = "(?<=youtu.be/|watch\\?v=|/videos/|embed/)[^#&?]*"
        val compiledPattern: Pattern = Pattern.compile(pattern)
        val matcher: Matcher = compiledPattern.matcher(youTubeUrl)
        return if (matcher.find()) {
            matcher.group()
        } else {
            "error"
        }
    }

    private fun showGameToolTip() {
        balloon = createBalloon(requireContext()) {
            setArrowSize(8)
            setWidthRatio(0.7f)
            setArrowPosition(0.95f)
            setArrowOrientation(ArrowOrientation.TOP)
            setCornerRadius(8f)
            setMarginRight(12)
            setAlpha(1f)
            setPadding(8)
            setText(
                getString(
                    R.string.game_rules_tool_tip,
                    getCurrentState().onboardingDetails?.pointsFormulae?.wicketMultiplier.toString(),
                    getCurrentState().onboardingDetails?.pointsFormulae?.winningPoints.toString()
                )
            )
            setTextColorResource(R.color.white)
            setBackgroundColorResource(R.color.indigo_1)
            setOnBalloonClickListener(object : OnBalloonClickListener {
                override fun onBalloonClick(view: View) {
                    balloon?.dismiss()
                }
            })
            setBalloonAnimation(BalloonAnimation.FADE)
            setLifecycleOwner(lifecycleOwner)
        }
        balloon?.apply {
            show(binding.inProgressCard.tvTotalPoints, 0, 0)
            showAlignBottom(binding.inProgressCard.tvTotalPoints, 0, 0)
        }
    }

    // some booster after completion by user, need some time for backend to process
    // for this we show waiting time in the booster task
    fun onBoosterTaskReflectWaitingTime(eta: Long) {
        val minutes = TimeUnit.SECONDS.toMinutes(eta) + 2
        val pendingText =
            resources.getQuantityString(R.plurals.booster_pending, minutes.toInt(), minutes.toString())
        shortToast(pendingText)
        binding.boosterQuestionCard.pendingState.apply {
            text = pendingText
            visible()
        }
        binding.boosterQuestionCard.buttonSubmit.apply {
            setBackgroundColor(ContextCompat.getColor(requireContext(), R.color.grey600))
            disable()
        }
    }

    private fun addOkcNumberInDevice() {
        if (Permission.isContactPermissionAlreadyGranted(requireContext())) {
            pushIntentWithDelay(Intent.AddOkNumberInDevice)
        } else {
            openSaveContactDeviceScreen()
        }
    }

    private fun openSaveContactDeviceScreen() {
        try {
            val helpNumber = firebaseRemoteConfig.get().getString(FRC_SUPPORT_NUMBER_KEY)
            val contactIntent =
                android.content.Intent(ContactsContract.Intents.Insert.ACTION)
            contactIntent.type = ContactsContract.RawContacts.CONTENT_TYPE
            contactIntent
                .putExtra(ContactsContract.Intents.Insert.NAME, OKC_NAME)
                .putExtra(ContactsContract.Intents.Insert.PHONE, helpNumber)
                .putExtra(INTENT_KEY_FINISH_ACTIVITY_ON_SAVE_COMPLETED, true)

            startActivityForResult(contactIntent, REQUEST_ADD_CONTACT)
        } catch (e: Exception) {
            shortToast(getString(R.string.err_default))
        }
    }
}
