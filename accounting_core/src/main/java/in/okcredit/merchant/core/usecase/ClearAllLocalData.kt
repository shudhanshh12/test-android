package `in`.okcredit.merchant.core.usecase

import `in`.okcredit.merchant.contract.GetBusinessIdList
import `in`.okcredit.merchant.core.store.CoreLocalSource
import dagger.Lazy
import io.reactivex.Completable
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.rx2.rxCompletable
import tech.okcredit.android.base.preferences.DefaultPreferences
import tech.okcredit.android.base.preferences.Scope
import javax.inject.Inject

class ClearAllLocalData @Inject constructor(
    private val localSource: Lazy<CoreLocalSource>,
    private val getBusinessIdList: Lazy<GetBusinessIdList>,
    private val defaultPreferences: Lazy<DefaultPreferences>,
) {
    fun execute(): Completable {
        return localSource.get().clearCommandTable()
            .andThen(localSource.get().clearTransactionTable())
            .andThen(localSource.get().clearCustomerTable())
            .andThen(clearAllPref())
    }

    private fun clearAllPref(): Completable {
        return rxCompletable {
            getBusinessIdList.get().execute().first().forEach { businessId ->
                defaultPreferences.get()
                    .remove(DefaultPreferences.Keys.PREF_BUSINESS_CORE_SDK_ENABLED, Scope.Business(businessId))
            }
        }
    }
}
