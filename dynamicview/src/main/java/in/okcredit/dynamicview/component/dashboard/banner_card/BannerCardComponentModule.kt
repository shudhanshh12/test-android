package `in`.okcredit.dynamicview.component.dashboard.banner_card

import `in`.okcredit.dynamicview.component.ComponentFactory
import `in`.okcredit.dynamicview.data.model.ComponentModel
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.multibindings.IntoMap
import dagger.multibindings.StringKey

@Module
abstract class BannerCardComponentModule {

    @Binds
    @IntoMap
    @StringKey(BannerCardComponentModel.KIND)
    abstract fun bannerComponentViewFactory(componentFactory: BannerCardComponentFactory): ComponentFactory

    companion object {

        @Provides
        @IntoMap
        @StringKey(BannerCardComponentModel.KIND)
        fun bannerComponent(): Class<out ComponentModel> = BannerCardComponentModel::class.java
    }
}
