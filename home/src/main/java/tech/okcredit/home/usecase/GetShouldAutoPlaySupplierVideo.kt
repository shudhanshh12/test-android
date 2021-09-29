package tech.okcredit.home.usecase

import `in`.okcredit.backend.contract.RxSharedPrefValues.CAN_SHOW_SUPPLIER_VIDEO
import dagger.Lazy
import io.reactivex.Single
import kotlinx.coroutines.rx2.asObservable
import kotlinx.coroutines.rx2.rxCompletable
import tech.okcredit.android.base.preferences.DefaultPreferences
import tech.okcredit.android.base.preferences.Scope
import javax.inject.Inject

class GetShouldAutoPlaySupplierVideo @Inject constructor(
    private val defaultPreferences: Lazy<DefaultPreferences>,
) {
    fun execute(): Single<Boolean> {
        return defaultPreferences.get()
            .getBoolean(CAN_SHOW_SUPPLIER_VIDEO, Scope.Individual, true)
            .asObservable()
            .firstOrError()
            .flatMap {
                if (it) {
                    rxCompletable { defaultPreferences.get().set(CAN_SHOW_SUPPLIER_VIDEO, false, Scope.Individual) }
                        .toSingleDefault(it)
                } else {
                    Single.just(it)
                }
            }
    }
}
