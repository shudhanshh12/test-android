package tech.okcredit.android.ab.workers

import `in`.okcredit.merchant.contract.GetBusinessIdList
import dagger.Lazy
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.rx2.rxCompletable
import tech.okcredit.android.ab.store.AbLocalSource
import javax.inject.Inject

class ClearAbData @Inject constructor(
    private val getBusinessIdList: Lazy<GetBusinessIdList>,
    private val localSource: Lazy<AbLocalSource>,
) {
    fun execute() = rxCompletable {
        getBusinessIdList.get().execute().first().forEach { businessId ->
            localSource.get().clearAbData(businessId)
        }
    }
}
