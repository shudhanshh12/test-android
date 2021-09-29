package `in`.okcredit.dynamicview.component.recycler

import `in`.okcredit.dynamicview.component.ComponentFactory
import `in`.okcredit.dynamicview.data.model.ComponentModel
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.multibindings.IntoMap
import dagger.multibindings.StringKey

@Module
abstract class RecyclerComponentModule {

    @Binds
    @IntoMap
    @StringKey(RecyclerComponentModel.Kind.VERTICAL)
    abstract fun verticalRecyclerComponentFactory(componentFactory: RecyclerComponentFactory): ComponentFactory

    @Binds
    @IntoMap
    @StringKey(RecyclerComponentModel.Kind.HORIZONTAL)
    abstract fun horizontalRecyclerComponentFactory(componentFactory: RecyclerComponentFactory): ComponentFactory

    @Binds
    @IntoMap
    @StringKey(RecyclerComponentModel.Kind.GRID)
    abstract fun gridRecyclerComponentFactory(componentFactory: RecyclerComponentFactory): ComponentFactory

    companion object {

        @Provides
        @IntoMap
        @StringKey(RecyclerComponentModel.Kind.VERTICAL)
        fun verticalRecyclerComponent(): Class<out ComponentModel> = RecyclerComponentModel::class.java

        @Provides
        @IntoMap
        @StringKey(RecyclerComponentModel.Kind.HORIZONTAL)
        fun horizontalRecyclerRecyclerComponent(): Class<out ComponentModel> = RecyclerComponentModel::class.java

        @Provides
        @IntoMap
        @StringKey(RecyclerComponentModel.Kind.GRID)
        fun gridRecyclerComponent(): Class<out ComponentModel> = RecyclerComponentModel::class.java
    }
}
