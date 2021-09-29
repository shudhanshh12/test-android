package tech.okcredit.home.widgets.filter_option.usecase

import `in`.okcredit.shared.usecase.Result
import `in`.okcredit.shared.usecase.UseCase
import dagger.Lazy
import dagger.Reusable
import io.reactivex.Observable
import tech.okcredit.home.widgets.filter_option.data.FilterOptionRepository
import javax.inject.Inject

@Reusable
class GetFilterEducationVisibility @Inject constructor(
    private val filterOptionRepository: Lazy<FilterOptionRepository>,
    private val filterOptionVisibility: Lazy<EnableFilterOptionVisibility>,
) {
    fun execute(): Observable<Result<Boolean>> {
        return UseCase.wrapObservable(
            filterOptionVisibility.get().canEnabledFilterOption().flatMap { enabled ->
                filterOptionRepository.get().canShowFilterEducation()
                    .flatMapObservable { canShowFilterEducation ->
                        filterOptionRepository.get().setFilterEducationPreference(false)
                            .andThen(Observable.just(enabled && canShowFilterEducation))
                    }
            }
        )
    }
}
