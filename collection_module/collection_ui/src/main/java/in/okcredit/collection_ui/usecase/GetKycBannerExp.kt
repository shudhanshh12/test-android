package `in`.okcredit.collection_ui.usecase

import `in`.okcredit.backend.contract.RxSharedPrefValues
import `in`.okcredit.shared.usecase.Result
import `in`.okcredit.shared.usecase.UseCase
import dagger.Lazy
import io.reactivex.Observable
import kotlinx.coroutines.rx2.asObservable
import kotlinx.coroutines.rx2.rxCompletable
import tech.okcredit.android.ab.AbRepository
import tech.okcredit.android.base.preferences.DefaultPreferences
import tech.okcredit.android.base.preferences.Scope
import javax.inject.Inject

class GetKycBannerExp @Inject constructor(
    private val ab: Lazy<AbRepository>,
    private val rxSharedPreference: Lazy<DefaultPreferences>,
) : UseCase<Unit, Boolean> {

    companion object {
        const val EXPT_NAME = "postlogin_android-all-kyc_banner_on_qr_page"
        const val SHOW_BANNER = "show"
        const val HIDE_BANNER = "dont_show"
    }

    override fun execute(req: Unit): Observable<Result<Boolean>> {
        return UseCase.wrapObservable(
            isExperimentEnabled()
                .flatMap {
                    isShowVariant()
                        .flatMap {
                            canShowKycBanner()
                                .flatMap {
                                    return@flatMap Observable.just(true)
                                }
                        }
                }.onErrorReturn { false }
        )
    }

    fun disableKycBanner() = rxCompletable {
        rxSharedPreference.get().set(RxSharedPrefValues.SHOULD_SHOW_KYC_BANNER, false, Scope.Individual)
    }

    private fun isExperimentEnabled() = ab.get().isExperimentEnabled(EXPT_NAME)
        .filter { it }

    private fun isShowVariant() = ab.get().getExperimentVariant(EXPT_NAME)
        .filter { it == SHOW_BANNER }

    private fun canShowKycBanner() =
        rxSharedPreference.get().getBoolean(RxSharedPrefValues.SHOULD_SHOW_KYC_BANNER, Scope.Individual)
            .asObservable()
            .filter { it }
}
