package `in`.okcredit.individual.di

import `in`.okcredit.individual.BuildConfig
import `in`.okcredit.individual.IndividualRepositoryImpl
import `in`.okcredit.individual.contract.GetIndividual
import `in`.okcredit.individual.contract.IndividualRepository
import `in`.okcredit.individual.contract.SetIndividualPreference
import `in`.okcredit.individual.contract.SyncIndividual
import `in`.okcredit.individual.contract.UpdateIndividualMobile
import `in`.okcredit.individual.data.local.IndividualDao
import `in`.okcredit.individual.data.local.IndividualDatabase
import `in`.okcredit.individual.data.remote.IndividualApiClient
import `in`.okcredit.individual.usecase.GetIndividualImpl
import `in`.okcredit.individual.usecase.SetIndividualPreferenceImpl
import `in`.okcredit.individual.usecase.SetIndividualPreferenceWorker
import `in`.okcredit.individual.usecase.SyncIndividualImpl
import `in`.okcredit.individual.usecase.UpdateIndividualMobileImpl
import android.content.Context
import android.os.Looper
import dagger.Binds
import dagger.Lazy
import dagger.Provides
import dagger.Reusable
import dagger.multibindings.IntoMap
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.create
import tech.okcredit.android.auth.AuthOkHttpClient
import tech.okcredit.android.base.crashlytics.RecordException
import tech.okcredit.android.base.di.AppScope
import tech.okcredit.android.base.extensions.delegatingCallFactory
import tech.okcredit.android.base.json.GsonUtils
import tech.okcredit.android.base.utils.debug
import tech.okcredit.android.base.utils.release
import tech.okcredit.android.base.workmanager.ChildWorkerFactory
import tech.okcredit.android.base.workmanager.WorkerKey

@dagger.Module
abstract class IndividualModule {

    @Binds
    @AppScope
    abstract fun repository(repository: IndividualRepositoryImpl): IndividualRepository

    @Binds
    @AppScope
    abstract fun updateIndividualMobile(updateIndividualMobile: UpdateIndividualMobileImpl): UpdateIndividualMobile

    @Binds
    @AppScope
    abstract fun getIndividual(getIndividual: GetIndividualImpl): GetIndividual

    @Binds
    @AppScope
    abstract fun syncIndividual(syncIndividual: SyncIndividualImpl): SyncIndividual

    @Binds
    @AppScope
    abstract fun setIndividualPreference(setIndividualPreference: SetIndividualPreferenceImpl): SetIndividualPreference

    @Binds
    @IntoMap
    @WorkerKey(SetIndividualPreferenceWorker::class)
    @Reusable
    abstract fun setIndividualPreferenceWorker(factory: SetIndividualPreferenceWorker.Factory): ChildWorkerFactory

    companion object {
        @Provides
        fun database(context: Context): IndividualDatabase = IndividualDatabase.getInstance(context)

        @Provides
        @Reusable
        fun dao(database: IndividualDatabase): IndividualDao = database.dao()

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
        ): IndividualApiClient {
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
