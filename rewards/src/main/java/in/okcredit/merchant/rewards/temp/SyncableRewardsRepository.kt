package `in`.okcredit.merchant.rewards.temp

import `in`.okcredit.merchant.contract.GetActiveBusinessId
import `in`.okcredit.merchant.rewards.RewardsRepositoryImpl
import `in`.okcredit.merchant.rewards.RewardsRepositoryImpl.Companion.TAG
import `in`.okcredit.merchant.rewards.RewardsSyncRepository
import `in`.okcredit.merchant.rewards.server.internal.ApiMessages
import `in`.okcredit.rewards.contract.RewardModel
import `in`.okcredit.rewards.contract.RewardsSyncer
import dagger.Lazy
import io.reactivex.Completable
import io.reactivex.Observable
import io.reactivex.Single
import io.reactivex.subjects.BehaviorSubject
import tech.okcredit.android.auth.AuthService
import tech.okcredit.android.base.rxjava.SchedulerProvider
import timber.log.Timber
import javax.inject.Inject

/********** The purpose of this class is to wait for All rewards Api until its ready.
 * it's ready to serve data if rewards data sync Al least once  **********/

class SyncableRewardsRepository @Inject constructor(
    private val rewardsRepository: RewardsRepositoryImpl,
    private val syncer: RewardsSyncer,
    private val authService: AuthService,
    schedulerProvider: Lazy<SchedulerProvider>,
    private val getActiveBusinessId: Lazy<GetActiveBusinessId>,
) : RewardsSyncRepository {

    private val isReady = BehaviorSubject.createDefault(false)

    init {
        // runs when sdk is initialized in App.onCreate()
        Timber.i("$TAG init SyncableRewardsApi(temp)")
        // schedule sync everything via work manager
        authState()
            .filter {
                Timber.i("$TAG authState() before filter=$it")
                it
            }
            .flatMapCompletable {
                Timber.i("$TAG authState() after filter=$it")
                getActiveBusinessId.get().execute().flatMapCompletable { businessId ->
                    syncer.scheduleEverything(businessId)
                }
            }
            .subscribeOn(schedulerProvider.get().io())
            .subscribe()

        // if at least synced once, then ready for usage
        syncer.getLastSyncEverythingTime()
            .map {
                Timber.i("$TAG getLastSyncEverythingTime() observed value = $it")
                isReady.onNext(it.first)
            }
            .subscribeOn(schedulerProvider.get().io())
            .subscribe()
    }

    fun listRewards(): Observable<List<RewardModel>> {
        Timber.i("$TAG isReady() result ${isReady.value}")
        return isReady().switchMap { rewardsRepository.listRewards() }
    }

    override fun listRewardsFromServer(businessId: String): Single<List<RewardModel>> {
        return rewardsRepository.listRewardsFromServer(businessId)
    }

    fun claimReward(rewardId: String, userLocale: String, businessId: String): Single<ApiMessages.ClaimRewardResponse> {
        return isReady().firstOrError().flatMap { rewardsRepository.claimReward(rewardId, userLocale, businessId) }
    }

    override fun clearLocalData(): Completable {
        return rewardsRepository.clearLocalData()
    }

    private fun isReady(): Observable<Boolean> = isReady
        .filter { it }
        .hide()
        .doOnNext { Timber.d("$TAG IS READY TO ACCESS DATA = $it") }
        .distinctUntilChanged()

    private fun authState(): Observable<Boolean> = Observable.just(authService.isAuthenticated()).distinctUntilChanged()
}
