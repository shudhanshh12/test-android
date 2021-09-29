package `in`.okcredit.dynamicview.component.dashboard.cell2

import `in`.okcredit.dynamicview.component.ComponentFactory
import `in`.okcredit.dynamicview.data.model.ComponentModel
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.multibindings.IntoMap
import dagger.multibindings.StringKey

@Module
abstract class Cell2ComponentModule {

    @Binds
    @IntoMap
    @StringKey(Cell2ComponentModel.KIND)
    abstract fun cell2ComponentViewFactory(componentFactory: Cell2ComponentFactory): ComponentFactory

    companion object {

        @Provides
        @IntoMap
        @StringKey(Cell2ComponentModel.KIND)
        fun cell2Component(): Class<out ComponentModel> = Cell2ComponentModel::class.java
    }
}
