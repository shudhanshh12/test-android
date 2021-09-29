package `in`.okcredit.dynamicview.component.banner

import `in`.okcredit.dynamicview.component.ComponentFactory
import `in`.okcredit.dynamicview.data.model.ComponentModel
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.multibindings.IntoMap
import dagger.multibindings.StringKey

@Module
abstract class BannerComponentModule {

    @Binds
    @IntoMap
    @StringKey(BannerComponentModel.KIND)
    abstract fun bannerComponentViewFactory(componentFactory: BannerComponentFactory): ComponentFactory

    companion object {

        @Provides
        @IntoMap
        @StringKey(BannerComponentModel.KIND)
        fun bannerComponent(): Class<out ComponentModel> = BannerComponentModel::class.java
    }
}
