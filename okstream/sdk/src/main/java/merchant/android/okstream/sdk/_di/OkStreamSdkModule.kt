package merchant.android.okstream.sdk._di

import android.content.Context
import dagger.Binds
import dagger.Provides
import merchant.android.okstream.sdk.OkStreamSdk
import merchant.android.okstream.sdk.OkStreamSdkImpl
import merchant.android.okstream.sdk.database.OkStreamDataBase
import merchant.android.okstream.sdk.database.OkStreamDataBaseDao
import tech.okcredit.android.base.di.AppScope

@dagger.Module
abstract class OkStreamSdkModule {
    @AppScope
    @Binds
    abstract fun okStreamService(okStreamSdkImpl: OkStreamSdkImpl): OkStreamSdk

    companion object {

        @Provides
        fun database(context: Context): OkStreamDataBase {
            return OkStreamDataBase.getInstance(context)
        }

        @Provides
        fun dao(database: OkStreamDataBase): OkStreamDataBaseDao {
            return database.contactsDataBaseDao()
        }
    }
}
