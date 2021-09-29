package `in`.okcredit.merchant.rewards.di_

import `in`.okcredit.merchant.rewards.BuildConfig
import `in`.okcredit.merchant.rewards.RewardsNavigatorImpl
import `in`.okcredit.merchant.rewards.RewardsRepositoryImpl
import `in`.okcredit.merchant.rewards.RewardsSyncRepository
import `in`.okcredit.merchant.rewards.RewardsSyncerImpl
import `in`.okcredit.merchant.rewards.helpers.RewardsClaimHelperImpl
import `in`.okcredit.merchant.rewards.server.internal.RewardsApiClient
import `in`.okcredit.merchant.rewards.store.database.RewardsDataBase
import `in`.okcredit.merchant.rewards.store.database.RewardsDataBaseDao
import `in`.okcredit.merchant.rewards.temp.SyncableRewardsRepository
import `in`.okcredit.merchant.rewards.ui.RewardsActivity
import `in`.okcredit.merchant.rewards.ui.rewards_dialog.ClaimRewardActivity
import `in`.okcredit.merchant.rewards.ui.rewards_dialog.di_.ClaimRewardActivityModule
import `in`.okcredit.merchant.rewards.ui.rewards_screen.di_.RewardsActivityModule
import `in`.okcredit.merchant.rewards.usecase.GetRewardByIdImpl
import `in`.okcredit.rewards.contract.GetRewardById
import `in`.okcredit.rewards.contract.RewardsClaimHelper
import `in`.okcredit.rewards.contract.RewardsNavigator
import `in`.okcredit.rewards.contract.RewardsRepository
import `in`.okcredit.rewards.contract.RewardsSyncer
import android.content.Context
import android.os.Looper
import dagger.Binds
import dagger.Lazy
import dagger.Provides
import dagger.Reusable
import dagger.android.ContributesAndroidInjector
import dagger.multibindings.IntoMap
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.create
import tech.okcredit.android.auth.AuthOkHttpClient
import tech.okcredit.android.base.crashlytics.RecordException
import tech.okcredit.android.base.extensions.delegatingCallFactory
import tech.okcredit.android.base.json.GsonUtils
import tech.okcredit.android.base.utils.debug
import tech.okcredit.android.base.utils.release
import tech.okcredit.android.base.workmanager.ChildWorkerFactory
import tech.okcredit.android.base.workmanager.WorkerKey

@dagger.Module
abstract class RewardsModule {

    @Binds
    @Reusable
    abstract fun api(api: SyncableRewardsRepository): RewardsSyncRepository

    @ContributesAndroidInjector(modules = [RewardsActivityModule::class])
    abstract fun rewardActivity(): RewardsActivity

    @ContributesAndroidInjector(modules = [ClaimRewardActivityModule::class])
    abstract fun claimActivity(): ClaimRewardActivity

    @Binds
    @Reusable
    abstract fun rewardsNavigator(rewardsNavigator: RewardsNavigatorImpl): RewardsNavigator

    @Binds
    @Reusable
    abstract fun getRewardById(getRewardById: GetRewardByIdImpl): GetRewardById

    @Binds
    @Reusable
    abstract fun rewardsRepository(rewardsRepository: RewardsRepositoryImpl): RewardsRepository

    @Binds
    @Reusable
    abstract fun syncer(syncer: RewardsSyncerImpl): RewardsSyncer

    @Binds
    @Reusable
    abstract fun rewardsClaimHelper(rewardsClaimHelper: RewardsClaimHelperImpl): RewardsClaimHelper

    @Binds
    @IntoMap
    @WorkerKey(RewardsSyncerImpl.SyncEverythingWorker::class)
    @Reusable
    abstract fun WorkerCustomerTxnAlertDialogDismissWorker(factory: RewardsSyncerImpl.SyncEverythingWorker.Factory): ChildWorkerFactory

    companion object {

        @Provides
        fun database(context: Context): RewardsDataBase {
            checkMainThread()
            return RewardsDataBase.getInstance(context)
        }

        @Provides
        @Reusable
        fun dao(database: RewardsDataBase): RewardsDataBaseDao {
            checkMainThread()
            return database.rewardsDataBaseDao()
        }

        @Suppress("MemberVisibilityCanBePrivate")
        internal fun checkMainThread() {
            if (Looper.getMainLooper() == Looper.myLooper()) {
                debug {
                    throw IllegalStateException("Initialized on main thread.")
                }
                release {
                    RecordException.recordException(IllegalStateException("Initialized on main thread."))
                }
            }
        }

        @Provides
        internal fun apiClient(
            @AuthOkHttpClient defaultOkHttpClient: Lazy<OkHttpClient>,
        ): RewardsApiClient {
            checkMainThread()
            return Retrofit.Builder()
                .baseUrl(BuildConfig.BASE_URL)
                .delegatingCallFactory(defaultOkHttpClient)
                .addConverterFactory(GsonConverterFactory.create(GsonUtils.gson()))
                .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                .build()
                .create()
        }
    }
}
