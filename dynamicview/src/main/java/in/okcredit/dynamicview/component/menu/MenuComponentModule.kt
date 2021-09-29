package `in`.okcredit.dynamicview.component.menu

import `in`.okcredit.dynamicview.component.ComponentFactory
import `in`.okcredit.dynamicview.data.model.ComponentModel
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.multibindings.IntoMap
import dagger.multibindings.StringKey

@Module
abstract class MenuComponentModule {

    @Binds
    @IntoMap
    @StringKey(MenuComponentModel.KIND)
    abstract fun menuComponentViewFactory(componentFactory: MenuComponentFactory): ComponentFactory

    companion object {

        @Provides
        @IntoMap
        @StringKey(MenuComponentModel.KIND)
        fun menuComponent(): Class<out ComponentModel> = MenuComponentModel::class.java
    }
}
