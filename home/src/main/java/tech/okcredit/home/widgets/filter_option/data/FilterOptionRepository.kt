package tech.okcredit.home.widgets.filter_option.data

import `in`.okcredit.home.HomePreferences
import dagger.Lazy
import io.reactivex.Single
import kotlinx.coroutines.rx2.asObservable
import kotlinx.coroutines.rx2.rxCompletable
import tech.okcredit.android.base.preferences.Scope
import javax.inject.Inject

class FilterOptionRepository @Inject constructor(
    private val homePreferences: Lazy<HomePreferences>
) {
    companion object {
        const val FILTER_EDUCATION_SHOWN = "key_filter_education_shown"
    }

    fun setFilterEducationPreference(shown: Boolean) = rxCompletable {
        homePreferences.get().set(FILTER_EDUCATION_SHOWN, shown, Scope.Individual)
    }

    fun canShowFilterEducation(): Single<Boolean> =
        homePreferences.get().getBoolean(FILTER_EDUCATION_SHOWN, Scope.Individual).asObservable().firstOrError()
}
