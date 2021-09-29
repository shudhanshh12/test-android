package merchant.okcredit.gamification.ipl.game.ui

import `in`.okcredit.merchant.contract.BusinessConstants
import `in`.okcredit.merchant.contract.Request
import `in`.okcredit.merchant.contract.UpdateBusiness
import `in`.okcredit.referral.contract.usecase.GetShareAppIntent
import `in`.okcredit.shared.base.BaseViewModel
import `in`.okcredit.shared.base.UiState
import `in`.okcredit.shared.usecase.Result
import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import dagger.Lazy
import io.reactivex.Observable
import io.reactivex.ObservableSource
import io.reactivex.ObservableTransformer
import merchant.okcredit.gamification.ipl.R
import merchant.okcredit.gamification.ipl.game.data.server.model.response.BoosterQuestion
import merchant.okcredit.gamification.ipl.game.data.server.model.response.OnboardingDetails
import merchant.okcredit.gamification.ipl.game.data.server.model.response.QuestionType
import merchant.okcredit.gamification.ipl.game.ui.GameContract.*
import merchant.okcredit.gamification.ipl.game.ui.GameContract.Intent.Load
import merchant.okcredit.gamification.ipl.game.ui.GameContract.PartialState.*
import merchant.okcredit.gamification.ipl.game.ui.youtube.usecase.GetYoutubeLink
import merchant.okcredit.gamification.ipl.game.usecase.*
import merchant.okcredit.gamification.ipl.game.utils.IplEventTracker
import merchant.okcredit.gamification.ipl.model.MatchStatus
import merchant.okcredit.gamification.ipl.model.PlayerScore
import merchant.okcredit.gamification.ipl.utils.IplUtils
import tech.okcredit.android.base.extensions.isNotNullOrBlank
import tech.okcredit.android.base.extensions.itOrBlank
import timber.log.Timber

