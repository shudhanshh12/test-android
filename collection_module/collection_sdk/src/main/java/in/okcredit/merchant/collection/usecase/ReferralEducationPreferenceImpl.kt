package `in`.okcredit.merchant.collection.usecase

import `in`.okcredit.collection.contract.ReferralEducationPreference
import `in`.okcredit.merchant.collection.store.preference.CollectionPreference
import dagger.Lazy
import io.reactivex.Single
import kotlinx.coroutines.rx2.asObservable
import kotlinx.coroutines.rx2.rxCompletable
import tech.okcredit.android.base.preferences.Scope
import javax.inject.Inject

class ReferralEducationPreferenceImpl @Inject constructor(private val collectionPreference: Lazy<CollectionPreference>) :
    ReferralEducationPreference {

    companion object {
        private const val MAX_LIMIT = 2
    }

    override fun setReferralEducationShown() = rxCompletable {
        collectionPreference.get().increment(CollectionPreference.TARGETED_REFERRAL_EDUCATION_SHOWN, Scope.Individual)
    }

    override fun shouldShowReferralEducationScreen(): Single<Boolean> {
        return collectionPreference.get().getInt(CollectionPreference.TARGETED_REFERRAL_EDUCATION_SHOWN, Scope.Individual)
            .asObservable()
            .map { it < MAX_LIMIT }
            .firstOrError()
    }
}
