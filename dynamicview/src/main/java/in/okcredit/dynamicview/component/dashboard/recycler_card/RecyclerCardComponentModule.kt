package `in`.okcredit.dynamicview.component.dashboard.recycler_card

import `in`.okcredit.dynamicview.component.ComponentFactory
import `in`.okcredit.dynamicview.data.model.ComponentModel
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.multibindings.IntoMap
import dagger.multibindings.StringKey

@Module
abstract class RecyclerCardComponentModule {

    @Binds
    @IntoMap
    @StringKey(RecyclerCardComponentModel.Kind.VERTICAL)
    abstract fun verticalRecyclerComponentFactory(componentFactory: RecyclerCardComponentFactory): ComponentFactory

    @Binds
    @IntoMap
    @StringKey(RecyclerCardComponentModel.Kind.HORIZONTAL)
    abstract fun horizontalRecyclerComponentFactory(componentFactory: RecyclerCardComponentFactory): ComponentFactory

    @Binds
    @IntoMap
    @StringKey(RecyclerCardComponentModel.Kind.GRID)
    abstract fun gridRecyclerComponentFactory(componentFactory: RecyclerCardComponentFactory): ComponentFactory

    companion object {

        @Provides
        @IntoMap
        @StringKey(RecyclerCardComponentModel.Kind.VERTICAL)
        fun verticalRecyclerComponent(): Class<out ComponentModel> = RecyclerCardComponentModel::class.java

        @Provides
        @IntoMap
        @StringKey(RecyclerCardComponentModel.Kind.HORIZONTAL)
        fun horizontalRecyclerRecyclerComponent(): Class<out ComponentModel> = RecyclerCardComponentModel::class.java

        @Provides
        @IntoMap
        @StringKey(RecyclerCardComponentModel.Kind.GRID)
        fun gridRecyclerComponent(): Class<out ComponentModel> = RecyclerCardComponentModel::class.java
    }
}
