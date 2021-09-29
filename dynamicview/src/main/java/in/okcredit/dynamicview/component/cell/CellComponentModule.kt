package `in`.okcredit.dynamicview.component.cell

import `in`.okcredit.dynamicview.component.ComponentFactory
import `in`.okcredit.dynamicview.data.model.ComponentModel
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.multibindings.IntoMap
import dagger.multibindings.StringKey

@Module
abstract class CellComponentModule {

    @Binds
    @IntoMap
    @StringKey(CellComponentModel.KIND)
    abstract fun cellComponentViewFactory(componentFactory: CellComponentFactory): ComponentFactory

    companion object {

        @Provides
        @IntoMap
        @StringKey(CellComponentModel.KIND)
        fun cellComponent(): Class<out ComponentModel> = CellComponentModel::class.java
    }
}
