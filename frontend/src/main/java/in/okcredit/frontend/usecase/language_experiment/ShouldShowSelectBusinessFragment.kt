package `in`.okcredit.frontend.usecase.language_experiment

import `in`.okcredit.merchant.contract.GetBusinessIdList
import `in`.okcredit.merchant.contract.IsMultipleAccountEnabled
import dagger.Lazy
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.rx2.awaitFirst
import javax.inject.Inject

class ShouldShowSelectBusinessFragment @Inject constructor(
    private val getBusinessIdList: Lazy<GetBusinessIdList>,
    private val isMultipleAccountEnabled: Lazy<IsMultipleAccountEnabled>,
) {
    suspend fun execute(): Boolean {
        val enabled = isMultipleAccountEnabled.get().execute().awaitFirst()
        val businessCount = getBusinessIdList.get().execute().first().size
        return businessCount > 1 && enabled
    }
}
