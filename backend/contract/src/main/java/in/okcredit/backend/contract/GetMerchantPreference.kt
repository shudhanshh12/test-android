package `in`.okcredit.backend.contract

import `in`.okcredit.individual.contract.PreferenceKey
import io.reactivex.Observable

@Deprecated("Please Use getMerchantPreference from individual contract")
interface GetMerchantPreference {

    fun execute(preference: PreferenceKey): Observable<String>
}
