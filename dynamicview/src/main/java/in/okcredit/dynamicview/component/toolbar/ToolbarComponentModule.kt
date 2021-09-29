package `in`.okcredit.dynamicview.component.toolbar

import `in`.okcredit.dynamicview.component.ComponentFactory
import `in`.okcredit.dynamicview.data.model.ComponentModel
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.multibindings.IntoMap
import dagger.multibindings.StringKey

@Module
abstract class ToolbarComponentModule {

    @Binds
    @IntoMap
    @StringKey(ToolbarComponentModel.KIND)
    abstract fun toolbarComponentViewFactory(componentFactory: ToolbarComponentFactory): ComponentFactory

    companion object {

        @Provides
        @IntoMap
        @StringKey(ToolbarComponentModel.KIND)
        fun toolbarComponent(): Class<out ComponentModel> = ToolbarComponentModel::class.java
    }
}
