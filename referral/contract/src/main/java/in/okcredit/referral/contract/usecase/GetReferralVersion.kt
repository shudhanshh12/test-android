package `in`.okcredit.referral.contract.usecase

import `in`.okcredit.referral.contract.utils.ReferralVersion
import io.reactivex.Observable

interface GetReferralVersion {
    fun execute(): Observable<ReferralVersion>
}
