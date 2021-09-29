package `in`.okcredit.frontend.usecase

import `in`.okcredit.backend.contract.Version
import `in`.okcredit.shared.service.keyval.KeyValService
import `in`.okcredit.shared.usecase.Result
import `in`.okcredit.shared.usecase.UseCase
import com.google.gson.Gson
import io.reactivex.Observable
import tech.okcredit.android.base.preferences.DefaultPreferences.Keys.PREF_INDIVIDUAL_KEY_SERVER_VERSION
import tech.okcredit.android.base.preferences.Scope
import javax.inject.Inject

class GetKnowMoreVideoLinks @Inject constructor(private val keyValService: KeyValService) :
    UseCase<Unit, GetKnowMoreVideoLinks.Response> {

    data class Response(var commonLedgerBuyerVideo: String?, var commonLedgerSellerVideo: String?)

    override fun execute(req: Unit): Observable<Result<Response>> {
        return UseCase.wrapObservable(
            keyValService.contains(PREF_INDIVIDUAL_KEY_SERVER_VERSION, Scope.Individual)
                .filter {
                    it
                }
                .flatMapObservable {
                    keyValService[PREF_INDIVIDUAL_KEY_SERVER_VERSION, Scope.Individual]
                        .map {
                            val version = Gson().fromJson(it, Version::class.java)
                            Response(version.commonLedgerBuyerVideo, version.commonLedgerSellerVideo)
                        }
                }
        )
    }
}
