package `in`.okcredit.dynamicview.component.dashboard.summary_card

import `in`.okcredit.dynamicview.component.ComponentFactory
import `in`.okcredit.dynamicview.data.model.ComponentModel
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.multibindings.IntoMap
import dagger.multibindings.StringKey

@Module
abstract class SummaryCardComponentModule {

    @Binds
    @IntoMap
    @StringKey(SummaryCardComponentModel.KIND)
    abstract fun summaryCardComponentViewFactory(componentFactory: SummaryCardComponentFactory): ComponentFactory

    companion object {

        @Provides
        @IntoMap
        @StringKey(SummaryCardComponentModel.KIND)
        fun summaryCardComponent(): Class<out ComponentModel> = SummaryCardComponentModel::class.java
    }
}
