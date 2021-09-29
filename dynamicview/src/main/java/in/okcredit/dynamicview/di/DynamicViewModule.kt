package `in`.okcredit.dynamicview.di

import `in`.okcredit.dynamicview.component.ComponentFactory
import `in`.okcredit.dynamicview.component.ComponentFactoryImpl
import `in`.okcredit.dynamicview.data.repository.DynamicViewRepositoryImpl
import `in`.okcredit.dynamicview.usecase.SyncDynamicComponentImpl
import dagger.Binds
import dagger.Module
import dagger.Reusable
import merchant.okcredit.dynamicview.contract.DynamicViewRepository
import merchant.okcredit.dynamicview.contract.SyncDynamicComponent

@Module
abstract class DynamicViewModule {

    @Binds
    @Reusable
    abstract fun componentFactory(componentFactory: ComponentFactoryImpl): ComponentFactory

    @Binds
    @Reusable
    abstract fun dynamicViewApi(dynamicViewRepository: DynamicViewRepositoryImpl): DynamicViewRepository

    @Binds
    @Reusable
    abstract fun syncDynamicComponent(syncDynamicComponent: SyncDynamicComponentImpl): SyncDynamicComponent
}
