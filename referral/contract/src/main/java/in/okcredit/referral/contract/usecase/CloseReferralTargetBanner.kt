package `in`.okcredit.referral.contract.usecase

import io.reactivex.Completable

interface CloseReferralTargetBanner {
    fun execute(): Completable
}
