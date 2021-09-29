package tech.okcredit.home.usecase

import `in`.okcredit.home.HomePreferences
import `in`.okcredit.shared.usecase.Result
import `in`.okcredit.shared.usecase.UseCase
import dagger.Lazy
import dagger.Reusable
import io.reactivex.Observable
import kotlinx.coroutines.rx2.asObservable
import tech.okcredit.android.ab.AbRepository
import tech.okcredit.android.base.preferences.Scope
import tech.okcredit.android.base.preferences.blockingSet
import javax.inject.Inject

@Reusable
class GetUploadButtonVisibility @Inject constructor(
    private val ab: Lazy<AbRepository>,
    private val homePreferences: Lazy<HomePreferences>
) {
    companion object {
        const val USER_MIGRATION_EXPERIMENT_NAME = "kb_migration"
        const val UPLOAD_BUTTON_TOOLTIP_SHOWN = "key_upload_button_tooltip_shown"
    }

    fun execute(): Observable<Result<Response>> {
        val observables = listOf(
            isKbMigrationFeatureEnabled().take(1),
            isUploadButtonTooltipShownPreference().take(1)
        )
        return UseCase.wrapObservable(
            Observable.combineLatest(observables) {
                val featureEnabled = it[0] as Boolean
                val isToolTipShown = it[1] as Boolean

                when {
                    featureEnabled.not() -> {
                        Response()
                    }
                    isToolTipShown.not() -> {
                        setUploadButtonTooltipShownPreference(true)
                        Response(
                            canShowUploadButton = true,
                            canShowUploadButtonToolTip = true
                        )
                    }
                    else -> {
                        Response(canShowUploadButton = true)
                    }
                }
            }
        )
    }

    data class Response(
        val canShowUploadButton: Boolean = false,
        val canShowUploadButtonToolTip: Boolean = false
    )

    private fun isKbMigrationFeatureEnabled() = ab.get().isFeatureEnabled(USER_MIGRATION_EXPERIMENT_NAME)

    fun setUploadButtonTooltipShownPreference(shown: Boolean) {
        homePreferences.get().blockingSet(UPLOAD_BUTTON_TOOLTIP_SHOWN, shown, Scope.Individual)
    }

    private fun isUploadButtonTooltipShownPreference(): Observable<Boolean> =
        homePreferences.get().getBoolean(UPLOAD_BUTTON_TOOLTIP_SHOWN, Scope.Individual).asObservable()
}
