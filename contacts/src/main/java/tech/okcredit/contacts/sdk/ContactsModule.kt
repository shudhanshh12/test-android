package tech.okcredit.contacts.sdk

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
import tech.okcredit.android.base.di.AppScope
import tech.okcredit.android.base.extensions.delegatingCallFactory
import tech.okcredit.android.base.json.GsonUtils
import tech.okcredit.android.base.utils.debug
import tech.okcredit.android.base.utils.release
import tech.okcredit.android.base.workmanager.ChildWorkerFactory
import tech.okcredit.android.base.workmanager.WorkerKey
import tech.okcredit.contacts.BuildConfig
import tech.okcredit.contacts.ContactsLocalSource
import tech.okcredit.contacts.ContactsNavigatorImpl
import tech.okcredit.contacts.ContactsRemoteSource
import tech.okcredit.contacts.ContactsRepositoryImpl
import tech.okcredit.contacts.contract.ContactsNavigator
import tech.okcredit.contacts.contract.ContactsRepository
import tech.okcredit.contacts.server.ContactsApiClient
import tech.okcredit.contacts.server.ContactsRemoteSourceImpl
import tech.okcredit.contacts.store.ContactsLocalSourceImpl
import tech.okcredit.contacts.store.database.ContactsDataBase
import tech.okcredit.contacts.store.database.ContactsDataBaseDao
import tech.okcredit.contacts.ui.AddOkCreditContactInAppBottomSheet
import tech.okcredit.contacts.ui.AddOkcreditContactTransparentActivity
import tech.okcredit.contacts.worker.AcknowledgeContactSavedWorker
import tech.okcredit.contacts.worker.AddOkCreditContactsWorker
import tech.okcredit.contacts.worker.CheckForContactsInOkcNetworkWorker
import tech.okcredit.contacts.worker.UploadContactsWorker
import java.lang.IllegalStateException

@dagger.Module
abstract class ContactsModule {

    @Binds
    @IntoMap
    @WorkerKey(UploadContactsWorker.Worker::class)
    @Reusable
    abstract fun uploadContactsWorker(factory: UploadContactsWorker.Worker.Factory): ChildWorkerFactory

    @Binds
    @IntoMap
    @WorkerKey(CheckForContactsInOkcNetworkWorker.Worker::class)
    @Reusable
    abstract fun checkContactsWorker(factory: CheckForContactsInOkcNetworkWorker.Worker.Factory): ChildWorkerFactory

    @Binds
    @IntoMap
    @WorkerKey(AddOkCreditContactsWorker::class)
    @Reusable
    abstract fun addOkCreditContactWorker(factory: AddOkCreditContactsWorker.Factory): ChildWorkerFactory

    @Binds
    @IntoMap
    @WorkerKey(AcknowledgeContactSavedWorker::class)
    @Reusable
    abstract fun acknowledgeContactSavedWorker(factory: AcknowledgeContactSavedWorker.Factory): ChildWorkerFactory

    @Binds
    @Reusable
    abstract fun contactsNavigators(contactsNavigatorImpl: ContactsNavigatorImpl): ContactsNavigator

    @ContributesAndroidInjector
    abstract fun addOkCreditContactInAppBottomSheet(): AddOkCreditContactInAppBottomSheet

    @ContributesAndroidInjector
    abstract fun addOkcreditContactActivity(): AddOkcreditContactTransparentActivity

    @Binds
    @AppScope
    abstract fun repository(repository: ContactsRepositoryImpl): ContactsRepository

    @Binds
    @AppScope
    abstract fun localSource(localSource: ContactsLocalSourceImpl): ContactsLocalSource

    @Binds
    @AppScope
    abstract fun remoteSource(remoteSource: ContactsRemoteSourceImpl): ContactsRemoteSource

    companion object {

        @Provides
        fun database(context: Context): ContactsDataBase {
            return ContactsDataBase.getInstance(context)
        }

        @Provides
        @Reusable
        fun dao(database: ContactsDataBase): ContactsDataBaseDao {
            return database.contactsDataBaseDao()
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
        ): ContactsApiClient {
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