class GamePresenter constructor(
    initialState: Lazy<State>,
    private val matchId: String,
    private val getOnboardingDetails: Lazy<GetOnboardingDetails>,
    private val selectTeam: Lazy<SelectTeam>,
    private val selectBatsman: Lazy<SelectBatsman>,
    private val selectBowler: Lazy<SelectBowler>,
    private val getTeamScore: Lazy<GetTeamScore>,
    private val getPlayerScore: Lazy<GetPlayerScore>,
    private val getBoosterQuestion: Lazy<GetBoosterQuestion>,
    private val getBusinessTypes: Lazy<GetBusinessTypes>,
    private val submitBoosterAnswer: Lazy<SubmitBoosterAnswer>,
    private val updateBusiness: Lazy<UpdateBusiness>,
    private val getMatchStatus: Lazy<GetMatchStatus>,
    private val boosterTrigger: Lazy<GetBoosterTrigger>,
    private val getPrediction: Lazy<GetPrediction>,
    private val shareAppIntent: Lazy<GetShareAppIntent>,
    private val eventTracker: Lazy<IplEventTracker>,
    private val addOkCreditContact: Lazy<AddOkCreditContact>,
    private val getYoutubeLink: Lazy<GetYoutubeLink>,
    private val firebaseRemoteConfig: Lazy<FirebaseRemoteConfig>
) : BaseViewModel<State, PartialState, ViewEvent>(
    initialState.get()
) {

    companion object {
        const val TOTAL_STEPS = 4
    }

    private var matchStatus: MatchStatus? = null

    override fun handle(): Observable<UiState.Partial<State>> {
        return Observable.mergeArray(
            loadOnboardingDetails(),
            reloadOnboardingDetails(),
            selectTeam(),
            selectBatsman(),
            selectBowlers(),
            getHomeTeamScore(),
            getAwayTeamScore(),
            getBatsmanScore(),
            getBowlerScore(),
            setGameExpired(),
            getMatchStatus(),
            boostedClicked(),
            showAllBatsMan(),
            showAllBowlers(),
            getBoosterQuestion(),
            boosterCompleted(),
            boosterAnswerSubmitted(),
            syncBusinessTypes(),
            updateBusinessType(),
            showAllBowlers(),
            observeBoosterTrigger(),
            loadPrediction(),
            reloadPrediction(),
            observeBoosterExpired(),
            observeFetchShareIntent(),
            observeAddOkCreditContact(),
            getYoutubeVideoUrl()
        )
    }

    private fun getYoutubeVideoUrl(): Observable<PartialState> {
        return intent<Intent.GetYoutubeLink>().switchMap {
            getYoutubeLink.get().execute(it.youtubeLinks)
        }.map {
            when (it) {
                is Result.Success -> {
                    SetYoutubeUrl(it.value)
                }
                else -> NoChange
            }
        }
    }

    private fun observeFetchShareIntent(): ObservableSource<UiState.Partial<State>>? {
        return intent<Intent.FetchShareAppIntent>()
            .switchMap { wrap(shareAppIntent.get().execute()) }
            .map {
                when (it) {
                    is Result.Success -> ShareIntentSuccess(it.value)
                    is Result.Failure -> {
                        when {
                            isInternetIssue(it.error) -> {
                                emitViewEvent(ViewEvent.ShowError(R.string.interent_error))
                                NoChange
                            }
                            else -> {
                                emitViewEvent(ViewEvent.ShowError(R.string.err_default))
                                NoChange
                            }
                        }
                    }
                    else -> NoChange
                }
            }
    }

    private fun observeAddOkCreditContact(): ObservableSource<UiState.Partial<State>>? {
        val helpNumber = firebaseRemoteConfig.get().getString(GameFragment.FRC_SUPPORT_NUMBER_KEY)
        return intent<Intent.AddOkNumberInDevice>()
            .switchMap { addOkCreditContact.get().execute(GameFragment.OKC_NAME, helpNumber) }
            .map {
                when (it) {
                    is Result.Success -> BoosterSuccessState
                    is Result.Failure -> {
                        when {
                            isInternetIssue(it.error) -> ShowNetworkError
                            else -> ShowServerError
                        }
                    }
                    else -> NoChange
                }
            }
    }

    private fun observeBoosterExpired(): ObservableSource<UiState.Partial<State>>? {
        return intent<Intent.BoosterExpired>()
            .map {
                BoosterExpiredState
            }
    }

    private fun observeBoosterTrigger(): ObservableSource<UiState.Partial<State>>? {
        return intent<Intent.BoosterTrigger>().take(1)
            .switchMap {
                boosterTrigger.get().execute(matchId)
            }.map {
                when (it) {
                    is Result.Success -> {
                        BoosterTriggerState(booster = it.value)
                    }
                    else -> NoChange
                }
            }
    }

    private fun updateBusinessType(): ObservableSource<PartialState> {
        return intent<Intent.BusinessTypeSelected>()
            .switchMap {
                wrap(
                    updateBusiness.get().execute(
                        Request(
                            inputType = BusinessConstants.BUSINESS_TYPE,
                            businessType = it.businessType
                        )
                    )
                )
            }.map {
                when (it) {
                    is Result.Success -> BoosterSuccessState
                    is Result.Failure -> {
                        when {
                            isInternetIssue(it.error) -> ShowNetworkError
                            else -> ShowServerError
                        }
                    }
                    else -> NoChange
                }
            }
    }

    private fun syncBusinessTypes(): ObservableSource<PartialState> {
        return intent<Intent.FetchBusinessTypes>()
            .switchMap { getBusinessTypes.get().execute() }
            .map {
                emitViewEvent(ViewEvent.UpdateBusinessType(it))
                NoChange
            }
    }

    private fun boosterAnswerSubmitted(): Observable<PartialState> {
        return intent<Intent.BoosterSubmitted>()
            .switchMap { submitBoosterAnswer.get().execute(matchId, it.question, it.choice) }
            .map {
                when (it) {
                    is Result.Success -> BoosterSuccessState
                    is Result.Failure -> {
                        when {
                            isInternetIssue(it.error) -> {
                                emitViewEvent(ViewEvent.ShowError(R.string.interent_error))
                                NoChange
                            }
                            else -> {
                                emitViewEvent(ViewEvent.ShowError(R.string.err_default))
                                NoChange
                            }
                        }
                    }
                    else -> NoChange
                }
            }
    }

    private fun boosterCompleted(): Observable<PartialState> {
        return intent<Intent.BoosterTaskCompleted>()
            .map {
                BoosterCompleted("")
            }
    }

    private fun boostedClicked(): Observable<PartialState> {
        return intent<Intent.BoosterClicked>()
            .map {
                BoosterClicked
            }
    }

    private fun loadOnboardingDetails(): Observable<PartialState> {
        return intent<Load>()
            .take(1)
            .compose(getOnboardingDetails())
    }

    private fun reloadOnboardingDetails(): Observable<PartialState> {
        return intent<Intent.Retry>()
            .compose(getOnboardingDetails())
    }

    private fun selectTeam(): Observable<PartialState> {
        return intent<Intent.SelectTeam>()
            .switchMap { selectTeam.get().execute(matchId, it.choiceId) }
            .map {
                when (it) {
                    is Result.Progress -> ShowTeamSelectLoading(true)
                    is Result.Success -> {
                        eventTracker.get().gameQuestionAnswered(IplEventTracker.Value.FIRST_QUESTION_ANSWERED)
                        getOnboardingState(it.value)
                    }
                    is Result.Failure -> {
                        when {
                            isInternetIssue(it.error) -> {
                                emitViewEvent(ViewEvent.ShowError(R.string.interent_error))
                                ShowTeamSelectLoading(false)
                            }
                            else -> {
                                emitViewEvent(ViewEvent.ShowError(R.string.err_default))
                                ShowTeamSelectLoading(false)
                            }
                        }
                    }
                }
            }
    }

    private fun selectBatsman(): Observable<PartialState> {
        return intent<Intent.SelectBatsman>()
            .switchMap { selectBatsman.get().execute(matchId, it.choiceId) }
            .map {
                when (it) {
                    is Result.Progress -> ShowBatsmanSelectLoading(true)
                    is Result.Success -> {
                        eventTracker.get().gameQuestionAnswered(IplEventTracker.Value.SECOND_QUESTION_ANSWERED)
                        getOnboardingState(it.value)
                    }
                    is Result.Failure -> {
                        when {
                            isInternetIssue(it.error) -> {
                                emitViewEvent(ViewEvent.ShowError(R.string.interent_error))
                                ShowBatsmanSelectLoading(false)
                            }
                            else -> {
                                emitViewEvent(ViewEvent.ShowError(R.string.err_default))
                                ShowBatsmanSelectLoading(false)
                            }
                        }
                    }
                }
            }
    }

    private fun selectBowlers(): Observable<PartialState> {
        return intent<Intent.SelectBowlers>()
            .switchMap { selectBowler.get().execute(matchId, it.choiceId) }
            .map {
                when (it) {
                    is Result.Progress -> ShowBowlerSelectLoading(true)
                    is Result.Success -> {
                        eventTracker.get().gameQuestionAnswered(IplEventTracker.Value.THIRD_QUESTION_ANSWERED)
                        getOnboardingState(it.value)
                    }
                    is Result.Failure -> {
                        when {
                            isInternetIssue(it.error) -> {
                                emitViewEvent(ViewEvent.ShowError(R.string.interent_error))
                                ShowBowlerSelectLoading(false)
                            }
                            else -> {
                                emitViewEvent(ViewEvent.ShowError(R.string.err_default))
                                ShowBowlerSelectLoading(false)
                            }
                        }
                    }
                }
            }
    }

    private fun getHomeTeamScore(): Observable<PartialState> {
        return intent<Intent.GetTeamScore>()
            .take(1)
            .switchMap { getTeamScore.get().execute(matchId, it.homeTeam.id) }
            .map {
                when (it) {
                    is Result.Success -> HomeTeamScoreState(it.value)
                    is Result.Failure -> {
                        when {
                            isInternetIssue(it.error) -> {
                                emitViewEvent(ViewEvent.ShowSnackBar(R.string.score_no_internet))
                                NoChange
                            }
                            else -> NoChange
                        }
                    }
                    else -> NoChange
                }
            }
    }

    private fun getAwayTeamScore(): Observable<PartialState> {
        return intent<Intent.GetTeamScore>().take(1).switchMap {
            getTeamScore.get().execute(matchId, it.awayTeam.id)
        }.map {
            when (it) {
                is Result.Success -> AwayTeamScoreState(it.value)
                is Result.Failure -> {
                    when {
                        isInternetIssue(it.error) -> {
                            emitViewEvent(ViewEvent.ShowSnackBar(R.string.score_no_internet))
                            NoChange
                        }
                        else -> NoChange
                    }
                }
                else -> NoChange
            }
        }
    }

    private fun getBatsmanScore(): Observable<PartialState> {
        return intent<Intent.GetBatsmanScore>().take(1).switchMap {
            getPlayerScore.get().execute(matchId, it.batsman.id)
        }.map {
            when (it) {
                is Result.Success -> BatsmanScoreState(it.value)
                is Result.Failure -> {
                    when {
                        isInternetIssue(it.error) -> {
                            emitViewEvent(ViewEvent.ShowSnackBar(R.string.score_no_internet))
                            NoChange
                        }
                        else -> NoChange
                    }
                }
                else -> NoChange
            }
        }
    }

    private fun getBowlerScore(): Observable<PartialState> {
        return intent<Intent.GetBowlerScore>().take(1).switchMap {
            getPlayerScore.get().execute(matchId, it.bowler.id)
        }.map {
            when (it) {
                is Result.Success -> BowlerScoreState(it.value)
                is Result.Failure -> {
                    when {
                        isInternetIssue(it.error) -> {
                            emitViewEvent(ViewEvent.ShowSnackBar(R.string.score_no_internet))
                            NoChange
                        }
                        else -> NoChange
                    }
                }
                else -> NoChange
            }
        }
    }

    private fun setGameExpired(): Observable<PartialState> {
        return intent<Intent.GameExpired>().take(1)
            .map {
                SetGameExpired
            }
    }

    private fun showAllBatsMan(): Observable<PartialState> {
        return intent<Intent.BatmanLoadMore>()
            .map {
                ShowAllBatsman
            }
    }

    private fun showAllBowlers(): Observable<PartialState> {
        return intent<Intent.BowlerLoadMore>()
            .map {
                ShowAllBowlers
            }
    }

    private fun getMatchStatus(): Observable<PartialState> {
        return getMatchStatus.get().execute(matchId).map {
            when (it) {
                is Result.Success -> {
                    matchStatus = it.value
                    MatchStatusState(it.value)
                }
                else -> NoChange
            }
        }
    }

    private fun getBoosterQuestion(): Observable<PartialState> {
        return intent<Intent.GetBoosterQuestion>().take(1)
            .switchMap { getBoosterQuestion.get().execute(matchId) }
            .map {
                when (it) {
                    is Result.Success -> {
                        BoosterQuestionState(it.value)
                    }
                    else -> NoChange
                }
            }
    }

    private fun loadPrediction(): Observable<PartialState> {
        return intent<Load>()
            .compose(getPrediction())
    }

    private fun reloadPrediction(): Observable<PartialState> {
        return intent<Intent.Retry>()
            .compose(getPrediction())
    }

    private fun getOnboardingState(onboardingDetails: OnboardingDetails): OnboardingState {
        pushIntent(Intent.GetTeamScore(onboardingDetails.teams.homeTeam, onboardingDetails.teams.awayTeam))
        onboardingDetails.batsmen.chosenPlayer?.let {
            pushIntent(Intent.GetBatsmanScore(it))
        }
        onboardingDetails.bowlers.chosenPlayer?.let {
            pushIntent(Intent.GetBowlerScore(it))
        }
        pushIntent(Intent.BoosterTrigger)
        return OnboardingState(onboardingDetails)
    }

    private fun getOnboardingDetails(): ObservableTransformer<Intent, PartialState> {
        return ObservableTransformer { upstream ->
            upstream.switchMap { getOnboardingDetails.get().execute(matchId) }
                .map {
                    when (it) {
                        is Result.Progress -> ShowLoading
                        is Result.Success -> {
                            pushIntent(Intent.GetYoutubeLink(it.value.youtubeLinks))
                            getOnboardingState(it.value)
                        }
                        is Result.Failure -> {
                            when {
                                isInternetIssue(it.error) -> {
                                    eventTracker.get().networkError(
                                        IplEventTracker.Value.GAME_SCREEN,
                                        IplUtils.getErrorCode(it.error),
                                        it.error.cause?.message
                                    )
                                    ShowNetworkError
                                }
                                else -> {
                                    eventTracker.get()
                                        .serverError(
                                            IplEventTracker.Value.GAME_SCREEN,
                                            IplUtils.getErrorCode(it.error),
                                            it.error.cause?.message
                                        )
                                    NoChange
                                }
                            }
                        }
                    }
                }
        }
    }

    private fun getPrediction(): ObservableTransformer<Intent, PartialState> {
        return ObservableTransformer { upstream ->
            upstream.switchMap { getPrediction.get().execute(matchId) }
                .map {
                    when (it) {
                        is Result.Success -> Prediction(prediction = it.value)
                        else -> NoChange
                    }
                }
        }
    }

    override fun reduce(
        currentState: State,
        partialState: PartialState,
    ): State {
        Timber.d("reduce called currentState = $currentState, partialState = $partialState")
        return when (partialState) {
            is NoChange -> currentState
            is ShowLoading -> currentState.copy(
                isLoading = true,
                isTeamSelectLoading = false,
                networkError = false,
                serverError = false
            )
            is ShowTeamSelectLoading -> currentState.copy(
                isLoading = false,
                isTeamSelectLoading = partialState.loading,
                isBatsmanSelectLoading = false,
                isBowlersSelectLoading = false,
                networkError = false,
                serverError = false
            )
            is ShowBatsmanSelectLoading -> currentState.copy(
                isLoading = false,
                isTeamSelectLoading = false,
                isBatsmanSelectLoading = partialState.loading,
                isBowlersSelectLoading = false,
                networkError = false,
                serverError = false
            )
            is ShowBowlerSelectLoading -> currentState.copy(
                isLoading = false,
                isTeamSelectLoading = false,
                isBatsmanSelectLoading = false,
                isBowlersSelectLoading = partialState.loading,
                networkError = false,
                serverError = false
            )
            is ShowNetworkError -> currentState.copy(
                isLoading = false,
                isTeamSelectLoading = false,
                networkError = true,
                serverError = false
            )
            is ShowServerError -> currentState.copy(
                isLoading = false,
                isTeamSelectLoading = false,
                networkError = false,
                serverError = true
            )
            is OnboardingState -> currentState.copy(
                isLoading = false,
                isTeamSelectLoading = false,
                networkError = false,
                serverError = false,
                onboardingDetails = partialState.onboardingDetails,
                progressCardState = getProgressCardState(partialState.onboardingDetails),
                totalPoints = getTotalPoints(
                    partialState.onboardingDetails,
                    currentState.batsmanScore,
                    currentState.bowlerScore,
                    currentState.boosterCompletedMultiplier
                ),
                chosenTeamWon = hasChosenTeamWon(partialState.onboardingDetails),
                winningTeamName = getWinningTeamName(partialState.onboardingDetails),
                winPoints = getWinPoints(partialState.onboardingDetails, currentState.boosterCompletedMultiplier)
            )
            is HomeTeamScoreState -> currentState.copy(homeTeamScore = partialState.teamScore)
            is AwayTeamScoreState -> currentState.copy(awayTeamScore = partialState.teamScore)
            is BatsmanScoreState -> currentState.copy(
                batsmanScore = partialState.batsmanScore,
                totalPoints = getTotalPoints(
                    currentState.onboardingDetails,
                    partialState.batsmanScore,
                    currentState.bowlerScore,
                    currentState.boosterCompletedMultiplier
                )
            )
            is BowlerScoreState -> currentState.copy(
                bowlerScore = partialState.bowlerScore,
                totalPoints = getTotalPoints(
                    currentState.onboardingDetails,
                    currentState.batsmanScore,
                    partialState.bowlerScore,
                    currentState.boosterCompletedMultiplier
                )
            )
            is SetGameExpired -> currentState.copy(gameExpired = true)
            is ShowAllBatsman -> currentState.copy(showAllBatsman = true)
            is ShowAllBowlers -> currentState.copy(showAllBowler = true)
            is MatchStatusState -> currentState.copy(
                matchStatusText = partialState.matchStatus.status,
                winningTeamName = getWinningTeamName(currentState.onboardingDetails),
                chosenTeamWon = hasChosenTeamWon(currentState.onboardingDetails),
                totalPoints = getTotalPoints(
                    currentState.onboardingDetails,
                    currentState.batsmanScore,
                    currentState.bowlerScore,
                    currentState.boosterCompletedMultiplier
                ),
                winPoints = getWinPoints(currentState.onboardingDetails, currentState.boosterCompletedMultiplier)
            )
            is Prediction -> currentState.copy(prediction = partialState.prediction)
            is BoosterTriggerState -> handleBoosterTrigger(currentState, partialState)
            is BoosterQuestionState -> handleBoosterQuestion(currentState, partialState)
            is BoosterClicked -> {
                currentState.boosterQuestion?.let { setEventBasedOnQuestionType(it) }
                currentState
            }
            is BoosterExpiredState -> handleBoosterExpired(currentState)
            is BoosterSuccessState -> handleBoosterSuccess(currentState)
            is BoosterCompleted -> handleBoosterCompleted(currentState, partialState)
            is ShareIntentSuccess -> {
                emitViewEvent(ViewEvent.ShareApp(partialState.intent))
                currentState
            }
            is SetYoutubeUrl -> currentState.copy(youtubeUrl = partialState.url)
        }
    }

    private fun handleBoosterQuestion(
        currentState: State,
        partialState: BoosterQuestionState,
    ): State {
        if (partialState.boosterQuestion.status) { // user has already completed the booster
            val boosterMultiplier = partialState.boosterQuestion.question.multiplier
            return currentState.copy(
                totalPoints = getTotalPoints(
                    onboardingDetails = currentState.onboardingDetails,
                    batsmanScore = currentState.batsmanScore,
                    bowlerScore = currentState.bowlerScore,
                    boosterMultiplier = boosterMultiplier
                ),
                progressCardState = getProgressCardState(currentState.onboardingDetails, true),
                boosterQuestion = null,
                boosterCompletedMultiplier = boosterMultiplier,
                winPoints = getWinPoints(currentState.onboardingDetails, boosterMultiplier)
            )
        } else if (!partialState.boosterQuestion.question.isExpired() && !currentState.isMatchOver()) {
            // check if booster is not expired and match is not over
            eventTracker.get().boosterCardDisplayed(
                partialState.boosterQuestion.question.questionType.value,
                partialState.boosterQuestion.question.questionSubType
            )
            return currentState.copy(
                boosterQuestion = partialState.boosterQuestion.question
            )
        }
        return currentState
    }

    private fun handleBoosterTrigger(
        currentState: State,
        partialState: BoosterTriggerState,
    ): State {
        // booster task completed check if not already hidden from UI
        if (partialState.booster.booster_status == 2) {
            if (currentState.boosterQuestion != null) {
                val boosterMultiplier = currentState.boosterQuestion.multiplier
                return currentState.copy(
                    totalPoints = getTotalPoints(
                        onboardingDetails = currentState.onboardingDetails,
                        batsmanScore = currentState.batsmanScore,
                        bowlerScore = currentState.bowlerScore,
                        boosterMultiplier = boosterMultiplier
                    ),
                    progressCardState = getProgressCardState(currentState.onboardingDetails, true),
                    boosterQuestion = null,
                    boosterCompletedMultiplier = boosterMultiplier,
                    winPoints = getWinPoints(currentState.onboardingDetails, boosterMultiplier)
                )
            } else {
                pushIntent(Intent.GetBoosterQuestion)
            }
        } else if (partialState.booster.booster_status == 1 && currentState.boosterQuestion == null) {
            // booster activated
            pushIntent(Intent.GetBoosterQuestion)
        }
        return currentState
    }

    private fun handleBoosterExpired(currentState: State): State {
        if (currentState.boosterQuestion != null && currentState.boosterQuestion.isExpired()) {
            return currentState.copy(
                boosterQuestion = null
            )
        }
        return currentState
    }

    private fun handleBoosterSuccess(currentState: State): State {
        return if (currentState.boosterQuestion != null) {
            val boosterMultiplier = currentState.boosterQuestion.multiplier
            currentState.copy(
                totalPoints = getTotalPoints(
                    onboardingDetails = currentState.onboardingDetails,
                    batsmanScore = currentState.batsmanScore,
                    bowlerScore = currentState.bowlerScore,
                    boosterMultiplier = boosterMultiplier
                ),
                progressCardState = getProgressCardState(currentState.onboardingDetails, true),
                boosterQuestion = null,
                boosterCompletedMultiplier = boosterMultiplier,
                winPoints = getWinPoints(currentState.onboardingDetails, boosterMultiplier)
            )
        } else {
            currentState
        }
    }

    private fun handleBoosterCompleted(
        currentState: State,
        partialState: BoosterCompleted,
    ): State {
        if (currentState.boosterQuestion != null) {
            if (currentState.boosterQuestion.questionType == QuestionType.BackendTrackedTask) {
                val boosterMultiplier = currentState.boosterQuestion.multiplier
                return currentState.copy(
                    totalPoints = getTotalPoints(
                        onboardingDetails = currentState.onboardingDetails,
                        batsmanScore = currentState.batsmanScore,
                        bowlerScore = currentState.bowlerScore,
                        boosterMultiplier = boosterMultiplier
                    ),
                    progressCardState = getProgressCardState(currentState.onboardingDetails, true),
                    boosterQuestion = null,
                    boosterCompletedMultiplier = boosterMultiplier,
                    winPoints = getWinPoints(currentState.onboardingDetails, boosterMultiplier)
                )
            } else {
                pushIntent(Intent.BoosterSubmitted(partialState.choice, currentState.boosterQuestion.id))
            }
        }
        return currentState
    }

    private fun getWinPoints(onboardingDetails: OnboardingDetails?, boosterCompletedMultiplier: Int?): Int {
        return if (boosterCompletedMultiplier != null && boosterCompletedMultiplier > 0) {
            (onboardingDetails?.pointsFormulae?.winningPoints ?: 0) * boosterCompletedMultiplier
        } else {
            onboardingDetails?.pointsFormulae?.winningPoints ?: 0
        }
    }

    private fun setEventBasedOnQuestionType(question: BoosterQuestion.Question) {
        eventTracker.get().boosterCardClicked(question.formattedQuestionType(), question.formattedQuestionSubtypeType())
        when (question.questionType) {
            QuestionType.MCQ -> emitViewEvent(ViewEvent.ShowMCQ(question))
            QuestionType.OpenEnded -> emitViewEvent(ViewEvent.ShowOpenQuestion(question))
            QuestionType.ClientTrackedTask -> setEventBasedOnClientSubType(question)
            QuestionType.BackendTrackedTask -> setEventBasedOnBackendTrackType(question)
        }
    }

    private fun setEventBasedOnClientSubType(question: BoosterQuestion.Question) {
        when (question.questionSubType) {
            1 -> emitViewEvent(ViewEvent.RateUs(question.appLink))
            2 -> emitViewEvent(ViewEvent.RateUs(question.appLink))
            3 -> emitViewEvent(ViewEvent.ShareBusinessCard)
            4 -> pushIntent(Intent.FetchShareAppIntent)
            5 -> emitViewEvent(ViewEvent.AddOkcNumberInPhone)
            6 -> emitViewEvent(ViewEvent.InstallOkShopApp(question.appLink))
            7 -> emitViewEvent(ViewEvent.VoiceCollection(question.mcqs.first().itOrBlank()))
        }
    }

    private fun setEventBasedOnBackendTrackType(question: BoosterQuestion.Question) {
        when (question.questionSubType) {
            1 -> pushIntent(Intent.FetchBusinessTypes)
            2 -> emitViewEvent(ViewEvent.UpdateBusinessCategory)
            3 -> emitViewEvent(ViewEvent.UpdateMerchantAddress)
            4 -> emitViewEvent(ViewEvent.SetupBankAccount)
            5 -> emitViewEvent(ViewEvent.UpdateMerchantEmail)
            6 -> emitViewEvent(ViewEvent.UpdateMerchantAbout)
            7 -> emitViewEvent(ViewEvent.UpdateMerchantName)
            8 -> emitViewEvent(ViewEvent.UpdateBusinessName)
            9 -> emitViewEvent(ViewEvent.AddCustomer)
            10 -> emitViewEvent(ViewEvent.AddTransaction)
            11 -> emitViewEvent(ViewEvent.AddTransaction)
            12 -> emitViewEvent(ViewEvent.VoiceCollection(question.mcqs.first().itOrBlank()))
        }
    }

    private fun getProgressCardState(
        onboardingDetails: OnboardingDetails?,
        boosterCompleted: Boolean = false,
    ): State.ProgressCardState {
        return State.ProgressCardState(
            getProgress(onboardingDetails, boosterCompleted),
            getCompletedSteps(onboardingDetails, boosterCompleted)
        )
    }

    private fun getTotalPoints(
        onboardingDetails: OnboardingDetails?,
        batsmanScore: PlayerScore?,
        bowlerScore: PlayerScore?,
        boosterMultiplier: Int? = null,
    ): Int {
        var totalPoints = 0
        totalPoints += (batsmanScore?.batting_runs ?: 0) * (onboardingDetails?.pointsFormulae?.runMultiplier ?: 0)
        totalPoints += (bowlerScore?.bowling_wickets ?: 0) *
            (onboardingDetails?.pointsFormulae?.wicketMultiplier ?: 0)
        if (hasChosenTeamWon(onboardingDetails) == true) {
            totalPoints += onboardingDetails?.pointsFormulae?.winningPoints ?: 0
        }

        if (onboardingDetails?.isQualified() == true) {
            totalPoints += onboardingDetails.pointsFormulae.onboardingPoints
        }

        // This should always be be last step
        if (boosterMultiplier != null && boosterMultiplier > 0) {
            totalPoints *= boosterMultiplier
        }
        return totalPoints
    }

    private fun getCompletedSteps(onboardingDetails: OnboardingDetails?, boosterCompleted: Boolean): Int {
        var completedSteps = 0
        if (onboardingDetails?.teams?.chosenTeam != null) {
            completedSteps++
        }

        if (onboardingDetails?.batsmen?.chosenPlayer != null) {
            completedSteps++
        }

        if (onboardingDetails?.bowlers?.chosenPlayer != null) {
            completedSteps++
        }
        if (boosterCompleted) completedSteps++
        return completedSteps
    }

    private fun getProgress(onboardingDetails: OnboardingDetails?, boosterCompleted: Boolean): Int =
        ((getCompletedSteps(onboardingDetails, boosterCompleted) * 100 / TOTAL_STEPS))

    private fun hasChosenTeamWon(onboardingDetails: OnboardingDetails?): Boolean? {
        val chosenTeam = onboardingDetails?.teams?.chosenTeam
        val winningTeamId = matchStatus?.winning_team_id
        return if (chosenTeam != null && winningTeamId.isNotNullOrBlank()) {
            chosenTeam.id == winningTeamId
        } else {
            null // We don't know yet who has won
        }
    }

    private fun getWinningTeamName(onboardingDetails: OnboardingDetails?): String {
        val homeTeam = onboardingDetails?.teams?.homeTeam
        val awayTeam = onboardingDetails?.teams?.awayTeam
        val winningTeamId = matchStatus?.winning_team_id

        return if (homeTeam != null && awayTeam != null && winningTeamId.isNotNullOrBlank()) {
            if (winningTeamId == homeTeam.id) {
                homeTeam.shortName
            } else {
                awayTeam.shortName
            }
        } else {
            ""
        }
    }
}
