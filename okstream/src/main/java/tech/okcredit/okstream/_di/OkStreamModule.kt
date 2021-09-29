package tech.okcredit.okstream._di

import dagger.Binds
import merchant.android.okstream.contract.OkStreamService
import merchant.android.okstream.sdk._di.OkStreamSdkModule
import tech.okcredit.android.base.di.AppScope
import tech.okcredit.okstream.OkStreamServiceImpl

@dagger.Module(includes = [OkStreamSdkModule::class])
abstract class OkStreamModule {
    @Binds
    @AppScope
    abstract fun okStreamService(okStreamService: OkStreamServiceImpl): OkStreamService
}
