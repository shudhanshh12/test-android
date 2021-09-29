package tech.okcredit.home.usecase

import `in`.okcredit.merchant.contract.GetActiveBusinessId
import `in`.okcredit.merchant.core.CoreSdk
import dagger.Lazy
import io.reactivex.Observable
import kotlinx.coroutines.rx2.asObservable
import javax.inject.Inject

class GetImmutableCustomersCount @Inject constructor(
    private val coreSdk: CoreSdk,
    private val getActiveBusinessId: Lazy<GetActiveBusinessId>,
) {
    fun execute(): Observable<Int> {
        return getActiveBusinessId.get().execute().flatMapObservable { businessId ->
            coreSdk.getImmutableCustomersCount(businessId).asObservable()
        }
    }
}
