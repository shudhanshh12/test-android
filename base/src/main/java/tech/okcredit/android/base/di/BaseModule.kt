package tech.okcredit.android.base.di

import dagger.Binds
import dagger.Reusable
import tech.okcredit.android.base.string_resource_provider.StringResourceProvider
import tech.okcredit.android.base.string_resource_provider.StringResourceProviderImpl

@dagger.Module
abstract class BaseModule {

    @Binds
    @Reusable
    abstract fun stringResourceProvider(stringResourceProvider: StringResourceProviderImpl): StringResourceProvider
}
