package `in`.okcredit.collection.contract

import io.reactivex.Completable
import io.reactivex.Single

interface ReferralEducationPreference {
    fun setReferralEducationShown(): Completable
    fun shouldShowReferralEducationScreen(): Single<Boolean>
}
