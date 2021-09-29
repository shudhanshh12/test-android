package merchant.okcredit.gamification.ipl.data

import `in`.okcredit.merchant.contract.BusinessRepository
import `in`.okcredit.merchant.contract.GetActiveBusiness
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.EventListener
import com.google.firebase.firestore.FirebaseFirestore
import dagger.Lazy
import io.reactivex.Observable
import io.reactivex.Single
import kotlinx.coroutines.rx2.awaitFirst
import merchant.okcredit.gamification.ipl.game.data.server.model.request.*
import merchant.okcredit.gamification.ipl.game.data.server.model.response.BoosterQuestion
import merchant.okcredit.gamification.ipl.game.data.server.model.response.LeaderBoardResponse
import merchant.okcredit.gamification.ipl.game.data.server.model.response.MysteryPrizeModel
import merchant.okcredit.gamification.ipl.game.data.server.model.response.OnboardingDetails
import merchant.okcredit.gamification.ipl.game.data.server.model.response.PredictionResponse
import merchant.okcredit.gamification.ipl.game.data.server.model.response.SundayGameResponse
import merchant.okcredit.gamification.ipl.model.Booster
import merchant.okcredit.gamification.ipl.model.MatchStatus
import merchant.okcredit.gamification.ipl.model.PlayerScore
import merchant.okcredit.gamification.ipl.model.TeamScore
import merchant.okcredit.gamification.ipl.model.isValid
import okhttp3.ResponseBody
import tech.okcredit.android.base.crashlytics.RecordException
import timber.log.Timber
import javax.inject.Inject

