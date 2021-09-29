package ${escapeKotlinIdentifiers(packageName)}.${featureName?lower_case}


import `in`.okcredit.shared.base.UserIntent
import io.reactivex.Observable
import tech.okcredit.android.base.extensions.viewLifecycleScoped
import ${escapeKotlinIdentifiers(packageName)}.${featureName?lower_case}.${featureName}Contract.*
<#if includeEpoxy>import ${escapeKotlinIdentifiers(packageName)}.${featureName?lower_case}.views.${featureName}View</#if>

class ${featureName}Screen : BaseScreenWithViewEvents<State, ViewEvent, Intent>(
    "${featureName}Screen",
    R.layout.${featureName?lower_case}_screen
)<#if includeEpoxy>, ${featureName}View.Listener</#if> {

    private val binding: ${featureName}ScreenBinding by viewLifecycleScoped(${featureName}ScreenBinding::bind)

    override fun loadIntent(): UserIntent {
        return Intent.Load
    }

    override fun userIntents(): Observable<UserIntent> {
        return Observable.ambArray()
    }

    override fun render(state: State) {}

    override fun handleViewEvent(event: ViewEvent) {}

    <#if includeEpoxy>
    override fun clicked${featureName}View() {
        TODO("Not yet implemented")
    }
    </#if>
}
