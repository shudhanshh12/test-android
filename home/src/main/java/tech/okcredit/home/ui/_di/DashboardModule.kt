package tech.okcredit.home.ui._di

import `in`.okcredit.dynamicview.di.DynamicViewModule
import `in`.okcredit.home.GetSupplierCreditEnabledCustomerIds
import `in`.okcredit.home.SetRelationshipAddedAfterOnboarding
import `in`.okcredit.shared.base.MviViewModel
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.Reusable
import dagger.multibindings.IntoMap
import dagger.multibindings.StringKey
import tech.okcredit.android.base.extensions.createViewModel
import tech.okcredit.home.ui.dashboard.DashboardContract
import tech.okcredit.home.ui.dashboard.DashboardFragment
import tech.okcredit.home.ui.dashboard.DashboardViewModel
import tech.okcredit.home.usecase.GetSupplierCreditEnabledCustomerIdsImpl
import tech.okcredit.home.usecase.dashboard.CollectionDefaultersValueProvider
import tech.okcredit.home.usecase.dashboard.CollectionValueProvider
import tech.okcredit.home.usecase.dashboard.DashboardValueProvider
import tech.okcredit.home.usecase.dashboard.NetBalanceValueProvider
import tech.okcredit.home.usecase.pre_network_onboarding.SetRelationshipAddedAfterOnboardingImpl
import javax.inject.Provider

@Module(includes = [DynamicViewModule::class])
abstract class DashboardModule {

    @Binds
    @IntoMap
    @StringKey(NetBalanceValueProvider.NET_BALANCE)
    abstract fun netBalanceProvider(provider: NetBalanceValueProvider): DashboardValueProvider

    @Binds
    @IntoMap
    @StringKey(CollectionValueProvider.COLLECTION)
    abstract fun collectionProvider(provider: CollectionValueProvider): DashboardValueProvider

    @Binds
    @Reusable
    abstract fun setRelationShipAdded(
        setRelationshipAddedAfterOnboardingImpl: SetRelationshipAddedAfterOnboardingImpl,
    ): SetRelationshipAddedAfterOnboarding

    @Binds
    @Reusable
    abstract fun getSupplierCreditEnabledCustomerIds(
        getSupplierCreditEnabledCustomerIds: GetSupplierCreditEnabledCustomerIdsImpl
    ): GetSupplierCreditEnabledCustomerIds

    @Binds
    @IntoMap
    @StringKey(CollectionDefaultersValueProvider.COLLECTION_DEFAULTERS)
    abstract fun collectionDefaultersProvider(provider: CollectionDefaultersValueProvider): DashboardValueProvider

    companion object {

        @Provides
        fun initialState(): DashboardContract.State = DashboardContract.State()

        @Provides
        fun viewModel(
            fragment: DashboardFragment,
            viewModelProvider: Provider<DashboardViewModel>
        ): MviViewModel<DashboardContract.State> = fragment.createViewModel(viewModelProvider)
    }
}