class IplRepository @Inject constructor(
    private val remoteDataSource: Lazy<IplRemoteDataSource>,
    private val localDataSource: Lazy<IplLocalDataSource>,
    private val businessRepository: Lazy<BusinessRepository>,
    private val getActiveBusiness: Lazy<GetActiveBusiness>,
    private val firestore: Lazy<FirebaseFirestore>,
) {

    companion object {
        private const val IPL_COLLECTION = "ipl-match-score-card"
        private const val IPL_SYNCS = "ipl-syncs"
        private const val MATCH_COLLECTION = "matches"
        private const val TEAM_COLLECTION = "teams"
        private const val PLAYER_COLLECTION = "players"
    }

    fun getTeamScore(matchId: String, teamId: String): Observable<TeamScore> {
        return Observable.create { emitter ->
            val listenerRegistration = firestore.get().collection(IPL_COLLECTION)
                .document(matchId)
                .collection(TEAM_COLLECTION)
                .document(teamId)
                .addSnapshotListener(
                    EventListener<DocumentSnapshot> { snapshot, e ->
                        if (e != null) {
                            Timber.e("Team score listen failed")
                            RecordException.recordException(e)
                            emitter.onError(e)
                            return@EventListener
                        }

                        if (snapshot?.exists() == true) {
                            val teamScore = snapshot.toObject(TeamScore::class.java)
                            if (teamScore.isValid()) {
                                emitter.onNext(teamScore!!)
                            } else {
                                RecordException.recordException(IllegalArgumentException("Team score is not valid"))
                                Timber.e("Team score is not valid $teamScore")
                            }
                        } else {
                            Timber.e("Team score is not available yet")
                        }
                    }
                )
            emitter.setCancellable { listenerRegistration.remove() }
        }
    }

    fun getPlayerScore(matchId: String, playerId: String): Observable<PlayerScore> {
        return Observable.create { emitter ->
            val listenerRegistration = firestore.get().collection(IPL_COLLECTION)
                .document(matchId)
                .collection(PLAYER_COLLECTION)
                .document(playerId)
                .addSnapshotListener(
                    EventListener<DocumentSnapshot> { snapshot, e ->
                        if (e != null) {
                            emitter.onError(e)
                            Timber.e("Player score listen failed")
                            RecordException.recordException(e)
                            return@EventListener
                        }

                        if (snapshot?.exists() == true) {
                            val playerScore = snapshot.toObject(PlayerScore::class.java)
                            if (playerScore != null) {
                                emitter.onNext(playerScore)
                            } else {
                                RecordException.recordException(IllegalArgumentException("Player score is not valid"))
                                Timber.e("Player score is not valid $snapshot")
                            }
                        } else {
                            Timber.e("Player score is not available yet")
                        }
                    }
                )
            emitter.setCancellable { listenerRegistration.remove() }
        }
    }

    fun getBoosterTrigger(matchId: String): Observable<Booster> {
        return getActiveBusiness.get().execute().switchMap {
            Observable.create<Booster> { emitter ->
                val listener = firestore.get().collection(IPL_SYNCS)
                    .document(it.id)
                    .collection(MATCH_COLLECTION)
                    .document(matchId)
                    .addSnapshotListener(
                        EventListener<DocumentSnapshot> { snapshot, e ->
                            if (e != null) {
                                Timber.e("Player score listen failed")
                                RecordException.recordException(e)
                                return@EventListener
                            }

                            if (snapshot?.exists() == true) {
                                val booster = snapshot.toObject(Booster::class.java)
                                if (booster != null) {
                                    emitter.onNext(booster)
                                } else {
                                    RecordException
                                        .recordException(IllegalArgumentException("Booster trigger is not valid"))
                                    Timber.e("Booster trigger is not valid $snapshot")
                                }
                            } else {
                                Timber.e("Booster trigger is not present")
                            }
                        }
                    )
                emitter.setCancellable { listener.remove() }
            }
        }
    }

    suspend fun getActiveMatches() = remoteDataSource.get().getActivesMatches()

    suspend fun getOnBoardingDetails(matchId: String): OnboardingDetails {
        val merchantId = getActiveBusiness.get().execute().awaitFirst().id
        val onboardingRequest = OnboardingRequest(merchantId = merchantId, matchId = matchId)
        return remoteDataSource.get().getOnboarding(onboardingRequest)
    }

    suspend fun getBoosterQuestion(matchId: String): BoosterQuestion {
        val merchantId = getActiveBusiness.get().execute().awaitFirst().id
        val onboardingRequest = OnboardingRequest(merchantId = merchantId, matchId = matchId)
        return remoteDataSource.get().getBoosterQuestion(onboardingRequest)
    }

    suspend fun getSundayGameDetails(): SundayGameResponse {
        val merchantId = getActiveBusiness.get().execute().awaitFirst().id
        Timber.d("merchantID: $merchantId")
        return remoteDataSource.get().getSundayGameDetails(MerchantRequest(merchantId))
    }

    suspend fun getLeaderBoardDetails(key: String): LeaderBoardResponse {
        val merchantId = getActiveBusiness.get().execute().awaitFirst().id
        Timber.d("merchantID: $merchantId")
        return remoteDataSource.get().getLeaderBoardDetails(LeaderBoardRequest(merchantId, key))
    }

    suspend fun selectTeam(matchId: String, choiceId: String): OnboardingDetails {
        val merchantId = getActiveBusiness.get().execute().awaitFirst().id
        val choiceRequest = ChoiceRequest(merchantId = merchantId, matchId = matchId, choiceId = choiceId)
        return remoteDataSource.get().selectTeam(choiceRequest)
    }

    suspend fun selectBatsman(matchId: String, choiceId: String): OnboardingDetails {
        val merchantId = getActiveBusiness.get().execute().awaitFirst().id
        val choiceRequest = ChoiceRequest(merchantId = merchantId, matchId = matchId, choiceId = choiceId)
        return remoteDataSource.get().selectBatsman(choiceRequest)
    }

    suspend fun selectBowler(matchId: String, choiceId: String): OnboardingDetails {
        val merchantId = getActiveBusiness.get().execute().awaitFirst().id
        val choiceRequest = ChoiceRequest(merchantId = merchantId, matchId = matchId, choiceId = choiceId)
        return remoteDataSource.get().selectBowler(choiceRequest)
    }

    suspend fun submitBoosterChoice(matchId: String, questionId: String, choice: String): OnboardingDetails {
        val merchantId = getActiveBusiness.get().execute().awaitFirst().id
        val choiceRequest =
            SubmitBoosterRequest(merchantId = merchantId, matchId = matchId, questionId = questionId, choice = choice)
        return remoteDataSource.get().submitBoosterChoice(choiceRequest)
    }

    fun getMatchStatus(matchId: String): Observable<MatchStatus> {
        return Observable.create { emitter ->
            val listenerRegistration = firestore.get().collection(IPL_COLLECTION)
                .document(matchId)
                .addSnapshotListener(
                    EventListener<DocumentSnapshot> { snapshot, e ->
                        if (e != null) {
                            Timber.e("Match status listen failed")
                            RecordException.recordException(e)
                            emitter.onError(e)
                            return@EventListener
                        }

                        if (snapshot?.exists() == true) {
                            val matchStatus = snapshot.toObject(MatchStatus::class.java)
                            if (matchStatus != null) {
                                emitter.onNext(matchStatus)
                            } else {
                                RecordException.recordException(IllegalArgumentException("Match status is not valid"))
                                Timber.e("Match status is not valid $snapshot")
                            }
                        } else {
                            Timber.e("Match status is not available yet")
                        }
                    }
                )
            emitter.setCancellable { listenerRegistration.remove() }
        }
    }

    fun getMerchantBusinessTypes(): Observable<List<`in`.okcredit.merchant.contract.BusinessType>> {
        return businessRepository.get().getBusinessTypes()
    }

    suspend fun getMysteryPrizes(): List<MysteryPrizeModel> {
        val merchantId = getActiveBusiness.get().execute().awaitFirst().id
        return remoteDataSource.get().getMysteryPrizes(MerchantRequest(merchantId))
    }

    suspend fun claimMysteryPrize(prizeId: String): ResponseBody {
        val merchantId = getActiveBusiness.get().execute().awaitFirst().id
        return remoteDataSource.get().claimMysteryPrize(ClaimMysteryPrizeRequest(merchantId, prizeId))
    }

    suspend fun clear() {
        localDataSource.get().clear()
        firestore.get().clearPersistence()
    }

    suspend fun getPrediction(matchId: String): PredictionResponse {
        val matchRequest = MatchRequest(matchId = matchId)
        return remoteDataSource.get().getPrediction(matchRequest)
    }

    fun setGamesEducationView() = localDataSource.get().setGamesEducationView()

    fun hasGamesEducationView(): Single<Boolean> {
        return localDataSource.get().hasGamesEducationView()
    }

    fun setHomeScreenDcToolTipViewed() = localDataSource.get().setHomeScreenDcToolTipViewed()

    suspend fun hasHomeScreenDcToolTipViewed() = localDataSource.get().hasHomeScreenDcToolTipViewed()
}
