package ${escapeKotlinIdentifiers(packageName)}.${featureName?lower_case}._di


import tech.okcredit.base.dagger.di.qualifier.InitialState
import dagger.Module
import dagger.Provides
import tech.okcredit.android.base.extensions.createViewModel
import javax.inject.Provider
import ${escapeKotlinIdentifiers(packageName)}.${featureName?lower_case}.${featureName}Screen
import ${escapeKotlinIdentifiers(packageName)}.${featureName?lower_case}.${featureName}Presenter
import ${escapeKotlinIdentifiers(packageName)}.${featureName?lower_case}.${featureName}Contract

@Module
abstract class ${featureName}Module {

    @Module
    companion object {

        @Provides
        @JvmStatic
        fun initialState(): ${featureName}Contract.State = ${featureName}Contract.State()

        @Provides
        @JvmStatic
        fun viewModel(
            fragment: ${featureName}Screen,
            viewModelProvider: Provider<${featureName}Presenter>
        ): Presenter<${featureName}Contract.State> = fragment.createViewModel(viewModelProvider)
    }
}
