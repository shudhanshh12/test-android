package tech.okcredit.android.referral.usecase

import `in`.okcredit.merchant.contract.GetActiveBusinessId
import `in`.okcredit.referral.contract.ReferralRepository
import `in`.okcredit.referral.contract.usecase.GetReferralLink
import dagger.Lazy
import io.reactivex.Single
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.rx2.asFlow
import javax.inject.Inject

class GetReferralLinkImpl @Inject constructor(
    private val referralRepository: Lazy<ReferralRepository>,
    private val getActiveBusinessId: Lazy<GetActiveBusinessId>,
) : GetReferralLink {

    override fun execute(): Single<String> = getActiveBusinessId.get().execute().flatMap { businessId ->
        referralRepository.get().getReferralLink(businessId).firstOrError()
    }

    override fun executeFlow(): Flow<String> = getActiveBusinessId.get().execute()
        .flatMapObservable { referralRepository.get().getReferralLink(it) }
        .asFlow()
}
