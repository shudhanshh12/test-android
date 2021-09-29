package `in`.okcredit.merchant.usecase

import `in`.okcredit.merchant.contract.SetActiveBusinessId
import `in`.okcredit.merchant.store.sharedprefs.BusinessPreferences
import `in`.okcredit.merchant.usecase.GetActiveBusinessIdImpl.Companion.DEFAULT_BUSINESS_ID
import dagger.Lazy
import io.reactivex.Completable
import kotlinx.coroutines.rx2.rxCompletable
import tech.okcredit.android.base.preferences.Scope
import javax.inject.Inject

class SetActiveBusinessIdImpl @Inject constructor(
    private val preference: Lazy<BusinessPreferences>,
) : SetActiveBusinessId {
    override fun execute(businessId: String): Completable = rxCompletable {
        preference.get().set(DEFAULT_BUSINESS_ID, businessId, Scope.Individual)
    }
}
