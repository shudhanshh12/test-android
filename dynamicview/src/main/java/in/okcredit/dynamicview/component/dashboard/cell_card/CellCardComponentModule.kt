package `in`.okcredit.dynamicview.component.dashboard.cell_card

import `in`.okcredit.dynamicview.component.ComponentFactory
import `in`.okcredit.dynamicview.data.model.ComponentModel
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.multibindings.IntoMap
import dagger.multibindings.StringKey

@Module
abstract class CellCardComponentModule {

    @Binds
    @IntoMap
    @StringKey(CellCardComponentModel.KIND)
    abstract fun cellComponentViewFactory(componentFactory: CellCardComponentFactory): ComponentFactory

    companion object {

        @Provides
        @IntoMap
        @StringKey(CellCardComponentModel.KIND)
        fun cellComponent(): Class<out ComponentModel> = CellCardComponentModel::class.java
    }
}
