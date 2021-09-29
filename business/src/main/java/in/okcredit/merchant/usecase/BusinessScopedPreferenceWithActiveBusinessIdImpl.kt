package `in`.okcredit.merchant.usecase

import `in`.okcredit.merchant.contract.BusinessScopedPreferenceWithActiveBusinessId
import `in`.okcredit.merchant.contract.GetActiveBusinessId
import dagger.Lazy
import io.reactivex.Completable
import io.reactivex.Single
import kotlinx.coroutines.rx2.await
import kotlinx.coroutines.rx2.rxCompletable
import kotlinx.coroutines.rx2.rxSingle
import tech.okcredit.android.base.preferences.OkcSharedPreferences
import tech.okcredit.android.base.preferences.Scope
import javax.inject.Inject

class BusinessScopedPreferenceWithActiveBusinessIdImpl @Inject constructor(
    private val getActiveBusinessId: Lazy<GetActiveBusinessId>,
) : BusinessScopedPreferenceWithActiveBusinessId {

    override fun setBoolean(prefs: OkcSharedPreferences, key: String, value: Boolean) = rxCompletable {
        val businessId = getActiveBusinessId.get().execute().await()
        prefs.set(key, value, Scope.Business(businessId))
    }

    override fun setString(prefs: OkcSharedPreferences, key: String, value: String) = rxCompletable {
        val businessId = getActiveBusinessId.get().execute().await()
        prefs.set(key, value, Scope.Business(businessId))
    }

    override fun contains(prefs: OkcSharedPreferences, key: String): Single<Boolean> = rxSingle {
        val businessId = getActiveBusinessId.get().execute().await()
        prefs.contains(key, Scope.Business(businessId))
    }

    override fun delete(prefs: OkcSharedPreferences, key: String): Completable = rxCompletable {
        val businessId = getActiveBusinessId.get().execute().await()
        prefs.remove(key, Scope.Business(businessId))
    }
}
