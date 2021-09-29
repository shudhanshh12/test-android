package `in`.okcredit.dynamicview.component.dashboard.advertisement

import `in`.okcredit.dynamicview.component.ComponentFactory
import `in`.okcredit.dynamicview.data.model.ComponentModel
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.multibindings.IntoMap
import dagger.multibindings.StringKey

@Module
abstract class AdvertisementComponentModule {

    @Binds
    @IntoMap
    @StringKey(AdvertisementComponentModel.KIND)
    abstract fun AdvertisementComponentViewFactory(componentFactory: AdvertisementComponentFactory): ComponentFactory

    companion object {

        @Provides
        @IntoMap
        @StringKey(AdvertisementComponentModel.KIND)
        fun AdvertisementComponent(): Class<out ComponentModel> = AdvertisementComponentModel::class.java
    }
}
