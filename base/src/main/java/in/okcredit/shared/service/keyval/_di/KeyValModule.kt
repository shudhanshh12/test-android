package `in`.okcredit.shared.service.keyval._di

import `in`.okcredit.shared.service.keyval.KeyValDao
import `in`.okcredit.shared.service.keyval.KeyValDatabase
import `in`.okcredit.shared.service.keyval.KeyValService
import `in`.okcredit.shared.service.keyval.KeyValServiceImpl
import android.content.Context
import dagger.Binds
import dagger.Module
import dagger.Provides
import tech.okcredit.android.base.di.AppScope

@Module
abstract class KeyValModule {

    @Binds
    @AppScope
    abstract fun keyValService(storage: KeyValServiceImpl): KeyValService

    companion object {

        @Provides
        @AppScope
        fun appDatabase(context: Context): KeyValDatabase = KeyValDatabase.newInstance(context)

        @Provides
        @AppScope
        fun storageDao(appKeyValDatabase: KeyValDatabase): KeyValDao = appKeyValDatabase.storageDao()
    }
}
